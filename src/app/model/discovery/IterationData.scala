package model.discovery

case class IterationData(givenPipelines: Seq[Pipeline], completedPipelines: Seq[Pipeline], possibleComponents: Seq[ComponentInstanceWithInputs])
