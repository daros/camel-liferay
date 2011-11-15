package org.apache.camel.component.liferay

import com.liferay.portal.kernel.messaging.{Message, MessageListener}
import org.apache.camel.{Exchange, Processor}
import org.apache.camel.impl.{DefaultMessage, DefaultExchange}
import collection.JavaConversions._
import collection.mutable.Iterable

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: 29/9-11
 * Time: 22:10 
 */

class MessageBusListener(val consumer: LiferayConsumer) extends  MessageListener {

    override def receive(message: Message) {
        val exchange = consumer.endpoint.createMessageBusExchange(message)

        try {
            consumer.processor.process(exchange)
        } catch {
            case ex: Exception => ex.printStackTrace()
        } finally {
            if (exchange.getException != null) {
                exchange.getException.printStackTrace()
            }
        }
    }
}