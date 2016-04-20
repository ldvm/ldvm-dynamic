package discovery.components.analyzer

import discovery.components.common.DescriptorChecker
import discovery.model._
import discovery.model.components.AnalyzerInstance
import discovery.model.components.descriptor.{AskDescriptor, ConstructDescriptor}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PopulatedPlacesAnalyzer extends AnalyzerInstance with DescriptorChecker {
  val portName: String = "INPUT_PORT"
  val port = Port(portName, 0)

  private val descriptor = AskDescriptor(
      """
      |PREFIX dbo: <http://dbpedia.org/ontology/>
      |PREFIX dbp: <http://dbpedia.org/property/>
      |
      |    ASK {
      |      ?p a dbo:PopulatedPlace ;
      |         dbo:populationTotal ?population ;
      |         dbp:officialName	?on .
      |    }
      """.stripMargin
  )

  override def getOutputDataSample(state: Option[ComponentState], dataSamples: Map[Port, DataSample]): Future[DataSample] = {
    dataSamples(port).executeConstruct(ConstructDescriptor(
      """
        |PREFIX dbo: <http://dbpedia.org/ontology/>
        |PREFIX dbp: <http://dbpedia.org/property/>
        |
        |    CONSTRUCT {
        |      ?p a dbo:PopulatedPlace ;
        |         dbo:populationTotal ?population ;
        |         dbp:officialName	?on .
        |    } WHERE {
        |      ?p a dbo:PopulatedPlace ;
        |         dbo:populationTotal ?population ;
        |         dbp:officialName	?on .
        |    }
      """.stripMargin)).map(ModelDataSample)
  }

  override def checkPort(port: Port, state: Option[ComponentState], outputDataSample: DataSample): Future[PortCheckResult] = {
    checkStatelessDescriptors(outputDataSample, descriptor)
  }

  override val getInputPorts: Seq[Port] = Seq(port)
}
