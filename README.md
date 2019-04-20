# GeoTrellis Streaming demo

This is a project that contains a rendering raster process via `GeoTrellis`. 
The input data is a `Kafka` stream, the output - a set of rasters and some json metadata outputs.

- [Environment](#environment)
- [Makefile commands](#makefile-commands)
- [application.conf description](#applicationconf-description) (a pretty important section, as here Azure credentials should be injected)
- [log4j.properties description](#log4jproperties-description) (enabling fluentd logging)
- [Usage Example in SBT shell](#usage-example-in-sbt-shell)
- [Usage example](#usage-example)
  - [Kafka in Docker usage](#kafka-in-docker-usage)

### Environment

To run this app in any `Spark` mode, be sure that you have a proper installed `Spark` client on your machine.
To rise a `Kafka` instance it's enough to run `make kafka` command (more detailed information about kafka in docker is provided in [Kafka in docker](#kafka-in-docker-usage) section).

Be sure that all neccesary changes were introduced into the `application.conf` file.

### Makefile commands

Makefile is provided to simplify launch and integration tests of the application.

| Command                 | Description
|-------------------------|--------------------------------------------------------------------------|
|local-spark-demo         |Run a spark streaming assembly on a local Spark server                    |
|local-spark-shell        |Run a spark shell with included fat jar locally                           |
|build                    |Build a fat jar to run on Spark                                           |
|clean                    |Clean up targets                                                          |
|kafka                    |Run a dockerized kafka, see README.md to know more about it               |
|service-example          |Run a dockerized version of the whole app example                         |
|kafka-send-messages      |Produce demo kafka messages                                               |

### application.conf description

Application settings provided via configuration file in the resources folder. (P.S. [application.conf.template](https://github.com/pomadchin/geotrellis-streaming-demo/blob/master/app/src/main/resources/application.conf.template) is a template to fill in.)

```conf

```

## Usage Example in SBT shell

1. For instance we already have `Kafka` running localy on 9092 port.
  1.1 If not, it is possible to launch [Kafka in docker](#kafka-in-docker-usage)
2. Open two projects `producer` and `streaming` in two separate terminal windows
3. `run` a streaming application (`project streaming`, `run`)
4. `run` a procuder application (`project producer`, `run --generate-and-send`)

To summarise: 

Terminal №1:

```bash
$ cd app; ./sbt
$ project streaming
$ run
```

Terminal №2:

```bash
$ cd app; ./sbt
$ project producer
$ run --generate-and-send
```

## Usage Example

1. For instance we already have `Kafka` running localy on 9092 port.
  1.1 If not, it is possible to launch [Kafka in docker](#kafka-in-docker-usage)
2. Build a fat assembly jar: `make build`
3. Launch a `Spark` app: `make local-spark-processing`
4. Post a test kafka message: `make kafka-send-messages`

To summarise: 

```bash
$ make build && make local-spark-processing
$ make kafka-send-messages
```

### Kafka in Docker usage

1. Add into the `/etc/hosts` file the following alias: ```127.0.0.1       localhost kafka``` (definitely a working variant of a Mac OS setup: ```127.0.0.1 localhost.localdomain localhost kafka ```)
2. Run `make kafka`
