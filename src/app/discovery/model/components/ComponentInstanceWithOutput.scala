package discovery.model.components

import discovery.model.{DataSample, Port, ComponentState}

import scala.concurrent.Future

trait ComponentInstanceWithOutput extends ComponentInstance {

  def getOutputDataSample(state: Option[ComponentState], dataSamples: Map[Port, DataSample]): Future[DataSample]

}
