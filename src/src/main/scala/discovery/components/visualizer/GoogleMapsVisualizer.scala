package discovery.components.visualizer

import discovery.model.components.VisualizerInstance
import discovery.model.components.descriptor.{AskDescriptor, Descriptor}
import discovery.model.{ComponentState, DataSample, Port, PortCheckResult}

import scala.concurrent.Future

class GoogleMapsVisualizer extends VisualizerInstance {
  val portName: String = "INPUT_PORT"

  private val descriptors = Seq(
    AskDescriptor(
      """
        |PREFIX s: <http://schema.org/>
        |
        |    ASK {
        |      ?something s:geo ?g .
        |      ?g a s:GeoCoordinates ;
        |        s:latitude ?lat ;
        |        s:longitude ?lng .
        |    }
      """.stripMargin
    )
  )

  override val getInputPorts: Seq[Port] = Seq(Port(portName, 0))

  override def checkPort(port: Port, state: Option[ComponentState], outputDataSample: DataSample): Future[PortCheckResult] = {
    super.checkAskDescriptors(port, outputDataSample)
  }

  override def descriptorsForPort(port: Port): Seq[Descriptor] = port match {
    case Port(`portName`, _) => descriptors
  }
}
