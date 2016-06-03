package discovery.components.visualizer

import discovery.components.common.DescriptorChecker
import discovery.model._
import discovery.model.components.VisualizerInstance
import discovery.model.components.descriptor.AskDescriptor

import scala.concurrent.Future

class PopulationVisualizer extends VisualizerInstance with DescriptorChecker {
  val portName = "INPUT"

  private val descriptor = AskDescriptor(
    """
      |PREFIX s: <http://schema.org/>
      |PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
      |
      |    ASK {
      |      ?p rdf:Value ?populationCount ;
      |         s:name ?placeName ;
      |         s:geo [
      |           s:latitude ?lat ;
      |           s:longitude ?lng
      |         ]
      |      .
      |    }
    """.stripMargin
  )

  override def checkPort(port: Port, state: Option[ComponentState], outputDataSample: DataSample): Future[PortCheckResult] = {
    checkStatelessDescriptors(outputDataSample, descriptor)
  }

  override def getInputPorts: Seq[Port] = Seq(Port(portName, 0))
}
