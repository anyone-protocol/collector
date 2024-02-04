cd /srv/collector.torproject.org/collector/

./create-tarball.sh

service nginx restart

java -jar collector-1.18.2-dev.jar
