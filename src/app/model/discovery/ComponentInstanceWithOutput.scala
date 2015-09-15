package model.discovery

import scala.concurrent.Future

trait ComponentInstanceWithOutput extends ComponentInstance {

  def getOutputDataSample(state: State, dataSamples: Map[InputPort, DataSample]): Future[DataSample]

}
