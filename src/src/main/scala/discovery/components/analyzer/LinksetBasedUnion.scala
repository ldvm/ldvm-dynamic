package discovery.components.analyzer

import com.hp.hpl.jena.rdf.model.{Model, ModelFactory, Resource}
import discovery.model._
import discovery.model.components.AnalyzerInstance
import discovery.model.components.descriptor.{AskDescriptor, ConstructDescriptor, SelectDescriptor}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class LinksetBasedUnion extends AnalyzerInstance {

  case class LinksetBasedUnionState(subjectsClass: String, objectsClass: String) extends ComponentState

  val linksPortName = "LINKS"
  val dataSource1PortName = "DS1"
  val dataSource2PortName = "DS2"

  val linksPort = Port(linksPortName, 1)
  val source1Port = Port(dataSource1PortName, 2)
  val source2Port = Port(dataSource2PortName, 2)

  val linksDescriptor = SelectDescriptor(
    """
      |PREFIX void: <http://rdfs.org/ns/void#>
      |
      |SELECT ?subjectsClass ?objectsClass
      |WHERE {
      |    ?ls a void:Linkset ;
      |        void:subjectsTarget ?subjectsDs ;
      |        void:objectsTarget ?objectsDs .
      |    ?subjectsDs void:class ?subjectsClass .
      |    ?objectsDs void:class ?objectsClass .
      |}
      |
      """.stripMargin
  )

  def classDescriptor(isClass: String) = AskDescriptor(
    s"""
       |PREFIX ldvm: <http://linked.opendata.cz/ontology/ldvm/>
       |
       |ASK {
       |   ?x a <$isClass> .
       |}
       |
       |""".stripMargin
  )

  override val getInputPorts: Seq[Port] = Seq(linksPort, source1Port, source2Port)

  override def checkPort(port: Port, state: Option[ComponentState], outputDataSample: DataSample): Future[PortCheckResult] = port match {
    case Port(`linksPortName`, _) => checkLinks(state, outputDataSample)
    case Port(`dataSource1PortName`, _) => checkClassPresence(s => s.subjectsClass, state, outputDataSample)
    case Port(`dataSource2PortName`, _) => checkClassPresence(s => s.objectsClass, state, outputDataSample)
  }

  def checkLinks(state: Option[ComponentState], outputDataSample: DataSample): Future[PortCheckResult] = state match {
    case None => outputDataSample.executeSelect(linksDescriptor).map { resultSet =>
      resultSet.hasNext match {
        case false => PortCheckResult(PortCheckResult.Status.Failure)
        case true => {
          val result = resultSet.next()
          val subjects = result.get("subjectsClass").asResource().getURI
          val objects = result.get("objectsClass").asResource().getURI
          val newState = LinksetBasedUnionState(subjects, objects)
          PortCheckResult(PortCheckResult.Status.Success, Some(newState))
        }
      }
    }
    case _ => Future.successful(PortCheckResult(PortCheckResult.Status.Failure, state))
  }


  def checkClassPresence(classUriSelector: LinksetBasedUnionState => String, state: Option[ComponentState], outputDataSample: DataSample): Future[PortCheckResult] = state match {
    case None => Future.successful(PortCheckResult(PortCheckResult.Status.Failure, state))
    case Some(s) => {
      val classUri = classUriSelector(s.asInstanceOf[LinksetBasedUnionState])
      outputDataSample.executeAsk(classDescriptor(classUri)).map { bool =>
        PortCheckResult(bool, state)
      }
    }
  }

  private def getSampleInstance(dataSample: DataSample, linkClassUri: String): Future[Resource] = {
    dataSample.executeSelect(SelectDescriptor(
      s"""
         | SELECT ?x WHERE { ?x a <$linkClassUri> }
      """.stripMargin)).map { rs =>
      rs.hasNext match {
        case true => rs.next().getResource("x")
        case _ => throw new Exception
      }
    }
  }

  // TODO: remove ComponentState parameter
  override def getOutputDataSample(state: Option[ComponentState], dataSamples: Map[Port, DataSample]): Future[DataSample] = {
    state match {
      case None => Future.failed(new Exception)
      case Some(s) => generateOutputDataSample(s, dataSamples)
    }
  }

  private def generateOutputDataSample(componentState: ComponentState, dataSamples: Map[Port, DataSample]): Future[DataSample] = {

    val lbuState = componentState.asInstanceOf[LinksetBasedUnionState]
    val sample1 = getSampleInstance(dataSamples(source1Port), lbuState.subjectsClass)
    val sample2 = getSampleInstance(dataSamples(source2Port), lbuState.objectsClass)

    for {
      s1 <- sample1
      s2 <- sample2
      l <- generateLink(dataSamples, s1, s2)
    } yield enrichDataSampleWithLink(l, dataSamples)
  }

  private def enrichDataSampleWithLink(generatedLink: Model, dataSamples: Map[Port, DataSample]): ModelDataSample = {
    val result = unionSamples(dataSamples).add(generatedLink)
    ModelDataSample(result)
  }

  private def generateLink(dataSamples: Map[Port, DataSample], sample1: Resource, sample2: Resource): Future[Model] = {
    dataSamples(linksPort).executeConstruct(
      ConstructDescriptor(
        s"""
           | PREFIX void: <http://rdfs.org/ns/void#>
           |
           | CONSTRUCT {
           |   <${sample1.getURI}> ?p <${sample2.getURI}>
           | } WHERE {
           |   [] void:linkPredicate ?p .
           | }
           | """.stripMargin
      )
    )
  }

  private def unionSamples(dataSamples: Map[Port, DataSample]): Model = {
    val models = dataSamples.values.map(_.getModel)
    val result = ModelFactory.createDefaultModel()
    models.foreach(result.add)
    result
  }
}
