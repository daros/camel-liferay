package org.apache.camel.component.liferay

import org.apache.camel.test.junit4.CamelTestSupport
import com.liferay.portal.kernel.messaging.sender.DefaultSynchronousMessageSender
import com.liferay.portal.kernel.messaging.config.DefaultMessagingConfigurator
import scala.collection.JavaConversions._
import org.apache.camel.scala.dsl.builder.RouteBuilder
import org.apache.camel.Exchange
import com.liferay.portal.kernel.messaging._
import util.{TestMessageListener, TestUUID}
import org.junit.{Test, Assert, After, Before}
import java.util.UUID
import org.apache.camel.component.mock.MockEndpoint

/**
 * Created by IntelliJ IDEA.
 * Created: 2011-11-04 18:52 
 * @author <a href="mailto:david.rosell@redpill-linpro.com">David Rosell</a>
 */

class LiferayCallToExternal extends CamelTestSupport {
    var messageBus: MessageBus = _
    var messageBusComponent: LiferayComponent = _
    var messageSender: DefaultSynchronousMessageSender = _
    var messagingConfigurator: DefaultMessagingConfigurator = _

    var destination: BaseDestination = _

    @Test
    def testCallLiferay {
        // Setup message
        val responseId = UUID.randomUUID().toString
        val message = new Message()
        message.setPayload("Apa")
        message.setResponseId(responseId)

        // Send message to MessageBus
        messageBus.sendMessage(destination.getName, message)
        Thread.sleep(250)

        // Validate
        val ep = context.getEndpoint("mock:test").asInstanceOf[MockEndpoint]
        ep.getReceivedExchanges.foreach(e => {
            Assert.assertEquals("Apa", e.getIn().getBody)
            Assert.assertEquals(responseId, e.getIn().getHeader("responseId"))
        })
        Assert.assertEquals(1, ep.getReceivedCounter)
    }

    @Before
    override def setUp() {
        messageBus = new DefaultMessageBus

        messageSender = new DefaultSynchronousMessageSender()
        messageSender.setMessageBus(messageBus)
        messageSender.setPortalUUID(new TestUUID)
        messageSender.setTimeout(10000)

        destination = new SerialDestination
        destination.setName("mb_destination/test")
        destination.open

        messagingConfigurator = new DefaultMessagingConfigurator()
        messagingConfigurator.setDestinations(List(destination))
        messagingConfigurator.setMessageBus(messageBus)
        messagingConfigurator.afterPropertiesSet()

        super.setUp()
    }

    @After
    override def tearDown() {
        messagingConfigurator.destroy()
        messageBus.shutdown(true)

        super.tearDown()
    }

    override def createRouteBuilder = {
        context.addComponent("liferay", new LiferayComponent(messageBus))

        destination.register(new TestMessageListener)

        new RouteBuilder {
            from("liferay:" + destination.getName).to("mock:test")
        }.builder
    }
}
