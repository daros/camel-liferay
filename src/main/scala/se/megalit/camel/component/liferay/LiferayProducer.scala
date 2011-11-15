package org.apache.camel.component.liferay

import org.apache.camel.impl.DefaultProducer
import org.apache.camel.Exchange

import scala.collection.JavaConversions._
import java.util.Map
import com.liferay.portal.kernel.messaging.{MessageBus, Message}

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: 29/9-11
 * Time: 21:57 
 */

class LiferayProducer(val endpoint: LiferayEndpoint, messageBus: MessageBus)
    extends DefaultProducer(endpoint) {

    def process(exchange: Exchange) {
        val inMessage = exchange.getIn()
        val mbMessage: Message = inMessage.getBody.asInstanceOf[Message]

        // check for responseId
        val headers = inMessage.getHeaders()
        headers.filter(_._1 == "ResponseId").foreach(h => mbMessage.setResponseId(h._2.asInstanceOf[String]))

        headersFromMessage(inMessage).foreach(h => mbMessage.put(h._1, h._2))

        messageBus.sendMessage(endpoint.destination, mbMessage)
    }

    private def headersFromMessage(message: org.apache.camel.Message): Map[String, AnyRef] = {
        val headers = message.getHeaders
        val keys = endpoint.messageHeaders.split(",")

        val filtered = headers.filter(h => keys.contains(h._1))
        filtered
    }
}