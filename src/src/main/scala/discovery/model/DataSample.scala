package discovery.model

import com.hp.hpl.jena.query.{QueryExecutionFactory, QueryFactory, ResultSet}
import com.hp.hpl.jena.rdf.model.Model
import discovery.JenaUtil
import discovery.model.components.descriptor.{AskDescriptor, ConstructDescriptor, SelectDescriptor}

import scala.concurrent.Future

trait DataSample {

  def executeAsk(descriptor: AskDescriptor): Future[Boolean]

  def executeSelect(descriptor: SelectDescriptor): Future[ResultSet]

  def executeConstruct(descriptor: ConstructDescriptor): Future[Model]

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
}

case class RdfDataSample(rdfData: String) extends DataSample {
  private val innerModel = ModelDataSample(JenaUtil.modelFromTtl(rdfData))

  override def executeAsk(descriptor: AskDescriptor): Future[Boolean] = {
    innerModel.executeAsk(descriptor)
  }

  override def executeSelect(descriptor: SelectDescriptor): Future[ResultSet] = {
    innerModel.executeSelect(descriptor)
  }

  override def executeConstruct(descriptor: ConstructDescriptor): Future[Model] = {
    innerModel.executeConstruct(descriptor)
  }
}

case object EmptyDataSample extends DataSample {
  override def executeAsk(query: AskDescriptor): Future[Boolean] = Future.successful(false)

  override def executeConstruct(query: ConstructDescriptor): Future[Model] = ???

  override def executeSelect(query: SelectDescriptor): Future[ResultSet] = ???
}
