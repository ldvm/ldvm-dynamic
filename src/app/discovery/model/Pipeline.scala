package discovery.model

import discovery.model.components.VisualizerInstance

// TODO: do we need components at all? We can always infer them from bindings
case class Pipeline(components: Seq[PipelineComponent], bindings: Seq[PortBinding], lastComponent: PipelineComponent, lastOutputDataSample: DataSample) {
  def isComplete : Boolean = lastComponent.componentInstance.isInstanceOf[VisualizerInstance]
}
