package model.discovery

import scala.concurrent.Future

trait ComponentInstanceWithInputs extends ComponentInstance {

  def checkPort(port: Port, state: Option[State], outputDataSample: DataSample): Future[CheckResult]

  def getInputPorts : Seq[Port]

}
