package org.apache.camel.component.liferay

import org.apache.camel.impl.DefaultComponent
import com.liferay.portal.kernel.messaging.MessageBus
import java.util.Map
import org.apache.camel.Endpoint

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: 22/9-11
 * Time: 21:22 
 */

class LiferayComponent(val messageBus: MessageBus) extends DefaultComponent {

    def createEndpoint(uri: String, remaining: String, parameters: Map[String, AnyRef]) = {
        val endpoint = new LiferayEndpoint(uri, remaining, this, messageBus)
        setProperties(endpoint, parameters)
        endpoint
    }

}