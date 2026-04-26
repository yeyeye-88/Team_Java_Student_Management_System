# full_test.ps1 - 完整测试流程

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  {noop} 密码配置完整测试" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. 检查应用是否运行
Write-Host "[1/4] 检查应用状态..." -ForegroundColor Yellow
try {
    $testResponse = Invoke-WebRequest -Uri "http://localhost:22224/auth/getValidateCode" -Method POST -UseBasicParsing -TimeoutSec 2
    Write-Host "✓ 应用正在运行" -ForegroundColor Green
} catch {
    Write-Host "✗ 应用未运行，请先启动应用！" -ForegroundColor Red
    Write-Host "  执行: mvn spring-boot:run" -ForegroundColor Cyan
    exit 1
}

Write-Host ""

# 2. 验证数据库密码格式
Write-Host "[2/4] 验证数据库密码格式..." -ForegroundColor Yellow
$passwordCheck = mysql -h 202.194.14.120 -P 3306 -u java_3_10 -pJavaP310@ java_3_10 -N -e "SELECT COUNT(*) FROM user WHERE password LIKE '{noop}%';"
if ($passwordCheck.Trim() -gt 0) {
    Write-Host "✓ 数据库中存在 {noop} 格式的密码" -ForegroundColor Green
    mysql -h 202.194.14.120 -P 3306 -u java_3_10 -pJavaP310@ java_3_10 -e "SELECT user_name, LEFT(password, 15) as password_preview FROM user WHERE password LIKE '{noop}%';"
} else {
    Write-Host "✗ 数据库中未找到 {noop} 格式的密码" -ForegroundColor Red
}

Write-Host ""

# 3. 测试登录
Write-Host "[3/4] 测试登录功能..." -ForegroundColor Yellow

$testAccounts = @(
    @{ username = "admin"; role = "管理员" },
    @{ username = "stu01"; role = "学生" },
    @{ username = "tea01"; role = "教师" }
)

$successCount = 0
foreach ($account in $testAccounts) {
    $body = @{
        username = $account.username
        password = "123456"
    } | ConvertTo-Json
    
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:22224/auth/login" -Method POST -Body $body -ContentType "application/json" -UseBasicParsing
        
        if ($response.StatusCode -eq 200) {
            Write-Host "  ✓ $($account.role) ($($account.username)) - 登录成功" -ForegroundColor Green
            $successCount++
        }
    } catch {
        Write-Host "  ✗ $($account.role) ($($account.username)) - 登录失败" -ForegroundColor Red
    }
}

Write-Host ""

# 4. 总结
Write-Host "[4/4] 测试结果总结" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan
if ($successCount -eq 3) {
    Write-Host "✓ 所有测试通过！配置成功！" -ForegroundColor Green
} else {
    Write-Host "⚠ 部分测试失败，请检查配置" -ForegroundColor Yellow
}
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "测试账号信息：" -ForegroundColor Cyan
Write-Host "  管理员: admin / 123456" -ForegroundColor White
Write-Host "  学  生: stu01 / 123456" -ForegroundColor White
Write-Host "  教  师: tea01 / 123456" -ForegroundColor White
