# Diagnostic Script - Check Application Status

Write-Host "Mastodon 4j - Diagnostics" -ForegroundColor Cyan
Write-Host "=========================" -ForegroundColor Cyan
Write-Host ""

# Check if port 8080 is listening
Write-Host "1. Checking if port 8080 is open..." -ForegroundColor Yellow
$port8080 = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue

if ($port8080) {
    Write-Host "   ✅ Port 8080 is OPEN" -ForegroundColor Green
    Write-Host "   Process ID: $($port8080.OwningProcess)" -ForegroundColor Gray
    
    # Get process name
    $process = Get-Process -Id $port8080.OwningProcess -ErrorAction SilentlyContinue
    if ($process) {
        Write-Host "   Process Name: $($process.ProcessName)" -ForegroundColor Gray
    }
} else {
    Write-Host "   ❌ Port 8080 is CLOSED (application not running)" -ForegroundColor Red
}

Write-Host ""

# Check if Java is running
Write-Host "2. Checking for Java processes..." -ForegroundColor Yellow
$javaProcesses = Get-Process -Name java -ErrorAction SilentlyContinue

if ($javaProcesses) {
    Write-Host "   ✅ Found $($javaProcesses.Count) Java process(es)" -ForegroundColor Green
    foreach ($proc in $javaProcesses) {
        Write-Host "   - PID: $($proc.Id), Memory: $([math]::Round($proc.WorkingSet64/1MB, 2)) MB" -ForegroundColor Gray
    }
} else {
    Write-Host "   ❌ No Java processes running" -ForegroundColor Red
}

Write-Host ""

# Check if database file exists
Write-Host "3. Checking database..." -ForegroundColor Yellow
$dbPath = ".\data\mastodon_dev.mv.db"
if (Test-Path $dbPath) {
    $dbSize = (Get-Item $dbPath).Length / 1KB
    Write-Host "   ✅ Database exists: $([math]::Round($dbSize, 2)) KB" -ForegroundColor Green
} else {
    Write-Host "   ⚠️  Database not created yet (normal for first run)" -ForegroundColor Yellow
}

Write-Host ""

# Try to connect to localhost:8080
Write-Host "4. Testing HTTP connection..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -TimeoutSec 2 -ErrorAction Stop
    Write-Host "   ✅ Application is responding!" -ForegroundColor Green
    Write-Host "   Status: $($response.StatusCode)" -ForegroundColor Gray
} catch {
    Write-Host "   ❌ Cannot connect to http://localhost:8080" -ForegroundColor Red
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Gray
}

Write-Host ""
Write-Host "=========================" -ForegroundColor Cyan
Write-Host ""

# Recommendations
if (-not $port8080) {
    Write-Host "⚠️  Application is NOT running!" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "To start the application:" -ForegroundColor Cyan
    Write-Host "  cd mastodon-web" -ForegroundColor White
    Write-Host "  ..\mvnw.cmd spring-boot:run" -ForegroundColor White
    Write-Host ""
} elseif ($port8080) {
    Write-Host "✅ Application appears to be running!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Try accessing:" -ForegroundColor Cyan
    Write-Host "  http://localhost:8080/actuator/health" -ForegroundColor White
    Write-Host ""
    Write-Host "If Chrome blocks it, try:" -ForegroundColor Cyan
    Write-Host "  - Edge or Firefox" -ForegroundColor White
    Write-Host "  - Incognito mode" -ForegroundColor White
    Write-Host "  - Clear Chrome cache" -ForegroundColor White
}
