import subprocess
import sys
from pathlib import Path
from typing import Iterable, Sequence


REPO_ROOT = Path(__file__).resolve().parents[3]
HOOKS_DIR = Path(__file__).resolve().parent


def run(title: str, cmd: Sequence[str], cwd: Path = REPO_ROOT) -> None:
    print(f"\n[hook] {title}")
    print(" ".join(cmd))
    result = subprocess.run(cmd, cwd=cwd)
    if result.returncode != 0:
        print(f"[hook] {title} failed.")
        raise SystemExit(result.returncode)
    print(f"[hook] {title} passed.")


def git_output(args: Sequence[str]) -> str:
    result = subprocess.run(
        ["git", *args],
        cwd=REPO_ROOT,
        capture_output=True,
        text=True,
        check=True,
    )
    return result.stdout


def staged_files() -> list[str]:
    output = git_output(
        ["diff", "--cached", "--name-only", "--diff-filter=ACMR"]
    )
    return [
        line.strip().replace("\\", "/")
        for line in output.splitlines()
        if line.strip()
    ]


def has_android_changes(files: Iterable[str]) -> bool:
    prefixes = (
        "app/",
        "android/",
        "gradle/",
    )
    exact_files = {
        "build.gradle",
        "build.gradle.kts",
        "settings.gradle",
        "settings.gradle.kts",
        "gradle.properties",
        "gradlew",
        "gradlew.bat",
    }

    for path in files:
        if path in exact_files:
            return True
        if path.startswith(prefixes):
            return True
    return False


def gradle_command(*tasks: str) -> list[str]:
    wrapper = "gradlew.bat" if sys.platform.startswith("win") else "./gradlew"
    return [
        wrapper,
        *tasks,
        "--no-daemon",
        "--configuration-cache",
        "--console=plain",
    ]


def run_fast_checks(files: list[str]) -> None:
    run(
        "Fix Android resource casing",
        [sys.executable, str(HOOKS_DIR / "fix_asset_casing.py")],
    )

    if not has_android_changes(files):
        print(
            "[hook] No Android changes in staged files. "
            "Skipping Gradle lint."
        )
        return

    run(
        "Android lint (debug)",
        gradle_command(":app:lintDebug"),
    )


def main() -> None:
    files = staged_files()
    if not files:
        print("[hook] No staged files. Skipping pre-commit checks.")
        return

    run_fast_checks(files)


if __name__ == "__main__":
    main()
