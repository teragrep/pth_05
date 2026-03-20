FROM rockylinux/rockylinux:9

COPY rpm/com.teragrep-pth_05-*.rpm /pth_05.rpm
RUN dnf install -y /pth_05.rpm && rm -f /pth_05.rpm && dnf clean all

ENV PTH_05_ENDPOINT="http://0.0.0.0:8080"
ENV PTH_05_CREDENTIALS_FILE="/opt/teragrep/pth_05/etc/credentials.json"
ENV PTH_05_AUTHORIZE_FILE="/opt/teragrep/pth_05/etc/authorize.json"
ENV PTH_05_LOOKUP_PATH="/opt/teragrep/pth_05/etc/lookup"

ENV PTH_05_PROVIDER="s3"
ENV PTH_05_PROVIDER_IDENTITY="default-identity"
ENV PTH_05_PROVIDER_CREDENTIAL="default-credential"
ENV PTH_05_PROVIDER_ENDPOINT="https://s3.default.tld:443"

COPY docker/docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh
USER srv-s3gw
ENTRYPOINT ["/docker-entrypoint.sh"]
