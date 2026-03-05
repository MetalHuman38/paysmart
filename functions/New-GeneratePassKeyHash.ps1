# Paths to downloaded certs from Play Console
$UploadCertDer = "C:\Users\Metal\paysmart\functions\src\api\upload_cert.der"
$PlayCertDer   = "C:\Users\Metal\paysmart\functions\src\api\deployment_cert.der"

function Get-PasskeyHashFromDer([string]$DerPath) {
  if (!(Test-Path $DerPath)) { throw "Missing file: $DerPath" }
  $certBytes = [System.IO.File]::ReadAllBytes($DerPath)
  $sha = [System.Security.Cryptography.SHA256]::Create()
  $digest = $sha.ComputeHash($certBytes)
  [Convert]::ToBase64String($digest).TrimEnd('=').Replace('+','-').Replace('/','_')
}

$uploadHash = Get-PasskeyHashFromDer $UploadCertDer
$playHash   = Get-PasskeyHashFromDer $PlayCertDer
"PASSKEY_ANDROID_APK_KEY_HASHES=$uploadHash,$playHash"
