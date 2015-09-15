package model.discovery

case class DiscoveryInput(dataSources: Seq[DataSourceInstance], visualizers: Seq[VisualizerInstance], processors: Seq[ProcessorInstance])
