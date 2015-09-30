package discovery.components.datasource

import com.hp.hpl.jena.rdf.model.Model
import discovery.model._
import discovery.model.components.DataSourceInstance

import scala.concurrent.Future

class JenaDataSource(model: Model, name: String = "") extends DataSourceInstance {
  override def getOutputDataSample(state: Option[ComponentState], dataSamples: Map[Port, DataSample]): Future[DataSample] = {
    Future.successful(ModelDataSample(model))
  }

  override def toString: String = s"JenaDataSource($name)"
}
