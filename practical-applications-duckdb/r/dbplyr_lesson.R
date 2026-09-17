# Practical Applications of DuckDB — R lesson, option 2: dbplyr (DBI)
#
# From the podcast: dbplyr (a tidyverse package) exposes dplyr's grammar over
# any DBI backend. DuckDB makes a *great* dbplyr backend because expressed vs
# executed — every dplyr verb is translated into SQL and duckdb executes it.
#
# NOTE: R is NOT part of this machine's stack. These scripts are reference
# material for learners who have R installed. Run from the course root:
#
#     Rscript r/dbplyr_lesson.R
#
# Prereqs (one time):  install.packages(c("duckdb", "dbplyr", "dplyr"))

library(duckdb)
library(dbplyr)
library(dplyr)

con <- dbConnect(duckdb(), dbdir = "data/analytics.duckdb")

# dbplyr translates this dplyr chain into SQL; DuckDB runs it lazily.
by_department <- tbl(con, "employees") %>%
  group_by(department) %>%
  summarise(avg_salary = mean(salary), n = n()) %>%
  arrange(desc(avg_salary))

show_query(by_department)  # inspect the generated SQL
collect(by_department)     # materialise

dbDisconnect(con, shutdown = TRUE)
cat("dbplyr lesson complete.\n")