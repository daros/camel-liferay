package org.apache.camel.component.liferay

import org.apache.camel.test.junit4.CamelTestSupport
import com.liferay.portal.kernel.messaging.sender.DefaultSynchronousMessageSender
import com.liferay.portal.kernel.messaging.config.DefaultMessagingConfigurator
import com.liferay.portal.kernel.messaging._
import scala.collection.JavaConversions._
import org.junit.{Assert, Test, After, Before}
import org.apache.camel.Exchange
import org.apache.camel.component.mock.MockEndpoint
import org.apache.camel.scala.dsl.builder.RouteBuilder
import scala.Predef._
import util.{DefaultMessageListener, ResponseMessageListener, TestMessageListener, TestUUID}

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: 2/10-11
 * Time: 16:51 
 */

class LiferayQueryExternalTest extends CamelTestSupport {

    var uriIn: String = _
    var uriReply: String = _
    var uriDefaultResponse: String = _

    var destinationIn: BaseDestination = _
    var destinationReply: BaseDestination = _
    var defaultResponse: BaseDestination = _

    var messageBus: MessageBus = _
    var messageBusComponent: LiferayComponent = _
    var messageSender: DefaultSynchronousMessageSender = _
    var messagingConfigurator: DefaultMessagingConfigurator = _

    var mbMessage: Message = _

    @Test
    def testListenForReply() {
        try {
            messageBus.registerMessageListener(destinationIn.getName, new TestMessageListener)
            messageBus.registerMessageListener(destinationReply.getName, new ResponseMessageListener)
            messageBus.registerMessageListener(defaultResponse.getName, new DefaultMessageListener)

            println(destinationIn.isRegistered)
            println(destinationReply.isRegistered)
            println(defaultResponse.isRegistered)

            mbMessage = new Message
            mbMessage.setPayload("Apa")


            messageBus.sendMessage(destinationIn.getName, mbMessage)
            messageBus.sendMessage(destinationIn.getName, mbMessage)

            Thread.sleep(250)
            //            var reply = messageSender.send(destinationIn.getName, mbMessage, 1000)
            //            println("reply: " + reply)
            //            Thread.currentThread().wait(20000)
            val ep = context.getEndpoint("mock:test").asInstanceOf[MockEndpoint]

            Assert.assertEquals(2, ep.getReceivedCounter)
        } finally {
            //            messageBus.unregisterMessageListener(destinationReply.getName, testListener)
        }
    }

    @Before
    override def setUp() {
        messageBus = new DefaultMessageBus

        messageSender = new DefaultSynchronousMessageSender()
        messageSender.setMessageBus(messageBus)
        messageSender.setPortalUUID(new TestUUID)
        messageSender.setTimeout(10000)

        destinationIn = new SerialDestination
        destinationIn.setName("mb_destination/in")
        destinationIn.open
        destinationReply = new SerialDestination
        destinationReply.setName("mb_destination/reply")
        destinationReply.open
        defaultResponse = new SerialDestination
        defaultResponse.setName("liferay/message_bus/default_response")
        defaultResponse.open

        messagingConfigurator = new DefaultMessagingConfigurator()
        messagingConfigurator.setDestinations(List(destinationIn, destinationReply, defaultResponse))
        messagingConfigurator.setMessageBus(messageBus)
        messagingConfigurator.afterPropertiesSet()

        uriIn = "liferay:" + destinationIn.getName
        uriReply = "liferay:" + destinationReply.getName
        uriDefaultResponse = "liferay:" + defaultResponse.getName

        super.setUp()
    }

    @After
    override def tearDown() {
        messagingConfigurator.destroy()
        messageBus.shutdown(true)

        super.tearDown()
    }

    override def createRouteBuilder = {
        messageBus.addDestination(defaultResponse)

        messageBusComponent = new LiferayComponent(messageBus)
        context.addComponent("liferay", messageBusComponent)

        defaultResponse.register(new DefaultMessageListener)

        new RouteBuilder {
            ("liferay:" + destinationIn.getName).to("direct:test")
            "direct:test".to("mock:test")
        }.builder
    }
}
