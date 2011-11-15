package org.apache.camel.component.liferay.util

import com.liferay.portal.kernel.uuid.PortalUUID
import java.util.UUID
import com.liferay.portal.kernel.messaging.{Message, MessageListener}
import org.junit.Assert

/**
 * Created by IntelliJ IDEA.
 * Created: 2011-11-04 18:55 
 * @author <a href="mailto:david.rosell@redpill-linpro.com">David Rosell</a>
 */

class TestUUID extends PortalUUID {
    override def generate() = UUID.randomUUID().toString
}

class TestMessageListener extends MessageListener {
    def receive(message: Message) {
        println("TestMessageListener")
        Assert.assertEquals("Apa", message.getPayload)
    }
}

class ResponseMessageListener extends MessageListener {
    def receive(message: Message) {
        println("ResponseMessageListener")
        Assert.assertEquals("Apa", message.getPayload)
    }
}

class DefaultMessageListener extends MessageListener {
    def receive(message: Message) {
        println("DefaultMessageListener")
        Assert.assertEquals("Apa", message.getPayload)
    }
}
