package discovery.model

case class PortCheckResult(status: PortCheckResult.Status.Status, maybeState: Option[ComponentState] = None)

object PortCheckResult {

  object Status extends Enumeration {
    type Status = Value
    val Success, Failure = Value
  }

}