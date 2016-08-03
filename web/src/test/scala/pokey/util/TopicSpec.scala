package pokey.util

import akka.testkit.TestProbe
import pokey.test.AkkaUnitSpec

class TopicSpec extends AkkaUnitSpec {
  "A Topic" when {
    val someMessage = 'msg
    "ActorRefs subscribe to it" should {
      "publish messages to those ActorRefs" in {
        val probes = Seq.fill(3)(TestProbe())
        val topic = probes.foldLeft(Topic())((topic, probe) => topic.subscribe(probe.ref))
        topic.publish(someMessage)
        probes.foreach { probe =>
          probe.expectMsg(someMessage)
        }
      }
    }

    "An ActorRef unsubscribes" should {
      "no longer send messages to that ActorRef" in {
        val probe = TestProbe()
        val topic = Topic().subscribe(probe.ref)
        topic.publish(someMessage)
        probe.expectMsg(someMessage)

        val topicWithoutProbe = topic.unsubscribe(probe.ref)
        topicWithoutProbe.publish(someMessage)
        probe.expectNoMsg()
      }
    }
  }
}
