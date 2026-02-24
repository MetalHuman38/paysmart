import subprocess
import sys
from pathlib import Path
from typing import Sequence


REPO_ROOT = Path(__file__).resolve().parents[3]


def run(title: str, cmd: Sequence[str], cwd: Path = REPO_ROOT) -> None:
    print(f"\n[hook] {title}")
    print(" ".join(cmd))
    result = subprocess.run(cmd, cwd=cwd)
    if result.returncode != 0:
        print(f"[hook] {title} failed.")
        raise SystemExit(result.returncode)
    print(f"[hook] {title} passed.")


def gradle_command(*tasks: str) -> list[str]:
    wrapper = "gradlew.bat" if sys.platform.startswith("win") else "./gradlew"
    return [
        wrapper,
        *tasks,
        "--no-daemon",
        "--configuration-cache",
        "--console=plain",
    ]


def main() -> None:
    # Production-safe gate: lint + unit tests before push.
    run("Android lint (debug)", gradle_command(":app:lintDebug"))
    run("Android unit tests (debug)", gradle_command(":app:testDebugUnitTest"))


if __name__ == "__main__":
    main()
