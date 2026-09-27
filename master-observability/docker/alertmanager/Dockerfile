FROM prom/alertmanager:v0.28.0

COPY alertmanager.yml /etc/alertmanager/alertmanager.yml
CMD ["--config.file=/etc/alertmanager/alertmanager.yml", "--log.level=debug"]