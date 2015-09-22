package discovery

import org.scalatest.{FlatSpec, Matchers}

trait LdvmSpec extends FlatSpec with Matchers with PipelineAsserts {
  def createDiscovery(): Discovery = {
    val pipelineBuilder = new PipelineBuilder()
    new Discovery(new DiscoveryPortMatcher(pipelineBuilder), pipelineBuilder)
  }
}