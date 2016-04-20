package discovery.components.analyzer

import java.io.{ByteArrayInputStream, StringWriter}

import com.hp.hpl.jena.query.{QueryExecutionFactory, QueryFactory}
import com.hp.hpl.jena.rdf.model.{Model, ModelFactory, Resource}
import discovery.model._
import discovery.model.components.AnalyzerInstance
import discovery.model.components.descriptor.{AskDescriptor, ConstructDescriptor, SelectDescriptor}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ClassBasedLinker extends AnalyzerInstance {
  val linksPortName = "LINKS"
  val dataSource1PortName = "DS1"
  val dataSource2PortName = "DS2"

  val linksPort = Port(linksPortName, 1)
  val source1Port = Port(dataSource1PortName, 2)
  val source2Port = Port(dataSource2PortName, 2)

  val linksDescriptor = ConstructDescriptor(
    """
      |PREFIX ldvm: <http://linked.opendata.cz/ontology/ldvm/>
      |
      |CONSTRUCT {
      |   [] ldvm:sourceClass ?x .
      |   [] ldvm:targetClass ?y .
      |} WHERE {
      |   [] ldvm:sourceClass ?x .
      |   [] ldvm:targetClass ?y .
      |   FILTER NOT EXISTS { [] a ?x }
      |   FILTER NOT EXISTS { [] a ?y }
      |   FILTER NOT EXISTS { [] a ?y ; a ?x }
      |}
      |
      | """.stripMargin
  )

  def classSelect =
    s"""
       | PREFIX ldvm: <http://linked.opendata.cz/ontology/ldvm/>
       | SELECT ?sourceClass ?targetClass WHERE { [] ldvm:sourceClass ?sourceClass . [] ldvm:targetClass ?targetClass . }
    """.stripMargin

  def classDescriptor(isClass: String, isNotClass: String) = AskDescriptor(
    s"""
       |PREFIX ldvm: <http://linked.opendata.cz/ontology/ldvm/>
       |
       |ASK {
       |   ?x a <$isClass> .
       |   FILTER NOT EXISTS { [] a <$isClass> ; a <$isNotClass> . }
       |   FILTER NOT EXISTS { [] ldvm:sourceClass <$isClass> . }
       |   FILTER NOT EXISTS { [] ldvm:targetClass <$isClass> . }
       |}
       |
       |""".stripMargin
  )

  override val getInputPorts: Seq[Port] = Seq(linksPort, source1Port, source2Port)

  override def checkPort(port: Port, state: Option[ComponentState], outputDataSample: DataSample): Future[PortCheckResult] = port match {
    case Port(`linksPortName`, _) => checkLinks(state, outputDataSample)
    case Port(`dataSource1PortName`, _) => checkClassPresence("sourceClass", state, outputDataSample)
    case Port(`dataSource2PortName`, _) => checkClassPresence("targetClass", state, outputDataSample)
  }

  def checkLinks(state: Option[ComponentState], outputDataSample: DataSample): Future[PortCheckResult] = state match {
    case None => outputDataSample.executeConstruct(linksDescriptor).map { dataModel =>
      dataModel.isEmpty match {
        case true => PortCheckResult(PortCheckResult.Status.Failure, state)
        case false => {
          val sw = new StringWriter()
          dataModel.write(sw)

          PortCheckResult(PortCheckResult.Status.Success, Some(ComponentState(sw.toString)))
        }
      }
    }
    case _ => Future.successful(PortCheckResult(PortCheckResult.Status.Failure))
  }

  def checkClassPresence(classType: String, state: Option[ComponentState], outputDataSample: DataSample): Future[PortCheckResult] = state match {
    case None => Future.successful(PortCheckResult(PortCheckResult.Status.Failure, state))
    case Some(s) => {
      val (source, target) = getLinkClasses(s) match {
        case (so, t) if classType == "sourceClass" => (so, t)
        case (so, t) if classType == "targetClass" => (t, so)
      }
      outputDataSample.executeAsk(classDescriptor(source.getURI, target.getURI)).map { bool =>
        PortCheckResult(bool, state)
      }
    }
  }

  private def getModelFromState(state: ComponentState): Model = {
    val model = ModelFactory.createDefaultModel()
    model.read(new ByteArrayInputStream(state.rdfData.getBytes()), null)
    model
  }

  private def getLinkClasses(state: ComponentState): (Resource, Resource) = {
    val model = getModelFromState(state)
    val query = QueryFactory.create(SelectDescriptor(classSelect).query)
    val execution = QueryExecutionFactory.create(query, model)
    val resultSet = execution.execSelect()

    resultSet.hasNext match {
      case true => {
        val solution = resultSet.nextSolution()
        (solution.getResource("sourceClass"), solution.getResource("targetClass"))
      }
      case false => throw new Exception
    }

  }

  private def getSampleInstance(dataSample: DataSample, linkClass: Resource): Future[Resource] = {
    dataSample.executeSelect(SelectDescriptor(
      s"""
         | SELECT ?x WHERE { ?x a <${linkClass.getURI}> }
      """.stripMargin)).map { rs =>
      rs.hasNext match {
        case true => rs.next().getResource("x")
        case _ => throw new Exception
      }
    }
  }

  override def getOutputDataSample(state: Option[ComponentState], dataSamples: Map[Port, DataSample]): Future[DataSample] = {
    state match {
      case None => Future.failed(new Exception)
      case Some(s) => generateOutputDataSample(s, dataSamples)
    }
  }

  private def generateOutputDataSample(componentState: ComponentState, dataSamples: Map[Port, DataSample]): Future[DataSample] = {
    val (source, target) = getLinkClasses(componentState)

    val sample1 = getSampleInstance(dataSamples(source1Port), source)
    val sample2 = getSampleInstance(dataSamples(source2Port), target)

    for {
      s1 <- sample1
      s2 <- sample2
      l <- generateLink(dataSamples, s1, s2)
      ds <- enrichDataSampleWithLink(l, dataSamples)
    } yield ds
  }

  private def enrichDataSampleWithLink(generatedLink: Model, dataSamples: Map[Port, DataSample]): Future[ModelDataSample] = {
    val result = unionSamples(dataSamples).map { unionedModel =>
      unionedModel.add(generatedLink)
    }

    result.map(ModelDataSample(_))
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

  private def unionSamples(dataSamples: Map[Port, DataSample]): Future[Model] = {
    val eventuallyModels = dataSamples.values.map(_.executeConstruct(ConstructDescriptor.getAll))
    Future.sequence(eventuallyModels).map { models =>
      models.reduce((modelA, modelB) => modelA.add(modelB))
    }
  }
}
