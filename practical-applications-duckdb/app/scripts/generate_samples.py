#!/usr/bin/env python3
"""Generate the course's sample wrangle files into data/files/.

  * fitbit.csv        — a deliberately messy CSV (US dates, blank cells,
                        quoted fields) for the CSV-sniffer lesson. This is
                        the shape of Simon's 70,000-file Fitbit export.
  * followers.jsonl   — a nested social-graph export (users + follows[]
                        arrays) for the JSON/unnest lesson. This is the shape
                        of Ned's Twitter-API feeds.

The hive-partitioned parquet folder (sales_hive/) is generated at runtime by
POST /wrangle/hive so it always matches the seeded data; this script writes
only the raw-text samples.

Run:  python scripts/generate_samples.py
"""

import argparse
import json
import random
import sys
from pathlib import Path


def fitbit_csv(dest: Path, rows: int = 200) -> None:
    rng = random.Random(2026)
    lines = ["Date,Time,Heart Rate (bpm),Steps,Calories"]
    for i in range(rows):
        month = rng.randint(1, 12)
        day = rng.randint(1, 28)
        hour = rng.randint(0, 23)
        minute = rng.randint(0, 59)
        hr = rng.randint(52, 178) if rng.random() > 0.12 else ""  # sometimes blank
        steps = rng.choice(['0', '"12,431"', '"8,214"', '"10,030"'])  # quoted commas
        cal = rng.randint(1400, 2600) if hr != "" else ""
        lines.append(
            f"{month:02d}/{day:02d}/2026,{hour:02d}:{minute:02d},{hr},{steps},{cal}"
        )
    dest.write_text("\n".join(lines) + "\n")


def followers_jsonl(dest: Path, users: int = 60) -> None:
    rng = random.Random(4242)
    with dest.open("w") as fh:
        for uid in range(1, users + 1):
            follows = [
                {"user_id": target, "since": f"2024-{rng.randint(1,12):02d}-01"}
                for target in rng.sample(
                    [u for u in range(1, users + 1) if u != uid],
                    rng.randint(0, 5),
                )
            ]
            record = {
                "user_id": uid,
                "name": f"person{uid}",
                "follows": follows,
            }
            fh.write(json.dumps(record) + "\n")


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--out",
        type=Path,
        default=Path(__file__).resolve().parents[2] / "data" / "files",
        help="output directory (default: course data/files)",
    )
    args = parser.parse_args()
    args.out.mkdir(parents=True, exist_ok=True)

    fitbit_csv(args.out / "fitbit.csv")
    followers_jsonl(args.out / "followers.jsonl")

    print(f"wrote {args.out / 'fitbit.csv'}")
    print(f"wrote {args.out / 'followers.jsonl'}")


if __name__ == "__main__":
    sys.exit(main())