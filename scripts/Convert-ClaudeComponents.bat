@echo off
setlocal enabledelayedexpansion

:: Convert-ClaudeComponents.bat
:: Converts and upgrades all Claude Code components to modern format
::
:: Usage:
::   Convert-ClaudeComponents.bat            (normal run)
::   Convert-ClaudeComponents.bat /dryrun    (preview only, no changes)
::   Convert-ClaudeComponents.bat /force     (force overwrite)
::   Convert-ClaudeComponents.bat /nobackup  (skip backup)

:: ── Parse arguments ──────────────────────────────────────────────────────────
set DRYRUN=0
set BACKUP=1
set FORCE=0

:parse_args
if "%~1"=="" goto done_args
if /i "%~1"=="/dryrun"   set DRYRUN=1
if /i "%~1"=="/nobackup" set BACKUP=0
if /i "%~1"=="/force"    set FORCE=1
shift
goto parse_args
:done_args

:: ── Paths ────────────────────────────────────────────────────────────────────
set PROJECT_ROOT=C:\Users\RyanT\Documents\code\ClaudeCode\mastodon_4j
set CLAUDE_DIR=%PROJECT_ROOT%\.claude

:: ── Header ───────────────────────────────────────────────────────────────────
echo ========================================
echo  Claude Code Components Upgrade Script
echo ========================================
echo.

:: ── Check .claude directory ───────────────────────────────────────────────────
if not exist "%CLAUDE_DIR%" (
    echo [ERROR] .claude directory not found at: %CLAUDE_DIR%
    exit /b 1
)
echo [OK] Found .claude directory
echo.

:: ── Step 1: Backup ───────────────────────────────────────────────────────────
if %BACKUP%==1 if %DRYRUN%==0 (
    echo Step 1: Creating backup...

    :: Build a timestamp string: YYYYMMDD-HHmmss
    for /f "tokens=2 delims==" %%I in ('wmic os get localdatetime /value') do set DT=%%I
    set TIMESTAMP=!DT:~0,8!-!DT:~8,6!
    set BACKUP_PATH=%PROJECT_ROOT%\.claude-backup-!TIMESTAMP!

    xcopy "%CLAUDE_DIR%" "!BACKUP_PATH!\" /E /I /Q >nul
    echo [OK] Backup created: !BACKUP_PATH!
    echo.
)

:: ── Step 2: Create directory structure ───────────────────────────────────────
echo Step 2: Creating new directory structure...

call :mkd "skills\mastodon-migration\references"
call :mkd "skills\mastodon-migration\examples"
call :mkd "skills\mastodon-migration\scripts"
call :mkd "skills\activitypub-federation\references"
call :mkd "skills\activitypub-federation\examples"
call :mkd "skills\spring-boot-patterns\references"
call :mkd "skills\spring-boot-patterns\examples"
call :mkd "skills\java-patterns\references"
call :mkd "skills\java-patterns\examples"
call :mkd "skills\testing-strategies\references"
call :mkd "skills\testing-strategies\examples"
call :mkd "templates\spring"
call :mkd "templates\tests"
call :mkd "templates\patterns\builder-pattern"
call :mkd "templates\patterns\factory-pattern"
call :mkd "templates\patterns\strategy-pattern"
call :mkd "templates\patterns\repository-pattern"
call :mkd "templates\activitypub"
call :mkd "templates\react"
call :mkd "teams\workflows"
call :mkd "teams\templates"
call :mkd "teams\playbooks"
call :mkd "agents\prompts"
call :mkd "agents\coordination"
call :mkd "metrics"
call :mkd "reports"
call :mkd "documentation"

echo [OK] Directory structure created
echo.

:: ── Steps 3 & 4: JSON conversion (requires PowerShell for JSON parsing) ───────
:: Pure batch cannot parse JSON, so these steps call PowerShell inline.
echo Step 3: Converting agents from JSON to MD format...
echo Step 4: Converting skills from JSON to SKILL.md format...

if %DRYRUN%==1 (
    echo [DRY RUN] Skipping JSON conversion steps.
) else (
    powershell -NoProfile -ExecutionPolicy Bypass -Command ^
        "$dryRun = $false;" ^
        "$claudeDir = '%CLAUDE_DIR%';" ^
        "$agentsConverted = 0; $skillsConverted = 0;" ^
        "" ^
        "# -- Convert agents --" ^
        "$agentFiles = Get-ChildItem -Path (Join-Path $claudeDir 'agents') -Filter '*.json' -ErrorAction SilentlyContinue;" ^
        "foreach ($agentFile in $agentFiles) {" ^
        "    $agentName = $agentFile.BaseName;" ^
        "    $mdPath = Join-Path $agentFile.DirectoryName ""$agentName.md"";" ^
        "    if (Test-Path $mdPath) { Write-Host ""  [SKIP] Already exists: $agentName.md""; continue }" ^
        "    try {" ^
        "        $j = Get-Content $agentFile.FullName -Raw | ConvertFrom-Json;" ^
        "        $md = ""---`nname: $($j.name)`ndescription: $($j.description)`nversion: $($j.version)`nmodel: $($j.model)`ntemperature: $($j.temperature)`n---`n`n# $($j.name)`n`n$($j.description)`n"";" ^
        "        $md | Out-File -FilePath $mdPath -Encoding UTF8;" ^
        "        Write-Host ""  [OK] Converted: $agentName.json -> $agentName.md"";" ^
        "        $agentsConverted++;" ^
        "    } catch { Write-Host ""  [FAIL] $agentName.json : $_"" }" ^
        "}" ^
        "Write-Host ""[OK] Converted $agentsConverted agents"";" ^
        "" ^
        "# -- Convert skills --" ^
        "$skillFiles = Get-ChildItem -Path (Join-Path $claudeDir 'skills') -Filter '*.json' -ErrorAction SilentlyContinue;" ^
        "foreach ($skillFile in $skillFiles) {" ^
        "    $skillName = $skillFile.BaseName;" ^
        "    $skillDir = Join-Path $skillFile.DirectoryName $skillName;" ^
        "    $skillMdPath = Join-Path $skillDir 'SKILL.md';" ^
        "    if (Test-Path $skillMdPath) { Write-Host ""  [SKIP] Already exists: $skillName/SKILL.md""; continue }" ^
        "    try {" ^
        "        $j = Get-Content $skillFile.FullName -Raw | ConvertFrom-Json;" ^
        "        if (-not (Test-Path $skillDir)) { New-Item -ItemType Directory -Path $skillDir -Force | Out-Null }" ^
        "        $md = ""---`nname: $($j.name)`nversion: $($j.version)`ndescription: $($j.description)`n---`n`n# $($skillName.ToUpper() -replace '-',' ')`n`n$($j.description)`n"";" ^
        "        $md | Out-File -FilePath $skillMdPath -Encoding UTF8;" ^
        "        foreach ($sub in @('references','examples')) {" ^
        "            $subDir = Join-Path $skillDir $sub;" ^
        "            if (-not (Test-Path $subDir)) { New-Item -ItemType Directory -Path $subDir -Force | Out-Null }" ^
        "        }" ^
        "        '# References' | Out-File -FilePath (Join-Path $skillDir 'references\overview.md') -Encoding UTF8;" ^
        "        Write-Host ""  [OK] Converted: $skillName.json -> $skillName/SKILL.md"";" ^
        "        $skillsConverted++;" ^
        "    } catch { Write-Host ""  [FAIL] $skillName.json : $_"" }" ^
        "}" ^
        "Write-Host ""[OK] Converted $skillsConverted skills"";"
)
echo.

:: ── Step 5: Create coordination protocol ─────────────────────────────────────
echo Step 5: Creating agent coordination protocol...

set COORD_DIR=%CLAUDE_DIR%\agents\coordination
set COORD_FILE=%COORD_DIR%\handoff-protocol.md

if %DRYRUN%==0 (
    if not exist "%COORD_FILE%" (
        (
            echo # Agent Handoff Protocol
            echo.
            echo ## Purpose
            echo.
            echo This protocol ensures smooth transitions between agents during multi-agent workflows.
            echo.
            echo ## Handoff Template
            echo.
            echo When one agent completes its task and hands off to another:
            echo.
            echo ### 1. Context Summary
            echo **Completed by:** [Agent Name]
            echo **Completed:** [Date/Time]
            echo **Task:** [What was accomplished]
            echo.
            echo ### 2. Key Decisions Made
            echo - Decision 1
            echo - Decision 2
            echo - Decision 3
            echo.
            echo ### 3. Files Created/Modified
            echo - `path/to/file1.java` - [Description]
            echo - `path/to/file2.java` - [Description]
            echo.
            echo ### 4. Next Steps Required
            echo 1. [Next action]
            echo 2. [Next action]
            echo 3. [Next action]
            echo.
            echo ### 5. Important Context
            echo - [Key information for next agent]
            echo - [Constraints or limitations]
            echo - [Dependencies or blockers]
            echo.
            echo ### 6. Questions for Next Agent
            echo - [ ] Question 1
            echo - [ ] Question 2
            echo.
            echo ### 7. Acceptance Criteria
            echo - [ ] Criteria 1
            echo - [ ] Criteria 2
        ) > "%COORD_FILE%"
        echo [OK] Created coordination protocol
    ) else (
        echo [SKIP] Coordination protocol already exists
    )
) else (
    echo [DRY RUN] Would create: %COORD_FILE%
)
echo.

:: ── Summary ───────────────────────────────────────────────────────────────────
echo ========================================
echo  Conversion Summary
echo ========================================
echo.

if %DRYRUN%==1 (
    echo DRY RUN - No changes were made.
    echo.
)

echo What was done:
echo   - Created new directory structure
echo   - Converted agents to .md format
echo   - Converted skills to SKILL.md format
echo   - Created agent coordination protocol
echo.
echo Next steps:
echo   1. Review converted files
echo   2. Populate template directories
echo   3. Create team playbooks
echo   4. Test with a simple feature
echo.

if %BACKUP%==1 if %DRYRUN%==0 (
    echo Backup location: !BACKUP_PATH!
    echo.
)

echo Conversion complete!
endlocal
exit /b 0

:: ─────────────────────────────────────────────────────────────────────────────
:: Helper: create a subdirectory under CLAUDE_DIR (prints a message)
:mkd
set _FULL=%CLAUDE_DIR%\%~1
if not exist "%_FULL%" (
    if %DRYRUN%==0 mkdir "%_FULL%" >nul 2>&1
    echo   Created: %~1
)
exit /b 0