cd /srv/collector.torproject.org/collector/

#./create-tarballs.sh

service nginx restart

java -DLOGBASE=data/logs -jar collector-1.18.2-dev.jar
