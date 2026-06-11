$ErrorActionPreference = 'Stop'

$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $repoRoot

$env:SPRING_DATASOURCE_URL = 'jdbc:mysql://127.0.0.1:3306/zp_pms?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false'
$env:SPRING_DATASOURCE_USERNAME = 'root'
$env:SPRING_DATASOURCE_PASSWORD = '123456'
$env:SPRING_REDIS_HOST = '127.0.0.1'
$env:SPRING_REDIS_PORT = '6379'
$env:ORDER_ID_CARD_SECRET = 'local-order-id-card-secret-2026'

Write-Host "Starting zp-service-order at $(Get-Date -Format o)"
Write-Host "Datasource: $env:SPRING_DATASOURCE_URL"
& mvn -pl zp-common install -DskipTests
& mvn -pl zp-service-order spring-boot:run
