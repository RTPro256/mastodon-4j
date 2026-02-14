# Optional Cleanup Script for mastodon_4j

# This script removes duplicate/unnecessary application classes from library modules

Write-Host "Mastodon 4j - Optional Cleanup" -ForegroundColor Cyan
Write-Host "==============================" -ForegroundColor Cyan
Write-Host ""

$projectRoot = $PSScriptRoot
$removed = 0
$notFound = 0

# Files to remove (duplicate @SpringBootApplication classes in library modules)
$filesToRemove = @(
    "mastodon-streaming\src\main\java\org\joinmastodon\streaming\MastodonStreamingApplication.java",
    "mastodon-streaming\src\main\java\org\joinmastodon\streaming\StreamingApplication.java"
)

Write-Host "Checking for unnecessary application classes..." -ForegroundColor Yellow
Write-Host ""

foreach ($file in $filesToRemove) {
    $fullPath = Join-Path $projectRoot $file
    
    if (Test-Path $fullPath) {
        Write-Host "  [DELETE] $file" -ForegroundColor Red
        Remove-Item $fullPath -Force
        $removed++
    } else {
        Write-Host "  [SKIP] $file (already removed)" -ForegroundColor Gray
        $notFound++
    }
}

Write-Host ""
Write-Host "Cleanup Summary:" -ForegroundColor Cyan
Write-Host "  Files removed: $removed" -ForegroundColor Green
Write-Host "  Already clean: $notFound" -ForegroundColor Gray
Write-Host ""

if ($removed -gt 0) {
    Write-Host "✅ Cleanup complete! Run 'mvnw.cmd clean install' to rebuild." -ForegroundColor Green
} else {
    Write-Host "✅ Project already clean!" -ForegroundColor Green
}
