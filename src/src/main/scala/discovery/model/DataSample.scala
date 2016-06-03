package discovery.model

import com.hp.hpl.jena.query.{QueryExecutionFactory, QueryFactory, ResultSet}
import com.hp.hpl.jena.rdf.model.{Model, ModelFactory}
import com.hp.hpl.jena.update.UpdateAction
import discovery.JenaUtil
import discovery.model.components.UpdateQuery
import discovery.model.components.descriptor.{AskDescriptor, ConstructDescriptor, SelectDescriptor}

import scala.concurrent.Future

trait DataSample {

  def executeAsk(descriptor: AskDescriptor): Future[Boolean]

  def executeSelect(descriptor: SelectDescriptor): Future[ResultSet]

  def executeConstruct(descriptor: ConstructDescriptor): Future[Model]

  def getModel: Model

  def transform(query: UpdateQuery): Model = {
    val resultModel = ModelFactory.createDefaultModel()
    resultModel.add(getModel)
    UpdateAction.parseExecute(query.query, resultModel)
    resultModel
  }
}

case class ModelDataSample(model: Model) extends DataSample {
  override def executeAsk(descriptor: AskDescriptor): Future[Boolean] = {
    Future.successful {
      val query = QueryFactory.create(descriptor.query)
      val execution = QueryExecutionFactory.create(query, model)
      execution.execAsk()
    }
  }

  override def executeConstruct(descriptor: ConstructDescriptor): Future[Model] = {
    Future.successful {
      val query = QueryFactory.create(descriptor.query)
      val execution = QueryExecutionFactory.create(query, model)
      execution.execConstruct()
    }
  }

  override def executeSelect(descriptor: SelectDescriptor): Future[ResultSet] =
    Future.successful {
      val query = QueryFactory.create(descriptor.query)
      val execution = QueryExecutionFactory.create(query, model)
      execution.execSelect()
    }

  override def getModel: Model = model
}

case class RdfDataSample(rdfData: String) extends DataSample {
  private val model = JenaUtil.modelFromTtl(rdfData)
  private val modelSample = ModelDataSample(model)

  override def executeAsk(descriptor: AskDescriptor): Future[Boolean] = {
    modelSample.executeAsk(descriptor)
  }

  override def executeSelect(descriptor: SelectDescriptor): Future[ResultSet] = {
    modelSample.executeSelect(descriptor)
  }

  override def executeConstruct(descriptor: ConstructDescriptor): Future[Model] = {
    modelSample.executeConstruct(descriptor)
  }

  override def getModel: Model = model
}

case object EmptyDataSample extends DataSample {
  override def executeAsk(query: AskDescriptor): Future[Boolean] = Future.successful(false)

  override def executeConstruct(query: ConstructDescriptor): Future[Model] = ???

  override def executeSelect(query: SelectDescriptor): Future[ResultSet] = ???

  override def getModel: Model = ???
}
