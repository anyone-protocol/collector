FROM nginx

RUN apt update && apt install -y wget apt-transport-https gnupg && mkdir -p /etc/apt/keyrings

RUN wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | tee /etc/apt/keyrings/adoptium.asc

RUN echo "deb [signed-by=/etc/apt/keyrings/adoptium.asc] https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | tee /etc/apt/sources.list.d/adoptium.list

RUN apt update && apt install -y temurin-17-jdk ant ivy git

ADD . /srv/collector.torproject.org/collector/temp

WORKDIR /srv/collector.torproject.org/collector/temp

RUN git submodule init && git submodule update

RUN ant fetch-metrics-lib && ant -lib /usr/share/java resolve

RUN ant tar

RUN mkdir -p /var/www/collector/html && mv src/main/webapp/* /var/www/collector/html/

RUN mv -f ./src/main/resources/nginx-collector /etc/nginx/conf.d/default.conf

RUN cp generated/dist/**dev.jar docker-entrypoint.sh src/main/resources/create-tarballs.sh ..

RUN ln -s /srv/collector.torproject.org/collector/indexed/recent/ /var/www/collector/html/recent

RUN ln -s /srv/collector.torproject.org/collector/indexed/index/ /var/www/collector/html/index

RUN ln -s /srv/collector.torproject.org/collector/indexed/archive/ /var/www/collector/html/archive

RUN rm -rf /srv/collector.torproject.org/collector/temp

EXPOSE 9000

ENTRYPOINT ["sh", "/srv/collector.torproject.org/collector/docker-entrypoint.sh"]
