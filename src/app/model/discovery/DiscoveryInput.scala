package model.discovery

case class DiscoveryInput(dataSources: Seq[ComponentInstanceWithOutput], visualizers: Seq[ComponentInstanceWithInputs], processors: Seq[ProcessorInstance])
