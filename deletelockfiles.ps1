Write-Host "Killing emulator processes..."
Get-Process emulator*,qemu* -ErrorAction SilentlyContinue | Stop-Process -Force

Write-Host "Cleaning TEMP..."
Remove-Item "$env:TEMP\*" -Recurse -Force -ErrorAction SilentlyContinue

Write-Host "Removing AVD lock files..."
Get-ChildItem "$env:USERPROFILE\.android\avd" -Recurse -Directory -Filter "*.lock" -ErrorAction SilentlyContinue |
Remove-Item -Recurse -Force

Write-Host "Done. You can now restart Android Studio."
