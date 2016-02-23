package com.ocs.dynamo.dao.query;

import javax.persistence.criteria.JoinType;

import org.junit.Assert;
import org.junit.Test;

public class FetchJoinInformationTest {

	@Test
	public void testEquals() {
		FetchJoinInformation info = new FetchJoinInformation("property1");
		Assert.assertEquals(JoinType.LEFT, info.getJoinType());

		Assert.assertFalse(info.equals(null));
		Assert.assertFalse(info.equals(new Object()));
		Assert.assertTrue(info.equals(info));

		FetchJoinInformation info2 = new FetchJoinInformation("property1");
		Assert.assertTrue(info.equals(info2));

		FetchJoinInformation info3 = new FetchJoinInformation("property1", JoinType.RIGHT);
		Assert.assertFalse(info.equals(info3));
	}

	@Test
	public void testHashcode() {
		FetchJoinInformation info = new FetchJoinInformation("property1");
		Assert.assertNotNull(info.hashCode());
	}
}
