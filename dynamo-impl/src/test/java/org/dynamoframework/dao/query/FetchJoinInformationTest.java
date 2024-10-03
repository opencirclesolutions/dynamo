package org.dynamoframework.dao.query;

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

import org.dynamoframework.dao.FetchJoinInformation;
import org.dynamoframework.dao.JoinType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FetchJoinInformationTest {

	@Test
	public void testEquals() {
		FetchJoinInformation info = new FetchJoinInformation("property1");
		assertEquals(JoinType.LEFT, info.getJoinType());

		assertFalse(info.equals(null));
		assertFalse(info.equals(new Object()));
		assertTrue(info.equals(info));

		FetchJoinInformation info2 = new FetchJoinInformation("property1");
		assertTrue(info.equals(info2));

		FetchJoinInformation info3 = new FetchJoinInformation("property1", JoinType.RIGHT);
		assertFalse(info.equals(info3));
	}

	@Test
	public void testHashcode() {
		FetchJoinInformation info = new FetchJoinInformation("property1");
		assertNotNull(info.hashCode());
	}
}
