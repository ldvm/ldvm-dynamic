package model.discovery

case class PortMatch(port: Port, startPipeline: Pipeline, maybeState: Option[State])
