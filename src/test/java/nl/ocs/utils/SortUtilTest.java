package nl.ocs.utils;

import nl.ocs.dao.SortOrder.Direction;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.data.sort.SortOrder;
import com.vaadin.shared.data.sort.SortDirection;

public class SortUtilTest {

	@Test
	public void testTranslate() {

		Assert.assertNull(SortUtil.translate());

		nl.ocs.dao.SortOrder[] orders = SortUtil.translate(new SortOrder("test1",
				SortDirection.ASCENDING));
		Assert.assertEquals(1, orders.length);

		Assert.assertEquals("test1", orders[0].getProperty());
		Assert.assertEquals(Direction.ASC, orders[0].getDirection());

		orders = SortUtil.translate(new SortOrder("test2", SortDirection.DESCENDING));
		Assert.assertEquals(1, orders.length);

		Assert.assertEquals("test2", orders[0].getProperty());
		Assert.assertEquals(Direction.DESC, orders[0].getDirection());
	}
}
