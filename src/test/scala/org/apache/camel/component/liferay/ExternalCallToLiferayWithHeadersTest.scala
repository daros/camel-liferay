package org.apache.camel.component.liferay

import org.apache.camel.test.junit4.CamelTestSupport
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.mock.MockEndpoint
import org.junit.{Assert, Before, Test}
import com.liferay.portal.kernel.messaging.{DefaultMessageBus, Message}

import scala.collection.JavaConversions._
import java.util.UUID
import org.apache.camel.{Exchange, Processor}

/**
 * Created by IntelliJ IDEA.
 * Created: 2011-10-01 03:28
 *
 * @author <a href="mailto:david.rosell@redpill-linpro.com">David Rosell</a>
 */
class ExternalCallToLiferayWithHeadersTest extends CamelTestSupport {

    var epLiferayHeaders: String = _

    @Test
    def testCallLiferayWithHeaders {
        // Setup message
        val responseId = UUID.randomUUID().toString
        val message = new Message()
        message.setPayload("Apa")
        message.setResponseId(responseId)

        // Call route
        template.requestBodyAndHeaders("direct:headers", message, Map("Hapa" -> "1", "Hbepa" -> "2", "Hcepa" -> "3"))

        Thread.sleep(100)

        // Validate
        val ep = context.getEndpoint("mock:headers").asInstanceOf[MockEndpoint]

        Assert.assertEquals(1, ep.getReceivedCounter)
        ep.getReceivedExchanges.foreach(e => {
            Assert.assertEquals("Apa", e.getIn().getBody)
            Assert.assertEquals(responseId, e.getIn().getHeader("responseId"))
            Assert.assertEquals("1", e.getIn().getHeader("Hapa"))
            Assert.assertEquals("2", e.getIn().getHeader("Hbepa"))
            Assert.assertNull(e.getIn().getHeader("Hcepa"))
        })
    }

    @Before
    override def setUp() {
        epLiferayHeaders = "liferay:mb_destination/test?MessageHeaders=Hapa,Hbepa"

        super.setUp()
    }

    override def createRouteBuilder() = {
        val messageBus = new DefaultMessageBus
        context.addComponent("liferay", new LiferayComponent(messageBus))

        new RouteBuilder() {
            def configure() {
                from("direct:headers").to(epLiferayHeaders)
                from(epLiferayHeaders).to("mock:headers")
            }
        }
    }


}