@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ========================================
echo   Jeez Fitness 全模块构建脚本
echo ========================================
echo.

set MODULES=jeez-common jeez-gateway jeez-auth-fitness jeez-member-fitness jeez-coach-fitness jeez-course-fitness jeez-store-fitness jeez-equipment-fitness jeez-manager-fitness
set FAILED=0
set SUCCESS_COUNT=0
set TOTAL=9

echo [1/9] 编译 jeez-common (install)...
call mvn clean install -DskipTests -pl jeez-common -q
if !errorlevel! neq 0 (
    echo [FAILED] jeez-common 编译失败！
    exit /b 1
)
echo [OK] jeez-common
set /a SUCCESS_COUNT+=1

for %%m in (jeez-gateway jeez-auth-fitness jeez-member-fitness jeez-coach-fitness jeez-course-fitness jeez-store-fitness jeez-equipment-fitness jeez-manager-fitness) do (
    set /a SUCCESS_COUNT+=1
    echo [!SUCCESS_COUNT!/9] 编译 %%m...
    call mvn clean compile -DskipTests -pl %%m -q
    if !errorlevel! neq 0 (
        echo [FAILED] %%m 编译失败！
        exit /b 1
    )
    echo [OK] %%m
)

echo.
echo ========================================
echo   全部 9 个模块编译成功！
echo ========================================
exit /b 0
