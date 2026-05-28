#!/usr/bin/env python3
"""
极简Webhook服务 - 自动拉取代码并执行 Docker Compose 部署
"""
from flask import Flask, request, jsonify
import subprocess
import os
import time
import threading
import logging

app = Flask(__name__)

# 项目目录和日志目录
PROJECT_DIR = '/www/jeez.fitness'
DOCKER_DIR = f'{PROJECT_DIR}/docker'
LOG_DIR = f'{PROJECT_DIR}/logs'

# 可配置参数
DEPLOY_BRANCH = os.getenv('DEPLOY_BRANCH', 'release')
BUILD_TIMEOUT_SECONDS = int(os.getenv('BUILD_TIMEOUT_SECONDS', '1800'))
STARTUP_TIMEOUT_SECONDS = int(os.getenv('STARTUP_TIMEOUT_SECONDS', '240'))
PRUNE_UNUSED_IMAGES = os.getenv('PRUNE_UNUSED_IMAGES', 'true').lower() in ('1', 'true', 'yes', 'on')
PRUNE_UNUSED_IMAGE_AGE = os.getenv('PRUNE_UNUSED_IMAGE_AGE', '24h')
EXPECTED_EXITED_SERVICES = {'minio-init'}

# 部署互斥锁，避免 webhook 重试导致并发部署
DEPLOY_LOCK = threading.Lock()

# 创建日志目录
os.makedirs(LOG_DIR, exist_ok=True)

# 配置日志（放到项目logs目录下）
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s [%(levelname)s] %(message)s',
    handlers=[
        logging.FileHandler(f'{LOG_DIR}/webhook.log'),
        logging.StreamHandler()
    ]
)

# 密钥验证（必须设置环境变量）
SECRET = os.getenv('WEBHOOK_SECRET', '')
if not SECRET:
    logging.error("错误：必须设置 WEBHOOK_SECRET 环境变量！")
    raise ValueError("必须设置 WEBHOOK_SECRET 环境变量")

logging.info("Webhook服务启动，项目目录: %s", PROJECT_DIR)
logging.info("日志目录: %s", LOG_DIR)
logging.info("自动部署分支: %s", DEPLOY_BRANCH)


def _mask_secret(text):
    if not text:
        return text
    return text.replace(SECRET, '***')


def _trim_output(text, max_len=12000):
    text = (text or '').strip()
    if not text:
        return ''
    if len(text) <= max_len:
        return text
    return f"[输出过长，仅保留末尾{max_len}字符]\n{text[-max_len:]}"


def run_command(command, cwd=None, timeout=None, check=True):
    command_str = ' '.join(command)
    logging.info("执行命令: %s (cwd=%s)", command_str, cwd or os.getcwd())

    try:
        result = subprocess.run(
            command,
            cwd=cwd,
            capture_output=True,
            text=True,
            timeout=timeout
        )
    except subprocess.TimeoutExpired as exc:
        combined_output = _trim_output((exc.stdout or '') + '\n' + (exc.stderr or ''))
        logging.error("命令超时(%ss): %s", timeout, command_str)
        if combined_output:
            logging.error("超时前输出:\n%s", _mask_secret(combined_output))
        raise RuntimeError(f"命令超时: {command_str}")

    stdout = _trim_output(result.stdout)
    stderr = _trim_output(result.stderr)

    if stdout:
        logging.info("命令输出(stdout):\n%s", _mask_secret(stdout))
    if stderr:
        logging.info("命令输出(stderr):\n%s", _mask_secret(stderr))

    if check and result.returncode != 0:
        combined = '\n'.join(part for part in [stdout, stderr] if part)
        raise RuntimeError(combined or f"命令失败: {command_str} (exit={result.returncode})")

    return result


def cleanup_docker_resources(stage):
    logging.info("开始 Docker 资源清理（%s）...", stage)

    # 清理已退出容器
    run_command(['docker', 'container', 'prune', '-f'], check=False)
    # 清理悬空镜像
    run_command(['docker', 'image', 'prune', '-f'], check=False)
    # 清理长期未使用镜像（可通过环境变量关闭）
    if PRUNE_UNUSED_IMAGES:
        run_command(
            ['docker', 'image', 'prune', '-a', '-f', '--filter', f'until={PRUNE_UNUSED_IMAGE_AGE}'],
            check=False
        )

    run_command(['docker', 'system', 'df'], check=False)
    logging.info("Docker 资源清理完成（%s）", stage)


def get_unexpected_exited_services():
    result = run_command(
        ['docker', 'compose', 'ps', '--services', '--status', 'exited'],
        cwd=DOCKER_DIR,
        check=False
    )
    services = [line.strip() for line in result.stdout.splitlines() if line.strip()]
    return [service for service in services if service not in EXPECTED_EXITED_SERVICES]


def wait_for_services_ready(timeout_seconds):
    deadline = time.time() + timeout_seconds
    while time.time() < deadline:
        unexpected_exited = get_unexpected_exited_services()
        if unexpected_exited:
            for service in unexpected_exited:
                container = f"jeez-{service}"
                run_command(['docker', 'logs', '--tail', '120', container], check=False)
            raise RuntimeError(f"以下服务异常退出: {', '.join(unexpected_exited)}")

        gateway_status = run_command(
            ['docker', 'inspect', '-f', '{{.State.Status}}|{{if .State.Health}}{{.State.Health.Status}}{{else}}none{{end}}|{{.State.ExitCode}}', 'jeez-gateway'],
            check=False
        )

        if gateway_status.returncode == 0:
            status_line = gateway_status.stdout.strip()
            parts = status_line.split('|')
            state = parts[0] if len(parts) > 0 else 'unknown'
            health = parts[1] if len(parts) > 1 else 'unknown'

            if state == 'running' and health in ('healthy', 'none'):
                logging.info("核心服务状态正常：gateway=%s/%s", state, health)
                return

            if state in ('exited', 'dead'):
                run_command(['docker', 'logs', '--tail', '120', 'jeez-gateway'], check=False)
                raise RuntimeError(f"gateway 异常退出: {status_line}")

        time.sleep(5)

    run_command(['docker', 'compose', 'ps'], cwd=DOCKER_DIR, check=False)
    raise RuntimeError(f"等待核心服务就绪超时（>{timeout_seconds}s）")


def run_deploy_pipeline():
    logging.info("开始部署流程...")

    # 0. 预清理，防止资源长期堆积
    cleanup_docker_resources('部署前')

    # 1. 拉取 release 代码
    run_command(['git', 'checkout', DEPLOY_BRANCH], cwd=PROJECT_DIR)
    run_command(['git', 'pull', '--ff-only', 'origin', DEPLOY_BRANCH], cwd=PROJECT_DIR)

    # 2. 先下线旧容器，避免重建时短时内存峰值导致服务被 OOM Kill
    run_command(['docker', 'compose', 'down', '--remove-orphans'], cwd=DOCKER_DIR, check=False)

    # 3. 构建镜像
    run_command(
        ['docker', 'compose', 'build'],
        cwd=DOCKER_DIR,
        timeout=BUILD_TIMEOUT_SECONDS
    )

    # 4. 启动容器
    run_command(['docker', 'compose', 'up', '-d', '--remove-orphans'], cwd=DOCKER_DIR)

    # 5. 等待关键服务稳定
    wait_for_services_ready(STARTUP_TIMEOUT_SECONDS)

    # 6. 部署后清理，移除已退出容器+悬空镜像+长期未使用镜像
    cleanup_docker_resources('部署后')
    run_command(['docker', 'compose', 'ps'], cwd=DOCKER_DIR, check=False)


def should_deploy_current_request():
    # GET 作为手动触发，直接部署
    if request.method == 'GET':
        logging.info("收到手动部署请求（GET）")
        return True, None

    payload = request.get_json(silent=True) or {}
    ref = payload.get('ref', '')
    branch = ref.replace('refs/heads/', '') if ref else ''

    if branch:
        logging.info("收到推送: 分支 %s", branch)
        if branch != DEPLOY_BRANCH:
            logging.info("忽略非%s分支的推送: %s", DEPLOY_BRANCH, branch)
            skip_response = (
                jsonify({
                    'status': 'ignored',
                    'message': f'Branch {branch} is not configured for auto-deploy'
                }),
                200
            )
            return False, skip_response
        return True, None

    logging.warning("POST 请求缺少 ref 字段，按手动部署处理")
    return True, None


@app.route('/deploy', methods=['POST', 'GET'])
def deploy():
    # 1. 验证密钥（支持URL参数或Header）
    token = request.args.get('token', '') or request.headers.get('X-Token', '')
    if token != SECRET:
        logging.warning("密钥验证失败，来自IP: %s", request.remote_addr)
        return jsonify({'error': 'Invalid token'}), 403

    # 2. 检查分支过滤逻辑
    try:
        should_deploy, skip_response = should_deploy_current_request()
        if not should_deploy:
            return skip_response
    except Exception as exc:
        logging.error("解析payload失败: %s", exc)
        return jsonify({'error': 'Invalid payload'}), 400

    # 3. 防并发部署
    if not DEPLOY_LOCK.acquire(blocking=False):
        logging.warning("当前已有部署任务在执行，拒绝并发部署")
        return jsonify({'status': 'busy', 'message': 'Deploy is already running'}), 409

    try:
        run_deploy_pipeline()
    except Exception as exc:
        logging.error("部署失败: %s", exc)
        # 失败时补充端口占用信息，便于定位 80/443 被占用问题
        run_command(['ss', '-ltnp'], check=False)
        run_command(['docker', 'compose', 'ps'], cwd=DOCKER_DIR, check=False)
        return jsonify({'error': 'Deploy failed', 'details': str(exc)}), 500
    finally:
        DEPLOY_LOCK.release()

    logging.info("部署成功完成！")
    return jsonify({'status': 'success', 'message': 'Deployed successfully'}), 200


@app.route('/health', methods=['GET'])
def health():
    """健康检查接口"""
    return jsonify({'status': 'ok'}), 200


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=9999)
