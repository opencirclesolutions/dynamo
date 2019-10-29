package com.ocs.dynamo.ui.composite.export.impl;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.github.mvysny.kaributesting.v10.Routes;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.ui.composite.export.ExportService;
import com.ocs.dynamo.ui.composite.type.ExportMode;

@Ignore
public class ExportDelegateImplTest extends BaseMockitoTest {

    private static Routes routes;

    @BeforeClass
    public static void createRoutes() {
        // initialize routes only once, to avoid view auto-detection before every test
        // and to speed up the tests
        routes = new Routes().autoDiscoverViews("com.ocs.dynamo");
    }
    
    @Before
    public void setup() {
        MockVaadin.setup(routes);
    }
    
    @Mock
    private ExportService exportService;

    private ExportDelegateImpl delegate = new ExportDelegateImpl();

    private EntityModelFactory factory = new EntityModelFactoryImpl();

    @Test
    public void testExport() {
        EntityModel<TestEntity> em = factory.getModel(TestEntity.class);
        delegate.export(em, ExportMode.FULL, null, null);
    }

    @Test
    public void testExportFixed() {
        EntityModel<TestEntity> em = factory.getModel(TestEntity.class);
        delegate.exportFixed(em, ExportMode.FULL, new ArrayList<>());
    }
}
