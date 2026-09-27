FROM grafana/grafana-oss:11.5.2

COPY provisioning/datasources /etc/grafana/provisioning/datasources
COPY provisioning/dashboards.yml /etc/grafana/provisioning/dashboards/dashboards.yml
COPY provisioning/dashboards /var/lib/grafana/dashboards

ENV GF_SECURITY_ADMIN_USER=admin \
    GF_SECURITY_ADMIN_PASSWORD=admin
ENV GF_AUTH_ANONYMOUS_ENABLED=true \
    GF_AUTH_ANONYMOUS_ORG_ROLE=Viewer