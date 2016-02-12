package nl.ocs.dao;

import nl.ocs.dao.impl.DefaultDaoImpl;
import nl.ocs.domain.QTestEntity;
import nl.ocs.domain.TestEntity;

import org.junit.Assert;
import org.junit.Test;

public class DefaultDaoImplTest {

	private DefaultDaoImpl<Integer, TestEntity> dao;

	@Test
	public void testCreate() {
		dao = new DefaultDaoImpl<>(QTestEntity.testEntity, TestEntity.class);
		Assert.assertEquals(QTestEntity.testEntity, dao.getDslRoot());
		Assert.assertEquals(TestEntity.class, dao.getEntityClass());
	}
}
