package model.discovery

import model.discovery.Status.Status

case class CheckResult(status: Status, maybeState: Option[State] = None)