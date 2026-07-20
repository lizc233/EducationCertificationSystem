$ErrorActionPreference = 'Stop'

Set-Location $PSScriptRoot

$logFile = Join-Path $PSScriptRoot 'spring-boot.console.log'
$bootstrapLogFile = Join-Path $PSScriptRoot 'run-dev.bootstrap.log'

if (Test-Path $logFile) {
    Remove-Item -LiteralPath $logFile -Force
}

if (Test-Path $bootstrapLogFile) {
    Remove-Item -LiteralPath $bootstrapLogFile -Force
}

try {
    "[$(Get-Date -Format s)] Starting backend from $PSScriptRoot" | Out-File -FilePath $bootstrapLogFile -Encoding utf8
    $mvn = (Get-Command mvn.cmd -ErrorAction Stop).Source
    "[$(Get-Date -Format s)] Using Maven command: $mvn" | Out-File -FilePath $bootstrapLogFile -Encoding utf8 -Append
    & $mvn spring-boot:run *>> $logFile
}
catch {
    "[$(Get-Date -Format s)] Startup failed: $($_.Exception.Message)" | Out-File -FilePath $bootstrapLogFile -Encoding utf8 -Append
    throw
}
