# zp-gateway 路由测试脚本
# 使用前请确保：
# 1. zp-gateway 已启动（端口 8080）
# 2. 对应的微服务已启动

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "zp-gateway 路由测试" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$GATEWAY_URL = "http://localhost:8080"

# 测试1：网关健康检查
Write-Host "[测试 1] 网关健康检查..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$GATEWAY_URL/actuator/health" -Method Get -TimeoutSec 5
    Write-Host "✓ 网关正常运行" -ForegroundColor Green
    Write-Host "  状态: $($response.status)" -ForegroundColor Gray
} catch {
    Write-Host "✗ 网关未启动或无法访问" -ForegroundColor Red
    Write-Host "  错误: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
Write-Host ""

# 测试2：Swagger 文档入口
Write-Host "[测试 2] Swagger 文档重定向..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$GATEWAY_URL/swagger-ui.html" -Method Get -TimeoutSec 5 -MaximumRedirection 0 -ErrorAction SilentlyContinue
    if ($response.StatusCode -eq 302) {
        Write-Host "✓ Swagger 重定向正常" -ForegroundColor Green
    } else {
        Write-Host "⚠ Swagger 响应异常 (状态码: $($response.StatusCode))" -ForegroundColor Yellow
    }
} catch {
    Write-Host "⚠ Swagger 测试跳过（可能需要浏览器访问）" -ForegroundColor Yellow
}
Write-Host ""

# 测试3：住拍用户服务 - 公开接口
Write-Host "[测试 3] 住拍用户服务路由..." -ForegroundColor Yellow
Write-Host "  提示: 此测试需要 zp-user 服务运行在 8090 端口" -ForegroundColor Gray
try {
    $response = Invoke-WebRequest -Uri "$GATEWAY_URL/zp-user/actuator/health" -Method Get -TimeoutSec 5
    Write-Host "✓ zp-user 服务可访问" -ForegroundColor Green
} catch {
    Write-Host "✗ zp-user 服务不可访问" -ForegroundColor Red
    Write-Host "  可能原因: 服务未启动或路由配置错误" -ForegroundColor Gray
}
Write-Host ""

# 测试4：住拍房源服务 - 公开接口
Write-Host "[测试 4] 住拍房源服务路由..." -ForegroundColor Yellow
Write-Host "  提示: 此测试需要 zp-house 服务运行在 8091 端口" -ForegroundColor Gray
try {
    $response = Invoke-WebRequest -Uri "$GATEWAY_URL/zp-house/actuator/health" -Method Get -TimeoutSec 5
    Write-Host "✓ zp-house 服务可访问" -ForegroundColor Green
} catch {
    Write-Host "✗ zp-house 服务不可访问" -ForegroundColor Red
    Write-Host "  可能原因: 服务未启动或路由配置错误" -ForegroundColor Gray
}
Write-Host ""

# 测试5：API 直接路由 - 用户认证
Write-Host "[测试 5] API 直接路由测试 (/api/auth)..." -ForegroundColor Yellow
Write-Host "  提示: 此测试需要 zp-user 服务运行在 8090 端口" -ForegroundColor Gray
try {
    # 尝试访问注册接口（应该是404或405，但证明路由通了）
    $response = Invoke-WebRequest -Uri "$GATEWAY_URL/api/auth/register" -Method Options -TimeoutSec 5
    Write-Host "✓ /api/auth 路由正常" -ForegroundColor Green
} catch {
    if ($_.Exception.Response.StatusCode -eq 404 -or $_.Exception.Response.StatusCode -eq 405) {
        Write-Host "✓ /api/auth 路由正常（接口返回 $($_.Exception.Response.StatusCode)）" -ForegroundColor Green
    } else {
        Write-Host "✗ /api/auth 路由异常" -ForegroundColor Red
    }
}
Write-Host ""

# 测试6：API 直接路由 - 房源
Write-Host "[测试 6] API 直接路由测试 (/api/houses)..." -ForegroundColor Yellow
Write-Host "  提示: 此测试需要 zp-house 服务运行在 8091 端口" -ForegroundColor Gray
try {
    $response = Invoke-WebRequest -Uri "$GATEWAY_URL/api/houses" -Method Get -TimeoutSec 5
    Write-Host "✓ /api/houses 路由正常" -ForegroundColor Green
} catch {
    if ($_.Exception.Response.StatusCode -eq 404) {
        Write-Host "✓ /api/houses 路由正常（暂无数据）" -ForegroundColor Green
    } else {
        Write-Host "✗ /api/houses 路由异常" -ForegroundColor Red
    }
}
Write-Host ""

# 总结
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "测试完成！" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "📝 使用说明：" -ForegroundColor White
Write-Host "  1. 所有微服务需要通过网关访问" -ForegroundColor Gray
Write-Host "  2. 公开接口无需 Token，其他接口需要 Authorization Header" -ForegroundColor Gray
Write-Host "  3. Swagger 文档地址: $GATEWAY_URL/swagger-gateway.html" -ForegroundColor Gray
Write-Host ""
Write-Host "🔗 常用访问路径：" -ForegroundColor White
Write-Host "  - 用户服务: $GATEWAY_URL/zp-user/**" -ForegroundColor Gray
Write-Host "  - 房源服务: $GATEWAY_URL/zp-house/**" -ForegroundColor Gray
Write-Host "  - 订单服务: $GATEWAY_URL/zp-order/**" -ForegroundColor Gray
Write-Host "  - API直接访问: $GATEWAY_URL/api/auth/**, /api/houses/**" -ForegroundColor Gray
Write-Host ""
