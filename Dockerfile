FROM debian:bookworm

ADD . /srv/collector/temp

WORKDIR /srv/collector

RUN apt-get update && \
    apt-get install -y default-jdk ant ivy git curl && \
    cd temp && \
    git submodule init && \
    git submodule update && \
    cp -rf ant/* src/build/java/ && \
    ant fetch-metrics-lib && \
    ant -lib /usr/share/java resolve && \
    ant tar && \
    cp generated/dist/**dev.jar docker-entrypoint.sh .. && \
    cd .. && \
    rm -rf temp

ENTRYPOINT ["sh", "docker-entrypoint.sh"]
