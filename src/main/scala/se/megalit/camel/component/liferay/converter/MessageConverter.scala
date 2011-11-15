package org.apache.camel.component.liferay.converter

import org.apache.camel.Converter
import com.liferay.portal.kernel.messaging.Message

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: 1/10-11
 * Time: 18:49 
 */

@Converter
object MessageConverter {
    
    def toMessage(string: String): Message = {
        val message = new Message
        message.setPayload(string)
        
        message
    }
    
    def toMessage(ex: Exception): Message = {
        val message = new Message
        message.setPayload(ex)
        
        message
    }
}