package discovery.model.components

import discovery.model._

import scala.concurrent.Future


trait ComponentInstanceWithInputs extends ComponentInstance {

  def checkPort(port: Port, state: Option[ComponentState], outputDataSample: DataSample): Future[PortCheckResult]

  def getInputPorts : Seq[Port]
}
