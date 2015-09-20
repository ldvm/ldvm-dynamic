package discovery.components

import com.hp.hpl.jena.rdf.model.Model
import discovery.model.components.DataSourceInstance
import discovery.model.{DataSample, Port, ComponentState}

import scala.concurrent.Future

class JenaDataSource(model: Model) extends DataSourceInstance {
  override def getOutputDataSample(state: Option[ComponentState], dataSamples: Map[Port, DataSample]): Future[DataSample] =  Future.successful(DataSample())
}
