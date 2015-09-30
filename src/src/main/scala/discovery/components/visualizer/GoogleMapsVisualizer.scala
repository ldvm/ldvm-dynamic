package discovery.components.visualizer

import discovery.model.components.VisualizerInstance
import discovery.model.components.descriptor.{AskDescriptor, Descriptor}
import discovery.model.{ComponentState, DataSample, Port, PortCheckResult}

import scala.concurrent.Future

class GoogleMapsVisualizer extends VisualizerInstance {

  private val inputPort = Port("PORT1", 0)

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

  override def getInputPorts: Seq[Port] = Seq(inputPort)

  override def checkPort(port: Port, state: Option[ComponentState], outputDataSample: DataSample): Future[PortCheckResult] = {
    super.checkAskDescriptors(port, outputDataSample)
  }

  override def getDescriptorsByPort: Map[Port, Seq[Descriptor]] = Map(inputPort -> descriptors)
}
