package discovery.components.analyzer

import discovery.model._
import discovery.model.components.AnalyzerInstance
import discovery.model.components.descriptor.Descriptor

import scala.concurrent.Future

class ClassBasedLinker extends AnalyzerInstance {
  val linksPortName = "LINKS"
  val dataSource1PortName = "DS1"
  val dataSource2PortName = "DS2"

  override val getInputPorts: Seq[Port] = Seq(
    Port(linksPortName, 1),
    Port(dataSource1PortName, 2),
    Port(dataSource2PortName, 2)
  )

  override def checkPort(port: Port, state: Option[ComponentState], outputDataSample: DataSample): Future[PortCheckResult] = port match {
    case Port(`linksPortName`, _) => ???
    case Port(`dataSource1PortName`, _) => ???
    case Port(`dataSource2PortName`, _) => ???
  }

  override def getOutputDataSample(state: Option[ComponentState], dataSamples: Map[Port, DataSample]): Future[DataSample] = ???



}
