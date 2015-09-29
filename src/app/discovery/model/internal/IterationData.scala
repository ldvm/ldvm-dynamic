package discovery.model.internal

import discovery.model.{Pipeline, PartialPipeline, CompletePipeline}
import discovery.model.components.ComponentInstanceWithInputs

case class IterationData(givenPipelines: Seq[PartialPipeline], completedPipelines: Seq[CompletePipeline], possibleComponents: Seq[ComponentInstanceWithInputs], iterationNumber:Int)
