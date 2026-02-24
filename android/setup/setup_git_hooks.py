import os
import subprocess
from pathlib import Path


PRE_COMMIT_SH = """#!/bin/sh
set -e
echo "Running pre-commit..."
python android/tools/hooks/pre_commit.py
"""

PRE_COMMIT_BAT = r"""@echo off
echo Running pre-commit...
python android\tools\hooks\pre_commit.py
if %errorlevel% neq 0 exit /b %errorlevel%
"""

PRE_PUSH_SH = """#!/bin/sh
set -e
echo "Running pre-push..."
python android/tools/hooks/pre_push.py
"""

PRE_PUSH_BAT = r"""@echo off
echo Running pre-push...
python android\tools\hooks\pre_push.py
if %errorlevel% neq 0 exit /b %errorlevel%
"""


def write_hook(name: str, sh_content: str, bat_content: str):
    hooks_dir = resolve_hooks_dir()
    sh_path = hooks_dir / name
    bat_path = hooks_dir / f"{name}.bat"
    sh_path.write_text(sh_content)
    bat_path.write_text(bat_content)
    os.chmod(sh_path, 0o755)


def resolve_hooks_dir() -> Path:
    script_dir = Path(__file__).resolve().parent
    default_root = script_dir.parents[1]

    try:
        top_level = subprocess.run(
            ["git", "rev-parse", "--show-toplevel"],
            cwd=default_root,
            capture_output=True,
            text=True,
            check=True,
        ).stdout.strip()
        repo_root = Path(top_level)
    except subprocess.CalledProcessError:
        repo_root = default_root

    return repo_root / ".git" / "hooks"


def main():
    hooks_dir = resolve_hooks_dir()
    if not hooks_dir.exists():
        print(".git/hooks not found (not a Git repo?)")
        return

    write_hook("pre-commit", PRE_COMMIT_SH, PRE_COMMIT_BAT)
    write_hook("pre-push", PRE_PUSH_SH, PRE_PUSH_BAT)

    print("Installed pre-commit and pre-push hooks for Windows and Bash.")

if __name__ == "__main__":
    main()
