param()

$fail = $false

function Write-CheckResult {
    param(
        [string]$Name,
        [bool]$Ok,
        [string]$Message
    )

    if ($Ok) {
        Write-Host "✅ ${Name}: $Message" -ForegroundColor Green
    } else {
        Write-Host "❌ ${Name} not found. $Message" -ForegroundColor Red
    }
}

Write-Host "Preflight checks" -ForegroundColor Cyan
if ($env:JAVA_HOME) {
    Write-Host "JAVA_HOME: $env:JAVA_HOME" -ForegroundColor Gray
} else {
    Write-Host "JAVA_HOME: (not set)" -ForegroundColor Yellow
}

try {
    $java = Get-Command java -ErrorAction Stop
    $javaVersion = & java --version 2>$null | Select-Object -First 1
    Write-CheckResult -Name "Java" -Ok $true -Message $javaVersion
} catch {
    Write-CheckResult -Name "Java" -Ok $false -Message "Install OpenJDK 25 and ensure JAVA_HOME\\bin is on PATH."
    $fail = $true
}

try {
    if (Test-Path "./mvnw.cmd") {
        Write-CheckResult -Name "Maven Wrapper" -Ok $true -Message "mvnw.cmd found"
    } else {
        throw "Missing mvnw.cmd"
    }
} catch {
    Write-CheckResult -Name "Maven Wrapper" -Ok $false -Message "Run from repo root or ensure mvnw.cmd exists."
    $fail = $true
}

try {
    $docker = Get-Command docker -ErrorAction Stop
    if ($env:DOCKER_HOST -and $env:DOCKER_HOST -like "*docker_cli*") {
        Write-CheckResult -Name "Docker" -Ok $false -Message "DOCKER_HOST points at docker_cli ($env:DOCKER_HOST). Unset or set to npipe:////./pipe/docker_engine."
        $fail = $true
    } else {
        $null = & docker info 2>$null
        if ($LASTEXITCODE -eq 0) {
            $dockerVersion = & docker version --format "{{.Server.Version}}" 2>$null
            if (-not $dockerVersion) {
                $dockerVersion = & docker --version 2>$null
            }
            Write-CheckResult -Name "Docker" -Ok $true -Message $dockerVersion
        } else {
            throw "Docker not running"
        }
    }
} catch {
    Write-CheckResult -Name "Docker" -Ok $false -Message "Start Docker Desktop to run Testcontainers-based tests."
    $fail = $true
}

if ($fail) {
    Write-Host "" 
    Write-Host "Preflight failed. Fix the issues above and rerun." -ForegroundColor Yellow
    exit 1
}

Write-Host "" 
Write-Host "All checks passed." -ForegroundColor Green
