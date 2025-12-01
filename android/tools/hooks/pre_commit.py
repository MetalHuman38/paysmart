# android/tools/hooks/pre_commit.py
import subprocess
import sys

def run(title, cmd):
    print(f"\n🔍 {title}...")
    result = subprocess.run(cmd, shell=True)

    if result.returncode != 0:
        print(f"❌ {title} failed.")
        sys.exit(result.returncode)

    print(f"✅ {title} passed.")

def main():
    # 1. Fix resource casing
    run("Fix asset casing", "python android/tools/hooks/fix_asset_casing.py")

if __name__ == "__main__":
    main()
