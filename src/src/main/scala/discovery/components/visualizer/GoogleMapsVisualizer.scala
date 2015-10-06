package discovery.components.visualizer

import discovery.components.common.DescriptorChecker
import discovery.model.components.VisualizerInstance
import discovery.model.components.descriptor.{AskDescriptor, Descriptor}
import discovery.model.{ComponentState, DataSample, Port, PortCheckResult}

import scala.concurrent.Future

class GoogleMapsVisualizer extends VisualizerInstance with DescriptorChecker {
  val portName: String = "INPUT_PORT"

  private val descriptor = AskDescriptor(
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

  override val getInputPorts: Seq[Port] = Seq(Port(portName, 0))

  override def checkPort(port: Port, state: Option[ComponentState], outputDataSample: DataSample): Future[PortCheckResult] = {
    checkStatelessDescriptors(outputDataSample, descriptor)
  }
}
