package org.apache.camel.component.liferay

import org.apache.camel.impl.DefaultConsumer
import org.apache.camel.{Processor, Endpoint}
import com.liferay.portal.kernel.messaging.{MessageListener, ParallelDestination, SerialDestination, MessageBus}

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: 29/9-11
 * Time: 21:22 
 */

class LiferayConsumer(val endpoint: LiferayEndpoint, var processor: Processor, val messageBus: MessageBus)
    extends DefaultConsumer(endpoint, processor) {

    var listener: MessageListener = _

    override protected def doStart {
        super.doStart()
        createMessageBusConnection
    }

    override def getEndpoint: LiferayEndpoint = super.getEndpoint.asInstanceOf[LiferayEndpoint]

    protected def createMessageBusConnection {
        registerDestination
        listener = new MessageBusListener(this);
        messageBus.registerMessageListener(endpoint.destination, listener)
    }

    protected def registerDestination {
        if (!messageBus.hasDestination(endpoint.destination)) {
            val destination = new ParallelDestination
            destination.setName(endpoint.destination)
            destination.setMaximumQueueSize(10)
            destination.setWorkersCoreSize(3)
            destination.afterPropertiesSet()

            messageBus.addDestination(destination)
        }
    }

    override def doResume() {
        super.doResume()
        messageBus.registerMessageListener(endpoint.destination, listener)
    }

    override def doStop() {
        messageBus.unregisterMessageListener(endpoint.destination, listener)
        super.doStop()
    }

    override def doSuspend() {
        messageBus.unregisterMessageListener(endpoint.destination, listener)
        super.doSuspend()
    }

    override def doShutdown() {
        messageBus.unregisterMessageListener(endpoint.destination, listener)
        super.doSuspend()
    }
}