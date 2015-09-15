package model.discovery

import scala.concurrent.Future

trait ComponentInstanceWithInputs extends ComponentInstance {

  def checkPort(port: InputPort, state: State, outputDataSample: DataSample) : Future[PortCheckResult]

  def getInputPorts : Future[Seq[InputPort]]

}
