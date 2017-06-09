package com.ocs.dynamo.ui.composite.dialog;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseIntegrationTest;
import com.ocs.dynamo.ui.composite.layout.FormOptions;

public class EntityPopupDialogTest extends BaseIntegrationTest {

	@Inject
	private EntityModelFactory entityModelFactory;

	@Inject
	private TestEntityService testEntityService;

	private TestEntity e1;

	private TestEntity e2;

	@Before
	public void setup() {
		e1 = new TestEntity("Bob", 11L);
		e1 = testEntityService.save(e1);

		e2 = new TestEntity("Harry", 12L);
		e2 = testEntityService.save(e2);
	}

	/**
	 * 
	 */
	@Test
	public void testCreateNewEntity() {
		FormOptions fo = new FormOptions();
		EntityPopupDialog<Integer, TestEntity> dialog = new EntityPopupDialog<Integer, TestEntity>(testEntityService,
		        null, entityModelFactory.getModel(TestEntity.class), fo) {

			private static final long serialVersionUID = 485491706786708563L;

			@Override
			protected TestEntity createEntity() {
				TestEntity e1 = super.createEntity();
				e1.setName("Pete");
				return e1;
			}
		};
		dialog.build();
		dialog.getLayout().build();

		// OK button is only for read-only mode
		Assert.assertNull(dialog.getOkButton());

		TestEntity newEntity = dialog.getEntity();
		Assert.assertEquals("Pete", newEntity.getName());

		// save the new entity
		dialog.getSaveButtons().get(0).click();
		Assert.assertEquals(3, testEntityService.count());
	}

	@Test
	public void testUpdateExistingEntity() {
		FormOptions fo = new FormOptions();
		EntityPopupDialog<Integer, TestEntity> dialog = new EntityPopupDialog<Integer, TestEntity>(testEntityService,
		        e1, entityModelFactory.getModel(TestEntity.class), fo) {

			private static final long serialVersionUID = 485491706786708563L;

		};
		dialog.build();
		dialog.getLayout().build();

		dialog.getEntity().setName("BobChanged");

		// save the existing entity
		dialog.getSaveButtons().get(0).click();
		Assert.assertEquals(2, testEntityService.count());

		TestEntity saved = testEntityService.findById(e1.getId());
		Assert.assertEquals("BobChanged", saved.getName());
	}

	@Test
	public void testReadOnlyModues() {
		FormOptions fo = new FormOptions().setReadOnly(true);
		EntityPopupDialog<Integer, TestEntity> dialog = new EntityPopupDialog<Integer, TestEntity>(testEntityService,
		        e1, entityModelFactory.getModel(TestEntity.class), fo) {

			private static final long serialVersionUID = 485491706786708563L;

		};
		dialog.build();
		dialog.getLayout().build();

		// no save buttons present
		Assert.assertTrue(dialog.getSaveButtons().isEmpty());
		Assert.assertNotNull(dialog.getOkButton());
	}
}
