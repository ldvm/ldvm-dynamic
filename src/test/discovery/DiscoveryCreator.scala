package discovery

import com.google.common.util.concurrent.MoreExecutors

import scala.concurrent.ExecutionContext

trait DiscoveryCreator {
  implicit val executor : ExecutionContext = ExecutionContext.fromExecutor(MoreExecutors.directExecutor())

  val pipelineBuilder = new PipelineBuilder()

  def createDiscovery(): Discovery = {
    new Discovery(new DiscoveryPortMatcher(pipelineBuilder), pipelineBuilder)
  }

  def createPortMatcher(): DiscoveryPortMatcher = {
    new DiscoveryPortMatcher(pipelineBuilder)
  }
}
