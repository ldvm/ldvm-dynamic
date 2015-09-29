package discovery

import java.util.concurrent.atomic.AtomicInteger

import discovery.model._
import discovery.model.components.{ComponentInstance, DataSourceInstance, ProcessorInstance, VisualizerInstance}

import scala.concurrent.{ExecutionContext, Future}

class PipelineBuilder(implicit executor: ExecutionContext) {
  val pipelineComponentCounter: AtomicInteger = new AtomicInteger()

  def buildPipeline(componentInstance: ComponentInstance, portMatches: Seq[PortMatch], discoveryIteration: Int): Future[Pipeline] = {
    val newLastComponent = newComponent(componentInstance, discoveryIteration)
    val dataSamples = portMatches.map(portMatch => portMatch.port -> portMatch.startPipeline.lastOutputDataSample).toMap
    val eventuallyDataSample: Future[DataSample] = dataSample(componentInstance, portMatches)
    eventuallyDataSample.map { dataSample =>
      Pipeline(
        pipelineComponents(portMatches, newLastComponent),
        pipelineBindings(portMatches, newLastComponent),
        newLastComponent,
        dataSample)
    }
  }

  def buildInitialPipeline(dataSource: DataSourceInstance): Future[Pipeline] = {
    val pipelineComponent = newComponent(dataSource, 0)
    dataSource.getOutputDataSample(state = None, dataSamples = Map()).map { outputDataSample =>
      Pipeline(
        Seq(pipelineComponent),
        Seq(),
        pipelineComponent,
        outputDataSample)
    }
  }

  private def newComponent(componentInstance: ComponentInstance, discoveryIteration: Int): PipelineComponent = {
    PipelineComponent("PC" + pipelineComponentCounter.incrementAndGet(), componentInstance, discoveryIteration)
  }

  private def pipelineComponents(portMatches: Seq[PortMatch], newLastComponent: PipelineComponent): Seq[PipelineComponent] = {
    portMatches.flatMap(_.startPipeline.components ++ Seq(newLastComponent)).distinct
  }

  private def pipelineBindings(portMatches: Seq[PortMatch], newLastComponent: PipelineComponent): Seq[PortBinding] = {
    val newBindings = portMatches.map { portMatch => PortBinding(portMatch.startPipeline.lastComponent, portMatch.port, newLastComponent) }
    portMatches.flatMap(_.startPipeline.bindings) ++ newBindings
  }

  private def dataSample(componentInstance: ComponentInstance, portMatches: Seq[PortMatch]): Future[DataSample] = {
    val dataSamples = portMatches.map { portMatch => portMatch.port -> portMatch.startPipeline.lastOutputDataSample }.toMap
    componentInstance match {
      case c: ProcessorInstance => c.getOutputDataSample(portMatches.last.maybeState, dataSamples)
      case v: VisualizerInstance => Future.successful(DataSample())
    }
  }
}
