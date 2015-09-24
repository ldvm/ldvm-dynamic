package discovery

import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext


trait LdvmSpec extends FlatSpec with Matchers with PipelineAsserts {

  def createDiscovery()(implicit executor: ExecutionContext): Discovery = {
    val pipelineBuilder = new PipelineBuilder()
    new Discovery(new DiscoveryPortMatcher(pipelineBuilder), pipelineBuilder)
  }

}