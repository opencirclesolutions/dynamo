package com.ocs.dynamo.ui.composite.export.impl;

import java.util.ArrayList;

import org.junit.Test;
import org.mockito.Mock;

import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.ui.composite.export.ExportService;
import com.ocs.dynamo.ui.composite.type.ExportMode;
import com.vaadin.ui.UI;

public class ExportDelegateImplTest extends BaseMockitoTest {

	@Mock
	private ExportService exportService;

	@Mock
	private UI ui;

	private ExportDelegateImpl delegate = new ExportDelegateImpl();

	private EntityModelFactory factory = new EntityModelFactoryImpl();

	@Override
	public void setUp() {
		super.setUp();
		wireTestSubject(delegate);
	}

	@Test
	public void testExport() {
		EntityModel<TestEntity> em = factory.getModel(TestEntity.class);
		delegate.export(ui, em, ExportMode.FULL, null, null);
	}

	@Test
	public void testExportFixed() {
		EntityModel<TestEntity> em = factory.getModel(TestEntity.class);
		delegate.exportFixed(ui, em, ExportMode.FULL, new ArrayList<>());
	}
}
