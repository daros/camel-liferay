package org.apache.camel.component.liferay

import java.util.Map
import reflect.BeanProperty
import org.apache.camel.util.ObjectHelper
import org.apache.camel.{Exchange, Producer, Consumer, Processor}
import org.apache.camel.impl.{DefaultExchange, DefaultEndpoint}
import com.liferay.portal.kernel.messaging.{Message, MessageBus}

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: 29/9-11
 * Time: 21:06 
 */

class LiferayEndpoint(val uri: String, val destination: String, val component: LiferayComponent,
    val messageBus: MessageBus) extends DefaultEndpoint(uri, component) {

    @BeanProperty
    var messageHeaders = ""

    @BeanProperty
    var destinationType = ""

    def createConsumer(processor: Processor): Consumer = {
        ObjectHelper.notNull(messageBus, "messageBus")
        new LiferayConsumer(this, processor, messageBus)
    }

    def createProducer: Producer = {
        ObjectHelper.notNull(messageBus, "messageBus")
        new LiferayProducer(this, messageBus)
    }

    override def isSingleton = false

    override def isLenientProperties = true

    def createMessageBusExchange(message: Message) = {
        val exchange = createExchange()
        exchange.getIn.setHeader("ResponseId", message.getResponseId)
        exchange.getIn.setBody(message.getPayload)

        headersFromMessage("MessageHeaders", message).foreach(h => exchange.getIn.setHeader(h._1, h._2))

        exchange
    }

    private def headersFromMessage(key: String, mbMessage: Message): Iterable[(String, AnyRef)] = {
        val keys = this.messageHeaders.split(",")

        val headers = keys.map(key => (key, mbMessage.get(key)))
        headers
    }
}