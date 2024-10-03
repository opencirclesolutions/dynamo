package org.dynamoframework.utils;

/*-
 * #%L
 * Dynamo Framework
 * %%
 * Copyright (C) 2014 - 2024 Open Circle Solutions
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Some utilities to simplify working with URL's
 *
 * @author Patrick Deenen (patrick@opencircle.solutions)
 */
public final class UrlUtils {

	private UrlUtils() {
	}

	/**
	 * Create an http url query with the given key value parameters, will apply url encoding on the
	 * parameter values.
	 *
	 * @param hostAndPath the base url
	 * @param keyValues   Provide key1, value1, key2, value2, etc.
	 * @return The URI
	 * @throws URISyntaxException
	 */
	public static URI createUrl(String hostAndPath, String... keyValues) throws URISyntaxException {
		URI result = null;
		if (hostAndPath != null && keyValues != null && keyValues.length % 2 == 0) {
			StringBuilder r = new StringBuilder();
			if (hostAndPath.startsWith("http:")) {
				r.append(hostAndPath.substring(5));
			} else {
				r.append(hostAndPath);
			}
			boolean hasQuery = hostAndPath.matches(".+?.+=.+");
			if (keyValues.length > 0 && !hasQuery) {
				r.append("?");
			}
			for (int i = 0; i < keyValues.length - 1; i += 2) {
				if (keyValues[i] != null && keyValues[i + 1] != null) {
					if (i > 1 || hasQuery) {
						r.append("&");
					}
					r.append(keyValues[i]).append("=").append(keyValues[i + 1]);
				}
			}
			result = new URI("http", r.toString(), null);
		}
		return result;
	}
}
