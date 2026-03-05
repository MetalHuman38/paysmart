param(
    [ValidateRange(16, 128)]
    [int]$Bytes = 32,
    [switch]$Copy
)

$buffer = New-Object byte[] $Bytes
[System.Security.Cryptography.RandomNumberGenerator]::Fill($buffer)

# 64-char hex by default when Bytes=32
$secret = [Convert]::ToHexString($buffer).ToLowerInvariant()

$secret

if ($Copy) {
    Set-Clipboard -Value $secret
    Write-Host "Copied to clipboard."
}