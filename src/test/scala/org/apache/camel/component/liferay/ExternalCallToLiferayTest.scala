package org.apache.camel.component.liferay

import org.apache.camel.test.junit4.CamelTestSupport
import org.apache.camel.component.mock.MockEndpoint
import org.junit.{Assert, Before, Test}
import com.liferay.portal.kernel.messaging.{DefaultMessageBus, Message}

import scala.collection.JavaConversions._
import java.util.UUID
import org.apache.camel.scala.dsl.builder.RouteBuilder

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: 1/10-11
 * Time: 03:28 
 */

class ExternalCallToLiferayTest extends CamelTestSupport {

    var uri: String = _

    @Test
    def testCallLiferay {
        // Setup message
        val responseId = UUID.randomUUID().toString
        val message = new Message()
        message.setPayload("Apa")
        message.setResponseId(responseId)

        // Call route
        template.requestBody("direct:input", message)

        Thread.sleep(100)

        // Validate
        val ep = context.getEndpoint("mock:input").asInstanceOf[MockEndpoint]

        Assert.assertEquals(1, ep.getReceivedCounter)
        ep.getReceivedExchanges.foreach(e => {
            Assert.assertEquals("Apa", e.getIn().getBody)
            Assert.assertEquals(responseId, e.getIn().getHeader("responseId"))
        })
    }

    @Before
    override def setUp() {
        uri = "liferay:mb_destination/test"

        super.setUp()
    }

    override def createRouteBuilder() = {
        val messageBus = new DefaultMessageBus
        context.addComponent("liferay", new LiferayComponent(messageBus))

        new RouteBuilder {
            from("direct:input").to(uri)
            from(uri).to("mock:input")
        }.builder
    }


}