# android/setup/setup_git_hooks.py
import os
from pathlib import Path

HOOKS = Path(".git/hooks")

def main():
    if not HOOKS.exists():
        print("❌ .git/hooks not found (not a Git repo?)")
        return

    sh = HOOKS / "pre-commit"
    bat = HOOKS / "pre-commit.bat"

    sh.write_text("""#!/bin/sh
echo "Running pre-commit..."
python android/tools/hooks/pre_commit.py
""")

    bat.write_text(r"""@echo off
echo Running pre-commit...
python android\tools\hooks\pre_commit.py
if %errorlevel% neq 0 exit /b %errorlevel%
""")

    os.chmod(sh, 0o755)

    print("✅ Installed pre-commit hook for Windows + Bash.")

if __name__ == "__main__":
    main()
