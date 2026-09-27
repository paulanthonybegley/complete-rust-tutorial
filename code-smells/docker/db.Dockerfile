FROM postgres:16

COPY db/init.sql /docker-entrypoint-initdb.d/01-init.sql