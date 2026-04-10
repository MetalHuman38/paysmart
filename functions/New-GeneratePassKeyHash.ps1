param(
  [string]$ApkPath = "",
  [string]$DebugKeystore = "$env:USERPROFILE\.android\debug.keystore",
  [string]$DebugAlias = "androiddebugkey",
  [string]$DebugStorePassword = "android",
  [string]$DebugKeyPassword = "android",
  [string]$ReleaseKeystore = "",
  [string]$ReleaseAlias = "",
  [string]$ReleaseStorePassword = "",
  [string]$ReleaseKeyPassword = "",
  [string]$PlaySigningCertDer = "",
  [string]$UploadCertDer = ""
)

$ErrorActionPreference = "Stop"

function Normalize-Base64UrlNoPadding([byte[]]$Bytes) {
  [Convert]::ToBase64String($Bytes).TrimEnd('=').Replace('+', '-').Replace('/', '_')
}

function Get-Sha256Bytes([byte[]]$Bytes) {
  $sha256 = [System.Security.Cryptography.SHA256]::Create()
  try {
    return $sha256.ComputeHash($Bytes)
  } finally {
    $sha256.Dispose()
  }
}

function Convert-HexToBytes([string]$Hex) {
  $compact = ($Hex -replace "[:\s]", "").Trim()
  if ([string]::IsNullOrWhiteSpace($compact)) {
    throw "Fingerprint is empty."
  }
  if ($compact.Length % 2 -ne 0) {
    throw "Fingerprint hex must have an even number of characters."
  }

  $bytes = New-Object byte[] ($compact.Length / 2)
  for ($i = 0; $i -lt $compact.Length; $i += 2) {
    $bytes[$i / 2] = [Convert]::ToByte($compact.Substring($i, 2), 16)
  }
  return $bytes
}

function Resolve-KeytoolPath {
  if ($env:JAVA_HOME) {
    $candidate = Join-Path $env:JAVA_HOME "bin\keytool.exe"
    if (Test-Path $candidate) {
      return $candidate
    }
  }

  $fromPath = Get-Command keytool.exe -ErrorAction SilentlyContinue
  if ($fromPath) {
    return $fromPath.Source
  }

  throw "Unable to find keytool.exe. Set JAVA_HOME or add keytool to PATH."
}

function Try-ReadSdkDirFromLocalProperties {
  $repoRoot = Split-Path -Parent $PSScriptRoot
  $localPropertiesPath = Join-Path $repoRoot "local.properties"
  if (!(Test-Path $localPropertiesPath)) {
    return ""
  }

  $line = Get-Content $localPropertiesPath | Where-Object { $_ -match '^sdk\.dir=' } | Select-Object -First 1
  if (-not $line) {
    return ""
  }

  $raw = ($line -split '=', 2)[1].Trim()
  if ([string]::IsNullOrWhiteSpace($raw)) {
    return ""
  }

  return ($raw -replace '\\:', ':' -replace '\\\\', '\')
}

function Resolve-ApkSignerPath {
  $sdkRoots = @(
    $env:ANDROID_SDK_ROOT,
    $env:ANDROID_HOME,
    (Try-ReadSdkDirFromLocalProperties)
  ) | Where-Object { -not [string]::IsNullOrWhiteSpace($_) } | Select-Object -Unique

  foreach ($sdkRoot in $sdkRoots) {
    $buildToolsDir = Join-Path $sdkRoot "build-tools"
    if (!(Test-Path $buildToolsDir)) {
      continue
    }

    $candidate = Get-ChildItem -Path $buildToolsDir -Directory |
      Sort-Object Name -Descending |
      ForEach-Object { Join-Path $_.FullName "apksigner.bat" } |
      Where-Object { Test-Path $_ } |
      Select-Object -First 1

    if ($candidate) {
      return $candidate
    }
  }

  throw "Unable to find apksigner.bat. Set ANDROID_SDK_ROOT/ANDROID_HOME or ensure local.properties has sdk.dir."
}

function Get-PasskeyHashFromDer([string]$DerPath) {
  if (!(Test-Path $DerPath)) {
    throw "Missing DER certificate: $DerPath"
  }

  $certBytes = [System.IO.File]::ReadAllBytes((Resolve-Path $DerPath))
  $sha = Get-Sha256Bytes $certBytes
  return Normalize-Base64UrlNoPadding $sha
}

function Get-PasskeyHashFromKeystore(
  [string]$KeystorePath,
  [string]$Alias,
  [string]$StorePassword,
  [string]$KeyPassword
) {
  if (!(Test-Path $KeystorePath)) {
    throw "Missing keystore: $KeystorePath"
  }
  if ([string]::IsNullOrWhiteSpace($Alias)) {
    throw "Keystore alias is required for $KeystorePath"
  }

  $keytool = Resolve-KeytoolPath
  $arguments = @(
    "-list",
    "-v",
    "-alias", $Alias,
    "-keystore", (Resolve-Path $KeystorePath),
    "-storepass", $StorePassword
  )

  if (-not [string]::IsNullOrWhiteSpace($KeyPassword)) {
    $arguments += @("-keypass", $KeyPassword)
  }

  $output = & $keytool @arguments 2>&1
  if ($LASTEXITCODE -ne 0) {
    throw "keytool failed for $KeystorePath`n$output"
  }

  $match = [regex]::Match(($output -join [Environment]::NewLine), "(?m)^\s*SHA256:\s*([0-9A-F:]+)\s*$")
  if (-not $match.Success) {
    throw "Could not find SHA256 fingerprint in keytool output for $KeystorePath"
  }

  $fingerprintBytes = Convert-HexToBytes $match.Groups[1].Value
  return Normalize-Base64UrlNoPadding $fingerprintBytes
}

function Get-PasskeyHashFromApk([string]$ResolvedApkPath) {
  if (!(Test-Path $ResolvedApkPath)) {
    throw "Missing APK: $ResolvedApkPath"
  }

  $apksigner = Resolve-ApkSignerPath
  $output = & $apksigner verify --print-certs $ResolvedApkPath 2>&1
  if ($LASTEXITCODE -ne 0) {
    throw "apksigner failed for $ResolvedApkPath`n$output"
  }

  $match = [regex]::Match(($output -join [Environment]::NewLine), "(?m)^\s*Signer #1 certificate SHA-256 digest:\s*([0-9A-F:]+)\s*$")
  if (-not $match.Success) {
    throw "Could not find APK signer SHA-256 digest in apksigner output for $ResolvedApkPath"
  }

  $fingerprintBytes = Convert-HexToBytes $match.Groups[1].Value
  return Normalize-Base64UrlNoPadding $fingerprintBytes
}

function Try-ReadReleaseKeystoreFromLocalProperties {
  $repoRoot = Split-Path -Parent $PSScriptRoot
  $localPropertiesPath = Join-Path $repoRoot "local.properties"
  if (!(Test-Path $localPropertiesPath)) {
    return ""
  }

  $line = Get-Content $localPropertiesPath | Where-Object { $_ -match '^RELEASE_STORE_FILE=' } | Select-Object -First 1
  if (-not $line) {
    return ""
  }

  $raw = ($line -split '=', 2)[1].Trim()
  if ([string]::IsNullOrWhiteSpace($raw)) {
    return ""
  }

  return ($raw -replace '\\:', ':' -replace '\\\\', '\')
}

if ([string]::IsNullOrWhiteSpace($ReleaseKeystore)) {
  $ReleaseKeystore = Try-ReadReleaseKeystoreFromLocalProperties
}

if ([string]::IsNullOrWhiteSpace($ApkPath)) {
  $repoRoot = Split-Path -Parent $PSScriptRoot
  $candidate = Join-Path $repoRoot "app\build\outputs\apk\debug\app-debug.apk"
  if (Test-Path $candidate) {
    $ApkPath = $candidate
  }
}

if ([string]::IsNullOrWhiteSpace($PlaySigningCertDer)) {
  $candidate = Join-Path $PSScriptRoot "src\api\deployment_cert.der"
  if (Test-Path $candidate) {
    $PlaySigningCertDer = $candidate
  }
}

if ([string]::IsNullOrWhiteSpace($UploadCertDer)) {
  $candidate = Join-Path $PSScriptRoot "src\api\upload_cert.der"
  if (Test-Path $candidate) {
    $UploadCertDer = $candidate
  }
}

$results = New-Object System.Collections.Generic.List[object]

if (-not [string]::IsNullOrWhiteSpace($ApkPath) -and (Test-Path $ApkPath)) {
  $resolvedApkPath = (Resolve-Path $ApkPath).Path
  $results.Add([pscustomobject]@{
    Label = "apk_signer"
    Hash = Get-PasskeyHashFromApk $resolvedApkPath
    Source = $resolvedApkPath
    Usage = "Authoritative hash for the APK file you are about to install or already installed."
  })
}

if (Test-Path $DebugKeystore) {
  $results.Add([pscustomobject]@{
    Label = "debug_keystore"
    Hash = Get-PasskeyHashFromKeystore `
      -KeystorePath $DebugKeystore `
      -Alias $DebugAlias `
      -StorePassword $DebugStorePassword `
      -KeyPassword $DebugKeyPassword
    Source = $DebugKeystore
    Usage = "Use for locally installed debug APKs."
  })
}

if (-not [string]::IsNullOrWhiteSpace($ReleaseKeystore) -and (Test-Path $ReleaseKeystore) -and -not [string]::IsNullOrWhiteSpace($ReleaseAlias)) {
  $results.Add([pscustomobject]@{
    Label = "local_release_keystore"
    Hash = Get-PasskeyHashFromKeystore `
      -KeystorePath $ReleaseKeystore `
      -Alias $ReleaseAlias `
      -StorePassword $ReleaseStorePassword `
      -KeyPassword $ReleaseKeyPassword
    Source = $ReleaseKeystore
    Usage = "Use for locally installed release APKs signed with your own release key."
  })
}

if (-not [string]::IsNullOrWhiteSpace($PlaySigningCertDer) -and (Test-Path $PlaySigningCertDer)) {
  $results.Add([pscustomobject]@{
    Label = "play_app_signing_cert"
    Hash = Get-PasskeyHashFromDer $PlaySigningCertDer
    Source = $PlaySigningCertDer
    Usage = "Use for Play-distributed installs. This is usually the production passkey origin."
  })
}

if (-not [string]::IsNullOrWhiteSpace($UploadCertDer) -and (Test-Path $UploadCertDer)) {
  $results.Add([pscustomobject]@{
    Label = "upload_cert"
    Hash = Get-PasskeyHashFromDer $UploadCertDer
    Source = $UploadCertDer
    Usage = "Only use if an installed APK was actually signed with the upload cert."
  })
}

if ($results.Count -eq 0) {
  throw "No usable keystore/certificate sources were found."
}

Write-Host ""
Write-Host "Canonical passkey hashes (Base64URL, no padding):"
Write-Host ""

foreach ($result in $results) {
  Write-Host ("[{0}] {1}" -f $result.Label, $result.Hash)
  Write-Host ("  source: {0}" -f $result.Source)
  Write-Host ("  usage:  {0}" -f $result.Usage)
  Write-Host ("  origin: android:apk-key-hash:{0}" -f $result.Hash)
  Write-Host ""
}

$allHashes = ($results | Select-Object -ExpandProperty Hash) -join ","
Write-Host ("PASSKEY_ANDROID_APK_KEY_HASHES={0}" -f $allHashes)
