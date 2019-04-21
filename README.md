# GeoTrellis Streaming demo

This is a project that contains a rendering raster process via `GeoTrellis`. 
The input data is a `Kafka` stream, the output - a set of rasters and some json metadata outputs.

The project contains two subprojects: `streaming` (the actual streaming application) and 
`producer` (subproject used for test purposes, that generates test kafka messages).

- [Environment](#environment)
- [Makefile commands](#makefile-commands)
- [application.conf description](#applicationconf-description)
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
|kafka-send-messages      |Produce demo kafka messages                                               |
|sbt-spark-demo           |Run a spark streaming application from the SBT shell                      |

### application.conf description

Application settings provided via configuration file in the resources folder (`streaming`).

```conf
ingest.stream {
  # kafka setting
  kafka {
    threads           = 10
    topic             = "geotrellis-streaming"
    otopic            = "geotrellis-streaming-output"
    application-id    = "geotrellis-streaming"
    bootstrap-servers = "localhost:9092"
  }
  # spark streaming settings
  spark {
    batch-duration    = 10 // in seconds
    partitions        = 10
    auto-offset-reset = "latest"
    auto-commit       = true
    publish-to-kafka  = true
    group-id          = "spark-streaming-data"
    checkpoint-dir    = ""
  }
}

# geotrellis gdal VLM settings
vlm {
  geotiff.s3 {
    allow-global-read: false
    region: "us-west-2"
  }

  gdal.options {
    GDAL_DISABLE_READDIR_ON_OPEN = "YES"
    CPL_VSIL_CURL_ALLOWED_EXTENSIONS = ".tif"
  }

  # if true then uses GDALRasterSources, if false GeoTiffRasterSources
  source.gdal.enabled = true
}
```

Application settings provided via configuration file in the resources folder (`producer`).

```conf
lc8 {
  scenes = [
    {
      name = "LC08_L1TP_139044_20170304_20170316_01_T1" # name of the LC8 scene
      band = "1" # band number
      count = 2 # number of generated polygons
      crs = "EPSG:4326" # desired generated CRS
      output-path = "../data/img" # the output path where the result output should be placed after processing
    },
    {
      name = "LC08_L1TP_139045_20170304_20170316_01_T1"
      band = "2"
      count = 2
      crs = "EPSG:4326"
      output-path = "../data/img"
    },
    {
      name = "LC08_L1TP_139046_20170304_20170316_01_T1"
      band = "2"
      count = 2
      crs = "EPSG:4326"
      output-path = "../data/img"
    }
  ]
}
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
$ make kafka
```

Terminal №2:

```bash
$ cd app; ./sbt
$ project streaming
$ run
```

or

```bash
$ make sbt-spark-demo
```

Terminal №3:

```bash
$ ./sbt
$ project producer
$ run --generate-and-send
```

or

```bash
$ make kafka-send-messages
```

Extra summary:

```bash
# terminal 1
make kafka
# terminal 2
make sbt-spark-demo
# terminal 3
make kafka-send-messages
```

## Usage Example

1. For instance we already have `Kafka` running localy on 9092 port. If not, it is possible to launch [Kafka in docker](#kafka-in-docker-usage)
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
