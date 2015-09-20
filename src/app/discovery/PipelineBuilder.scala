package discovery

import java.util.concurrent.atomic.AtomicInteger

import discovery.model._
import discovery.model.components.{ComponentInstance, ProcessorInstance}
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

class PipelineBuilder {
  val pipelineComponentCounter: AtomicInteger = new AtomicInteger()

  def buildPipeline(componentInstance: ComponentInstance, portMatches: Seq[PortMatch]): Future[Pipeline] = {
    val newLastComponent = PipelineComponent("PC" + pipelineComponentCounter.incrementAndGet(), componentInstance)
    val eventuallyDataSample: Future[DataSample] = dataSample(componentInstance, portMatches)
    eventuallyDataSample.map { dataSample => // TODO: Future data sample to pipeline
      Pipeline(
        pipelineComponents(portMatches, newLastComponent),
        pipelineBindings(portMatches, newLastComponent),
        newLastComponent,
        dataSample)
    }
  }

  private def pipelineComponents(portMatches: Seq[PortMatch], newLastComponent: PipelineComponent): Seq[PipelineComponent] = {
    portMatches.flatMap(_.startPipeline.components) :+ newLastComponent
  }

  private def pipelineBindings(portMatches: Seq[PortMatch], newLastComponent: PipelineComponent): Seq[PortBinding] = {
    val newBindings = portMatches.map { portMatch => PortBinding(portMatch.startPipeline.lastComponent, portMatch.port, newLastComponent) }
    portMatches.flatMap(_.startPipeline.bindings) ++ newBindings
  }

  private def dataSample(componentInstance: ComponentInstance, portMatches: Seq[PortMatch]): Future[DataSample] = {
    val dataSamples = portMatches.map { portMatch => portMatch.port -> portMatch.startPipeline.lastOutputDataSample }.toMap
    componentInstance match {
      case c: ProcessorInstance => c.getOutputDataSample(portMatches.last.maybeState, dataSamples)
      case _ => Future.successful(DataSample())
    }
  }
}
