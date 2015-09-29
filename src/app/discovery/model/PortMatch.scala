package discovery.model

case class PortMatch(port: Port, startPipeline: PartialPipeline, maybeState: Option[ComponentState])
