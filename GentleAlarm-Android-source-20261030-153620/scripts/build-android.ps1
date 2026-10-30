param([switch]$Install)
$ErrorActionPreference = 'Stop'
$projectPath = Split-Path -Parent $PSScriptRoot
Push-Location -LiteralPath $projectPath
try {
    if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
        throw 'Java not found. Install Android Studio and use its bundled JDK 17 or newer.'
    }
    & .\gradlew.bat --no-daemon assembleDebug
    if ($LASTEXITCODE -ne 0) { throw 'Android build failed. See Gradle output above.' }
    $apkPath = Join-Path $projectPath 'app\build\outputs\apk\debug\app-debug.apk'
    Write-Output "APK ready: $apkPath"
    if ($Install) {
        $adbCommand = Get-Command adb -ErrorAction SilentlyContinue
        if (-not $adbCommand) { throw 'adb not on PATH. Add Android SDK platform-tools to PATH, then connect an Android phone with USB debugging enabled.' }
        & $adbCommand.Source install -r $apkPath
        if ($LASTEXITCODE -ne 0) { throw 'APK installation failed.' }
    }
} finally { Pop-Location }
