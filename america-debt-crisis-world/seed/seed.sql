-- =====================================================================
-- America's Debt Crisis in the World — seed dataset
-- (idempotent, deterministic)
--
-- Single source of truth for the course database, executed by:
--   * scripts/seed_db.py  (local notebook setup)
--   * the Docker "duckdb" CLI container on first start (seed/seed.sql)
--
-- The dataset is a stylised, deterministic reconstruction of the numbers in
-- the documentary "America's Debt Crisis" (CFS, 2026): the $40T crossing,
-- the $3B/day interest bill, the "333" plan targets, the 30-year yield spike
-- (5.3%, the most expensive 30-year sale since 2001) and the r-v "breathing
-- room" crossover. Values are anchors from the video plus deterministic
-- hash-based variation, so every run produces the SAME answers.
--
-- Tables:
--   gdp                      nominal GDP by year (billions, 1995-2026)
--   debt                     national debt by year (billions) + held-by-public %GDP
--   interest                 net interest by year (billions) + %GDP
--   interest_outlook         CBO-style interest outlook 2026-2036 (>$16T decade)
--   deficit                  receipts / outlays / deficit %GDP + primary surplus
--   spending                 outlays by category (five big items ~78% of all)
--   thirty_year_yields       average 30-year Treasury yield by year
--   monthly_30y_yield_2025   the Aug-2025 spike month by month
--   energy                   crude + natural gas (oil-equivalent) by year
--   growth_quarters          real GDP growth by quarter (the 3% target context)
-- =====================================================================

-- ---------------------------------------------------------------
-- Nominal GDP 1995-2026 (billions). Roughly +5%/yr with deterministic noise.
-- Anchors: ~7,700 (1995), ~30,000 (2026) so debt/GDP in 2026 ≈ 133% (gross).
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS gdp (
    year               INTEGER PRIMARY KEY,
    nominal_gdp_billions DOUBLE
);

INSERT INTO gdp (year, nominal_gdp_billions)
SELECT
    1995 + CAST(i AS INTEGER),
    ROUND(7700.0 * pow(1.0485, i) *
          (1.0 + 0.002 * (MOD(abs(i * 2654435761), 21) - 10) / 10.0), 0)
FROM range(32) AS t(i)
WHERE (SELECT count(*) FROM gdp) = 0;

-- ---------------------------------------------------------------
-- National debt 1995-2026 (billions). Anchors: ~10,000 (2006), ~20,000 (2016),
-- ~40,000 (2026) — the "doubled in 10 years, quadrupled in 20" claim.
-- (2^(1/10))^(year-2006):  x2 at 2016, x4 at 2026.
-- held_by_public_pct_gdp ~105% in 2026 (the video's ">100%" line).
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS debt (
    year                     INTEGER PRIMARY KEY,
    debt_total_billions      DOUBLE,
    debt_held_public_pct_gdp DOUBLE
);

INSERT INTO debt (year, debt_total_billions, debt_held_public_pct_gdp)
SELECT
    year,
    debt_total_billions,
    ROUND(100.0 * debt_total_billions
          / (SELECT nominal_gdp_billions FROM gdp g WHERE g.year = d.year)
          * 0.86, 1)
FROM (
    SELECT y AS year,
           ROUND(10000.0 * pow(pow(2, 1.0 / 10), y - 2006.0)
                 * (1.0 + 0.015 * (MOD(abs(y * 2654435761), 17) - 8) / 10.0), 0) AS debt_total_billions
    FROM (SELECT 1995 + CAST(i AS INTEGER) AS y, i FROM range(32) AS t(i)) s
) d
WHERE (SELECT count(*) FROM debt) = 0;

-- ---------------------------------------------------------------
-- Net interest 1995-2026 (billions) and % of GDP.
-- 2026 => ~3.2% of GDP (~$3 billion/day) — the video's carrying-cost story.
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS interest (
    year                 INTEGER PRIMARY KEY,
    net_interest_billions DOUBLE,
    interest_pct_gdp     DOUBLE
);

INSERT INTO interest (year, net_interest_billions, interest_pct_gdp)
SELECT
    g.year,
    ROUND(g.nominal_gdp_billions * pct / 100.0, 0),
    ROUND(pct, 2)
FROM gdp g
JOIN (
    SELECT 1995 + CAST(i AS INTEGER) AS year,
           1.8 + 0.045 * i AS pct
    FROM range(32) AS t(i)
) r ON r.year = g.year
WHERE (SELECT count(*) FROM interest) = 0;

-- ---------------------------------------------------------------
-- Interest outlook 2026-2036 — the >$16 trillion decade.
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS interest_outlook (
    fiscal_year          INTEGER PRIMARY KEY,
    net_interest_billions DOUBLE
);

INSERT INTO interest_outlook (fiscal_year, net_interest_billions)
SELECT fiscal_year, net_interest_billions FROM (VALUES
    (2026,  960),
    (2027,  1120),
    (2028,  1295),
    (2029,  1475),
    (2030,  1665),
    (2031,  1855),
    (2032,  2040),
    (2033,  2215),
    (2034,  2385),
    (2035,  2540),
    (2036,  2685)
) AS o(fiscal_year, net_interest_billions)
WHERE (SELECT count(*) FROM interest_outlook) = 0;

-- ---------------------------------------------------------------
-- Federal budget 1977-2026: receipts/outlays, deficit %GDP, primary surplus.
-- 2026: receipts ~5,431B, outlays ~7,500B => ~6.2% of GDP deficit; primary
-- (ex-interest) deficit ≈ 3.0% of GDP, the Bessent "3%" target.
-- Exactly 12 primary-surplus years in the 50 the notebook counts (the
-- "12 times in 50 years" line).
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS deficit (
    fiscal_year             INTEGER PRIMARY KEY,
    receipts_billions       DOUBLE,
    outlays_billions        DOUBLE,
    deficit_pct_gdp         DOUBLE,
    primary_surplus_billions DOUBLE,
    primary_surplus_flag    INTEGER
);

INSERT INTO deficit (fiscal_year, receipts_billions, outlays_billions,
                     deficit_pct_gdp, primary_surplus_billions, primary_surplus_flag)
SELECT
    fiscal_year,
    receipts_billions,
    outlays_billions,
    deficit_pct_gdp,
    primary_surplus_billions,
    CASE WHEN fiscal_year IN
        (1998, 1999, 2000, 2001, 2015, 1979, 1980, 1988, 1989, 2007, 2016, 1977)
        THEN 1 ELSE 0 END AS primary_surplus_flag
FROM (
    SELECT
        1977 + CAST(i AS INTEGER) AS fiscal_year,
        ROUND(500.0 + 168.0 * i - 0.35 * i * i
              + 25.0 * (MOD(abs(i * 2654435761), 20) - 10), 0) AS receipts_billions,
        ROUND(600.0 + 230.0 * i
              + 40.0 * (MOD(abs(i * 3 * 2654435761), 20) - 10), 0) AS outlays_billions,
        0.0 AS deficit_pct_gdp,
        0.0 AS primary_surplus_billions
    FROM range(50) AS t(i)
) s
WHERE (SELECT count(*) FROM deficit) = 0;

-- fix the 2026 anchors so the numbers match the video exactly
UPDATE deficit
SET receipts_billions = 5431, outlays_billions = 7500
WHERE fiscal_year = 2026;

-- recompute %GDP (against the 2026 GDP ~ 30,000B) and primary surplus
-- primary surplus = receipts - (outlays - interest), i.e. the budget *after*
-- the cash interest bill is stripped out. 2026 => 5431 - (7500 - 1068) = -1001B.
UPDATE deficit d
SET deficit_pct_gdp        = ROUND(100.0 * (outlays_billions - receipts_billions)
                                 / (SELECT nominal_gdp_billions FROM gdp g WHERE g.year = d.fiscal_year), 1),
    primary_surplus_billions = ROUND(receipts_billions - (outlays_billions -
        (SELECT i.net_interest_billions FROM interest i WHERE i.year = d.fiscal_year)), 0)
WHERE fiscal_year >= 1995;

UPDATE deficit d
SET primary_surplus_billions = CASE
    WHEN d.primary_surplus_flag = 1
        THEN ABS(outlays_billions - receipts_billions) + 120
    ELSE receipts_billions - (outlays_billions -
        COALESCE((SELECT i.net_interest_billions FROM interest i WHERE i.year = d.fiscal_year), 0))
    END;

-- ---------------------------------------------------------------
-- Outlays by category (2000-2026). The five big items are ~78% of everything
-- (2026) — the "spending cuts are theatrics" lesson.
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS spending (
    fiscal_year     INTEGER,
    category        VARCHAR,
    amount_billions DOUBLE,
    PRIMARY KEY (fiscal_year, category)
);

INSERT INTO spending (fiscal_year, category, amount_billions)
SELECT fiscal_year, category, amount_billions FROM (VALUES
    (2000, 'Social Security', 410), (2000, 'Medicare',  197), (2000, 'Net interest', 223),
    (2000, 'Health', 154), (2000, 'National defense', 295), (2000, 'All other', 409),
    (2005, 'Social Security', 523), (2005, 'Medicare',  299), (2005, 'Net interest', 184),
    (2005, 'Health', 250), (2005, 'National defense', 495), (2005, 'All other', 571),
    (2010, 'Social Security', 706), (2010, 'Medicare',  452), (2010, 'Net interest', 196),
    (2010, 'Health', 371), (2010, 'National defense', 693), (2010, 'All other', 655),
    (2015, 'Social Security', 882), (2015, 'Medicare',  634), (2015, 'Net interest', 223),
    (2015, 'Health', 512), (2015, 'National defense', 585), (2015, 'All other', 790),
    (2020, 'Social Security', 1090), (2020, 'Medicare',  776), (2020, 'Net interest', 345),
    (2020, 'Health', 862), (2020, 'National defense', 714), (2020, 'All other', 2219),
    (2026, 'Social Security', 1700), (2026, 'Medicare',  960), (2026, 'Net interest', 960),
    (2026, 'Health', 1210), (2026, 'National defense', 1020), (2026, 'All other', 1650)
) AS s(fiscal_year, category, amount_billions)
WHERE (SELECT count(*) FROM spending) = 0;

-- ---------------------------------------------------------------
-- 30-year Treasury yield by year (average). 2026 ≈ 5.3% — the most expensive
-- 30-year sale since 2001.
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS thirty_year_yields (
    year        INTEGER PRIMARY KEY,
    avg_30y_pct DOUBLE
);

INSERT INTO thirty_year_yields (year, avg_30y_pct)
SELECT year, avg_30y_pct FROM (VALUES
    (1995, 6.88), (1996, 6.71), (1997, 6.61), (1998, 5.58), (1999, 5.87), (2000, 5.81),
    (2001, 5.49), (2002, 5.41), (2003, 4.99), (2004, 4.85), (2005, 4.51), (2006, 4.59),
    (2007, 4.83), (2008, 4.28), (2009, 4.08), (2010, 4.25), (2011, 3.91), (2012, 2.92),
    (2013, 3.45), (2014, 3.34), (2015, 2.88), (2016, 2.60), (2017, 2.90), (2018, 3.11),
    (2019, 2.39), (2020, 1.56), (2021, 2.11), (2022, 3.07), (2023, 3.95), (2024, 4.42),
    (2025, 4.68), (2026, 5.30)
) AS y(year, avg_30y_pct)
WHERE (SELECT count(*) FROM thirty_year_yields) = 0;

-- ---------------------------------------------------------------
-- 2025 month-by-month 30-year yield: the Aug-18 spike (highest in 19 years)
-- that triggered the buyback announcement and the Druckenmiller editorial.
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS monthly_30y_yield_2025 (
    month      VARCHAR PRIMARY KEY,
    avg_30y_pct DOUBLE
);

INSERT INTO monthly_30y_yield_2025 (month, avg_30y_pct)
SELECT month, avg_30y_pct FROM (VALUES
    ('Jan', 4.70), ('Feb', 4.52), ('Mar', 4.61), ('Apr', 4.78),
    ('May', 4.55), ('Jun', 4.40), ('Jul', 4.86), ('Aug', 5.18),
    ('Sep', 4.98), ('Oct', 4.72), ('Nov', 4.58), ('Dec', 4.64)
) AS m(month, avg_30y_pct)
WHERE (SELECT count(*) FROM monthly_30y_yield_2025) = 0;

-- ---------------------------------------------------------------
-- Energy: crude output + natural gas as oil-equivalent (million barrels/day).
-- The 333-plan target is a *combined* 3M boe/day fresh over ~2025-2028.
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS energy (
    year                INTEGER PRIMARY KEY,
    crude_mbpd          DOUBLE,
    natural_gas_mboed   DOUBLE,
    total_boe_mbpd      DOUBLE
);

INSERT INTO energy (year, crude_mbpd, natural_gas_mboed, total_boe_mbpd)
SELECT year, crude_mbpd, natural_gas_mboed, crude_mbpd + natural_gas_mboed FROM (VALUES
    (2017, 9.35,  2.20, 0), (2018, 10.96, 2.58, 0), (2019, 12.23, 2.90, 0),
    (2020, 11.28, 3.05, 0), (2021, 11.25, 3.12, 0), (2022, 11.91, 3.54, 0),
    (2023, 12.93, 3.89, 0), (2024, 13.21, 4.05, 0), (2025, 13.55, 4.22, 0),
    (2026, 13.67, 4.45, 0), (2027, 13.72, 4.70, 0), (2028, 13.85, 4.85, 0)
) AS e(year, crude_mbpd, natural_gas_mboed, total_boe_mbpd)
WHERE (SELECT count(*) FROM energy) = 0;

UPDATE energy SET total_boe_mbpd = ROUND(crude_mbpd + natural_gas_mboed, 2);

-- ---------------------------------------------------------------
-- Real GDP growth by quarter (annualised). 2026 H1 = (2.1 + 1.5)/2 ≈ 1.8
-- against the 3% target.
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS growth_quarters (
    quarter                 VARCHAR PRIMARY KEY,
    real_gdp_growth_pct     DOUBLE
);

INSERT INTO growth_quarters (quarter, real_gdp_growth_pct)
SELECT quarter, real_gdp_growth_pct FROM (VALUES
    ('2023Q1', 2.2), ('2023Q2', 2.1), ('2023Q3', 4.9), ('2023Q4', 3.4),
    ('2024Q1', 3.4), ('2024Q2', 3.0), ('2024Q3', 2.8), ('2024Q4', 2.4),
    ('2025Q1', 1.4), ('2025Q2', 1.6), ('2025Q3', 2.0), ('2025Q4', 1.8),
    ('2026Q1', 2.1), ('2026Q2', 1.5)
) AS q(quarter, real_gdp_growth_pct)
WHERE (SELECT count(*) FROM growth_quarters) = 0;