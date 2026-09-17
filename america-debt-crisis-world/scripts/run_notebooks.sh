#!/usr/bin/env bash
# Execute every course notebook headlessly and fail loudly on any error.
# Uses the same local .venv the learners run `jupyter` from.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
VENV="${VENV:-$ROOT/.venv}"
PY="$VENV/bin/python"

if [ ! -f "$ROOT/data/analytics.duckdb" ]; then
    "$PY" "$ROOT/scripts/seed_db.py"
fi

"$PY" - <<PY
from pathlib import Path
import nbformat
from nbclient import NotebookClient
import sys

root = Path("$ROOT")
course = root / "notebooks" / "course"
failed = []
for nb_path in sorted(course.glob("*.ipynb")):
    nb = nbformat.read(nb_path, as_version=4)
    client = NotebookClient(nb, timeout=600, kernel_name="python3",
                            resources={"metadata": {"path": str(course)}})
    try:
        client.execute()
        print(f"OK    {nb_path.name}")
    except Exception as exc:
        failed.append((nb_path.name, exc))
        print(f"FAIL  {nb_path.name}: {exc}")
if failed:
    for name, exc in failed:
        print("[FAILED]", name, "→", exc)
    sys.exit(1)
print(f"all notebooks executed ({len(list(course.glob('*.ipynb')))})")
PY