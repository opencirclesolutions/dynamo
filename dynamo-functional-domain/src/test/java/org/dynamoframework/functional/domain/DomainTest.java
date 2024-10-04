package org.dynamoframework.functional.domain;

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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DomainTest {

	@Test
	public void testEquals() {

		Country c1 = new Country("NL", "Nederland");
		c1.setId(1);

		Country c2 = new Country("NL", "Nederland");
		c2.setId(1);

		Country c3 = new Country("NL", "Nederland");
		Country c4 = new Country("NL", "Nederland");

		Region r1 = new Region(null, "Europe");
		r1.setId(1);

		assertFalse(c1.equals(null));
		assertFalse(c1.equals(new Object()));

		// IDs match but class is different
		assertFalse(c1.equals(r1));

		// IDs are the same
		assertTrue(c1.equals(c2));
		// no IDs, check the code and type instead
		assertTrue(c3.equals(c4));

		// partial IDs
		assertTrue(c1.equals(c4));
	}
}
