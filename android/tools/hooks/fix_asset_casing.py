# android/tools/hooks/fix_asset_casing.py
import os
import subprocess
import sys

RES_DIR = "app/src/main/res"

def has_upper(s: str) -> bool:
    return any(c.isupper() for c in s)

def git_mv(src, tmp, final):
    subprocess.run(["git", "mv", src, tmp], check=True)
    subprocess.run(["git", "mv", tmp, final], check=True)

def main():
    if not os.path.isdir(RES_DIR):
        print("📁 No res folder found")
        return

    renames = []

    for root, _, files in os.walk(RES_DIR):
        for f in files:
            if has_upper(f):
                src = os.path.join(root, f)
                dst = os.path.join(root, f.lower())
                tmp = dst + ".tmp"
                renames.append((src, tmp, dst))

    if not renames:
        print("✅ No uppercase resource filenames.")
        return

    print("⚠️ Uppercase resource files found:")

    for src, tmp, dst in renames:
        print(f" - {os.path.basename(src)} -> {os.path.basename(dst)}")
        git_mv(src, tmp, dst)

    print("\n❌ Renamed files — please re-stage before committing.")
    sys.exit(1)

if __name__ == "__main__":
    main()
