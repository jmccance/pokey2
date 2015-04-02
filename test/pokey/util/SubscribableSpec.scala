package pokey.util

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import akka.testkit.EventFilter
import pokey.test.AkkaUnitSpec

class SubscribableSpec extends AkkaUnitSpec {

  "A Subscribable" when {
    import TestSubscribable._

    val someMessage = 'message

    "an actor subscribes to it" should {
      "execute the onSubscribe callback and reply with the Subscribed message" in {
        val subscribable = newSubscribable()

        EventFilter.info(message = SubMessage(self), occurrences = 1) intercept {
          subscribable ! Subscribe(self)
        }

        expectMsg(Subscribed(self))
      }
    }

    "it publishes a message" should {
      "send the message to all subscribed actors and invoke the onPublish callback" in {
        val subscribable = newSubscribable()

        EventFilter.info(message = SubMessage(self), occurrences = 1) intercept {
          subscribable ! Subscribe(self)
        }
        expectMsg(Subscribed(self))

        EventFilter.info(message = PubMessage(someMessage), occurrences = 1) intercept {
          // Send an auto-forwarded message to the subscribable
          subscribable ! someMessage
        }

        expectMsg(someMessage)
      }
    }

    "an actor unsubscribes from it" should {
      "execute onUnsubscribe callback and reply with the Unsubscribed message" in {
        val subscribable = newSubscribable()

        EventFilter.info(message = SubMessage(self), occurrences = 1) intercept {
          subscribable ! Subscribe(self)
        }
        expectMsg(Subscribed(self))

        EventFilter.info(message = UnSubMessage(self), occurrences = 1) intercept {
          subscribable ! Unsubscribe(self)
        }
        expectMsg(Unsubscribed(self))
      }

    }
  }

  object TestSubscribable extends TopicProtocol

  class TestSubscribable extends Actor with Subscribable with ActorLogging {
    import TestSubscribable._

    override protected val protocol: TopicProtocol = TestSubscribable

    override def receive: Receive = handleSubscriptions orElse {
      case msg => self ! Publish(msg)
    }

    override def onSubscribe(subscriber: ActorRef): Unit = log.info(SubMessage(subscriber))

    override def onPublish(message: Any): Unit = log.info(PubMessage(message))

    override def onUnsubscribe(subscriber: ActorRef): Unit = log.info(UnSubMessage(subscriber))
  }

  def newSubscribable() = system.actorOf(Props(new TestSubscribable))

  def SubMessage(subscriber: ActorRef) = s"onSubscribe: $subscriber"
  def PubMessage(message: Any) = s"onPublish: $message"
  def UnSubMessage(subscriber: ActorRef) = s"onUnsubscribe: $subscriber"
}
