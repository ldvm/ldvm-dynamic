package discovery.model

import discovery.model.components.VisualizerInstance

case class Pipeline(components: Seq[PipelineComponent], bindings: Seq[Binding], lastComponent: PipelineComponent, lastOutputDataSample: DataSample) {
  def isComplete : Boolean = components.lastOption.exists(_.componentInstance.isInstanceOf[VisualizerInstance])

}
