mkdir -p data/htdocs

java -DLOGBASE=${LOGBASE} -jar collector-*.jar
