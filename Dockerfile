FROM nginx

RUN apt update && apt install -y default-jdk ant ivy git curl

ADD . /srv/collector.torproject.org/collector/temp

WORKDIR /srv/collector.torproject.org/collector/temp

RUN git submodule init && git submodule update && cp -rf ant/* src/build/java/

RUN ant fetch-metrics-lib && ant -lib /usr/share/java resolve

RUN ant tar

RUN mkdir -p /var/www/collector/html

RUN mv -v src/main/resources/webapp/* /var/www/collector/html/

RUN mv -f src/main/resources/nginx-collector /etc/nginx/conf.d/default.conf

RUN ln -s /srv/collector.torproject.org/collector/indexed/recent/ /var/www/collector/html/recent

RUN ln -s /srv/collector.torproject.org/collector/indexed/archived/ /var/www/collector/html/archived

RUN ln -s /srv/collector.torproject.org/collector/indexed/index/ /var/www/collector/html/index

RUN cp generated/dist/**dev.jar docker-entrypoint.sh src/main/resources/create-tarballs.sh ..

RUN cd .. && rm -rf temp

EXPOSE 9000

ENTRYPOINT ["sh", "/srv/collector.torproject.org/collector/docker-entrypoint.sh"]
