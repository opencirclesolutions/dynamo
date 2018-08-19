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

import org.apache.camel.Message;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;

/**
 * Utility class for mock functionality related to Apache Camel
 * 
 * @author bas.rutten
 *
 */
public final class CamelMockUtil {

	private CamelMockUtil() {
		// hidden constructor
	}

	/**
	 * Capture the setting of the body on a camel message
	 * 
	 * @param clazz
	 *            the class of the body
	 * @param message
	 *            the message
	 * @return
	 */
	public static <X> X captureBodySet(Class<X> clazz, Message message) {
		ArgumentCaptor<X> captor = ArgumentCaptor.forClass(clazz);
		Mockito.verify(message).setBody(captor.capture());
		return captor.getValue();
	}

	/**
	 * Capture the setting of a header on a Message
	 * 
	 * @param clazz
	 *            the class of the header
	 * @param name
	 *            the name of the header
	 * @param message
	 *            the message on which to set the header
	 * @return
	 */
	public static <X> X captureHeaderSet(Class<X> clazz, String name, Message message) {
		ArgumentCaptor<X> captor = ArgumentCaptor.forClass(clazz);
		Mockito.verify(message).setHeader(Matchers.eq(name), captor.capture());
		return captor.getValue();
	}

	public static void verifyHeaderNotSet(String name, Message message) {
		Mockito.verify(message, Mockito.times(0)).setHeader(Matchers.eq(name), Matchers.any());
	}
}
