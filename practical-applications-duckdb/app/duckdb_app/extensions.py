"""Extensions — the podcast's "it is extensible and you only pay when you need".

  * spatial (core extension, opt-in): geometry type + ST_ functions. Works on
    every platform and is the lesson's primary demo.
  * h3 (community extension, from the community extension repository):
    geospatial indexing. Whether it installs depends on platform/version —
    the course treats install failure as a teaching point (extensions are
    versioned and platform-specific; try it inside the Docker linux image).

Installing an extension needs one network fetch the first time; afterwards it
is cached locally. Both installs are therefore attempted lazily and never
fail an endpoint — a missing extension returns an explicit message instead.
"""

import duckdb

from duckdb_app.wrangle import records


def _load(con: duckdb.DuckDBPyConnection, name: str) -> tuple[bool, str]:
    try:
        con.execute(f"INSTALL {name}")
        con.execute(f"LOAD {name}")
        return True, "loaded"
    except Exception as exc:  # network offline, community repo gap, ...
        return False, f"not available on this platform/version: {str(exc)[:140]}"


def spatial_demo(con: duckdb.DuckDBPyConnection) -> dict:
    ok, note = _load(con, "spatial")
    if not ok:
        return {"extension": "spatial", "ok": ok, "note": note}
    geo = records(con.sql(
        """
        SELECT
            a.name                                          AS place,
            a.kind,
            ST_Point(a.lon, a.lat)                          AS geom,
            ROUND(ST_Distance(ST_Point(a.lon, a.lat),
                              ST_Point(b.lon, b.lat))::DOUBLE, 3) AS km_to_hq
        FROM places a
        CROSS JOIN (SELECT * FROM places WHERE name = 'Head Office') b
        ORDER BY km_to_hq
        """
    ).df())
    return {
        "extension": "spatial",
        "ok": ok,
        "note": note,
        "points_and_distances_km": geo,
    }


def h3_demo(con: duckdb.DuckDBPyConnection) -> dict:
    ok, note = _load(con, "h3")
    if not ok:
        return {"extension": "h3", "ok": ok, "note": note}
    cells = records(con.sql(
        """
        SELECT name, h3_latlng_to_cell(lat, lon, 7) AS cell_at_res7
        FROM places
        """
    ).df())
    return {"extension": "h3", "ok": ok, "note": note, "cells": cells}