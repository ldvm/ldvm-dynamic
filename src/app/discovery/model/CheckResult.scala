package discovery.model

import discovery.model.Status.Status

case class CheckResult(status: Status, maybeState: Option[State] = None)