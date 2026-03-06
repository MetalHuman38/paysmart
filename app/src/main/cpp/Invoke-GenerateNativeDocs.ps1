[CmdletBinding()]
param(
    [string]$DoxygenCommand = "doxygen",
    [string]$ConfigPath = "$PSScriptRoot\Doxyfile"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

if (-not (Test-Path -LiteralPath $ConfigPath)) {
    throw "Doxygen config not found: $ConfigPath"
}

$doxygen = Get-Command $DoxygenCommand -ErrorAction SilentlyContinue
if (-not $doxygen) {
    throw "doxygen not found in PATH. Install Doxygen and retry."
}

$doxyText = Get-Content -Raw -LiteralPath $ConfigPath
$outputMatch = [regex]::Match($doxyText, '(?m)^\s*OUTPUT_DIRECTORY\s*=\s*(.+)\s*$')
if (-not $outputMatch.Success) {
    throw "OUTPUT_DIRECTORY is missing from Doxyfile"
}

$configuredOutput = $outputMatch.Groups[1].Value.Trim()
$publicRoot = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\..\..\..\public'))
$codedocsRoot = [System.IO.Path]::GetFullPath((Join-Path $publicRoot 'codedocs'))
$outputRoot = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot $configuredOutput))

if ($outputRoot -eq $publicRoot) {
    throw "Refusing generation: OUTPUT_DIRECTORY points to public root. Use public/codedocs instead."
}
if ($outputRoot -ne $codedocsRoot) {
    throw "Refusing generation: OUTPUT_DIRECTORY must resolve to $codedocsRoot but resolves to $outputRoot"
}

Push-Location $PSScriptRoot
try {
    & $DoxygenCommand $ConfigPath
    if ($LASTEXITCODE -ne 0) {
        throw "doxygen exited with code $LASTEXITCODE"
    }

    $generatedHtmlIndex = Join-Path $codedocsRoot 'index.html'
    if (Test-Path -LiteralPath $generatedHtmlIndex) {
        $redirectIndexPath = Join-Path $codedocsRoot '\index.html'
        $redirectHtml = @"
<!doctype html>
<html lang="en">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width,initial-scale=1" />
    <meta http-equiv="refresh" content="0; url=/codedocs/index.html" />
    <link rel="canonical" href="/codedocs/index.html" />
    <title>Redirecting to code docs...</title>
  </head>
  <body>
    <p>Redirecting to <a href="/codedocs/index.html">/codedocs/index.html</a>...</p>
  </body>
</html>
"@
        Set-Content -LiteralPath $redirectIndexPath -Value $redirectHtml -Encoding UTF8
        Write-Host "Native API docs generated at: $generatedHtmlIndex"
        Write-Host "Stable entry point: $redirectIndexPath"
    } else {
        Write-Host "Native API docs generated. Output root: $codedocsRoot"
    }
} finally {
    Pop-Location
}
