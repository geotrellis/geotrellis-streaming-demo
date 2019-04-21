import sbt._

object Dependencies {
  val geotrellisContrib   = "com.azavea.geotrellis" %% "geotrellis-contrib-gdal"    % Version.geotrellisContrib
  val circeCore           = "io.circe"              %% "circe-core"                 % Version.circe
  val circeGeneric        = "io.circe"              %% "circe-generic"              % Version.circe
  val circeGenericExtras  = "io.circe"              %% "circe-generic-extras"       % Version.circe
  val circeParser         = "io.circe"              %% "circe-parser"               % Version.circe
  val sparkCore           = "org.apache.spark"      %% "spark-core"                 % Version.spark
  val sparkStreaming      = "org.apache.spark"      %% "spark-streaming"            % Version.spark
  val sparkStreamingKafka = "org.apache.spark"      %% "spark-streaming-kafka-0-10" % Version.spark
  val scalaTest           = "org.scalatest"         %% "scalatest"                  % Version.scalaTest
  val decline             = "com.monovore"          %% "decline"                    % Version.decline
}
