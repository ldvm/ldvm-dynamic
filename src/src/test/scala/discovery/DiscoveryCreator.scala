package discovery

import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global

trait DiscoveryCreator {

  val pipelineBuilder = new PipelineBuilder()

  def createDiscovery(): Discovery = {
    new Discovery(new DiscoveryPortMatcher(pipelineBuilder), pipelineBuilder)
  }

  def createPortMatcher(): DiscoveryPortMatcher = {
    new DiscoveryPortMatcher(pipelineBuilder)
  }
}
