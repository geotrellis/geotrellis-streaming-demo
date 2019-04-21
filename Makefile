ASSEMBLY_VERSION := 0.0.1-SNAPSHOT
ASSEMBLY         := streaming/target/scala-2.11/geotrellis-streaming-demo-0.0.1-SNAPSHOT.jar

build: 
	./sbt "project streaming" assembly

clean:
	./sbt clean -no-colors

local-spark-demo:
	spark-submit \
        --master local[*] \
        --driver-memory 2G \
        --conf spark.scheduler.mode=FAIR \
        --conf spark.streaming.kafka.maxRatePerPartition=25 \
        --class com.granduke.Main \
        ${PWD}/${ASSEMBLY}

local-spark-shell:
	spark-shell \
        --master local[*] \
        --driver-memory 2G \
        --executor-memory 2G \
        --conf spark.scheduler.mode=FAIR \
        --conf spark.executor.cores=1 \
        --conf spark.streaming.kafka.maxRatePerPartition=25 \
        --jars ${PWD}/${ASSEMBLY}

kafka:
	docker-compose -f docker-compose.kafka.yml up

# service-example:
# 	docker-compose -f docker-compose.service.yml up

kafka-send-messages:
	./sbt "project producer" "run --generate-and-send"

sbt-spark-demo:
	./sbt "project streaming" "run"
