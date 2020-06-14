/*
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.ocs.dynamo.mock;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;

import com.ocs.dynamo.CamelConstants;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.test.MockUtil;

/**
 * Base class for unit tests for components that are used in Camel routes
 * 
 * @author bas.rutten
 * 
 */
public abstract class BaseCamelMockitoTest extends BaseMockitoTest {

    @Mock
    private Exchange exchange;

    @Mock
    private Message message;

    @Mock
    private Message outMessage;

    @Mock
    private MessageService messageService;

    public Exchange getExchange() {
        return exchange;
    }

    public Message getMessage() {
        return message;
    }

    public MessageService getMessageService() {
        return messageService;
    }

    public Message getOutMessage() {
        return outMessage;
    }

    public <T> void givenBody(Class<T> clazz, T body) {
        lenient().when(message.getBody(clazz)).thenReturn(body);
        lenient().when(message.getBody()).thenReturn(body);
    }

    public void givenHeader(String name, Object value) {
        when(getMessage().getHeader(name)).thenReturn(value);
    }

    public List<String> mockErrorList() {
        List<String> errorList = new ArrayList<>();
        givenHeader(CamelConstants.HEADER_ERROR_LIST, errorList);
        return errorList;
    }

    public void setExchange(Exchange exchange) {
        this.exchange = exchange;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    public void setOutMessage(Message outMessage) {
        this.outMessage = outMessage;
    }

    @BeforeEach
    public void setUp() {
        lenient().when(exchange.getIn()).thenReturn(message);
        lenient().when(exchange.getOut()).thenReturn(outMessage);
        MockUtil.mockMessageService(messageService);
    }

}
