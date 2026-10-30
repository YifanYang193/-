param([string]$JavaHomePath = $env:JAVA_HOME)
$ErrorActionPreference = 'Stop'
if (-not $JavaHomePath) { throw 'Set JAVA_HOME to a JDK, or pass -JavaHomePath with your JDK directory.' }
$javacPath = Join-Path $JavaHomePath 'bin\javac.exe'
$javaPath = Join-Path $JavaHomePath 'bin\java.exe'
$projectPath = Split-Path -Parent $PSScriptRoot
$outputPath = Join-Path $projectPath '.build\checks'
New-Item -ItemType Directory -Path $outputPath -Force | Out-Null
Push-Location -LiteralPath $projectPath
try {
    & $javacPath -encoding UTF-8 -d $outputPath app\src\main\java\com\gentlealarm\app\FadeEnvelope.java app\src\main\java\com\gentlealarm\app\AlarmTime.java checks\CoreChecks.java
    if ($LASTEXITCODE -ne 0) { throw 'Core checks compilation failed.' }
    & $javaPath -cp $outputPath com.gentlealarm.app.CoreChecks
    if ($LASTEXITCODE -ne 0) { throw 'Core checks failed.' }
} finally { Pop-Location }
