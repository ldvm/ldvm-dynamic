package discovery.model

import discovery.model.components.{DataSourceInstance, ExtractorInstance, ProcessorInstance, VisualizerInstance}

case class DiscoveryInput(
  dataSources: Seq[DataSourceInstance],
  extractors: Seq[ExtractorInstance],
  visualizers: Seq[VisualizerInstance],
  processors: Seq[ProcessorInstance]
)
