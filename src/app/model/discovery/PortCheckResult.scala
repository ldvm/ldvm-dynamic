package model.discovery

import model.discovery.Status.Status

case class PortCheckResult(state: State, status: Status)