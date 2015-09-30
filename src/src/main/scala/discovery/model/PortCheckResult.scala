package discovery.model

case class PortCheckResult(status: PortCheckResult.Status.Status, maybeState: Option[ComponentState] = None)

object PortCheckResult {

  def apply(success: Boolean) : PortCheckResult = {
    success match {
      case true => PortCheckResult(Status.Success)
      case false => PortCheckResult(Status.Failure)
    }
  }

  object Status extends Enumeration {
    type Status = Value
    val Success, Failure, Error = Value
  }

}
