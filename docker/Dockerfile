FROM centos:7

RUN yum -y install java-11-openjdk \
    && yum clean all

COPY rpm/target/rpm/com.teragrep-pth_05/RPMS/x86_64/com.teragrep-pth_05-*.rpm /pth_05.rpm
RUN yum install -y /pth_05.rpm && rm -f /pth_05.rpm && dnf clean all

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

ENTRYPOINT ["/docker-entrypoint.sh"]
