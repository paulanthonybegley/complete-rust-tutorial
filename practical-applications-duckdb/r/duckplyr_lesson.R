# Practical Applications of DuckDB — R lesson, option 1: duckplyr
#
# From the podcast: duckplyr is DuckDB's dplyr-flavoured R interface (built
# with Posit). Instead of emitting SQL, it targets DuckDB's native API, so a
# dplyr data-pipeline runs as one optimised query plan. The teaching point is
# ergonomics: the same dplyr grammar you already know, now powered by DuckDB.
#
# NOTE: R is NOT part of this machine's stack. These scripts are reference
# material for learners who have R installed. Run them from the course root:
#
#     Rscript r/duckplyr_lesson.R
#
# Prereqs (one time):  install.packages(c("duckdb", "duckplyr", "dplyr"))

library(duckdb)
library(duckplyr)

# Connect straight to the same analytics.duckdb file the app/Docker use.
con <- dbConnect(duckdb(), dbdir = "data/analytics.duckdb")

# dplyr pipeline -> DuckDB executes it
top <- sales %>%
  mutate(revenue = quantity * unit_price) %>%
  group_by(product_id) %>%
  summarise(store_revenue = sum(revenue)) %>%
  arrange(desc(store_revenue)) %>%
  head(5)

print(top, n = 5)

# duckplyr lets you fall back to base data frames on purpose:
df <- as.data.frame(sales)

dbDisconnect(con, shutdown = TRUE)
cat("duckplyr lesson complete.\n")