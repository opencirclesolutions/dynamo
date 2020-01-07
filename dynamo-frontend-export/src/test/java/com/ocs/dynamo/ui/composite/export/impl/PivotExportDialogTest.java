package com.ocs.dynamo.ui.composite.export.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.ui.composite.export.ExportService;
import com.ocs.dynamo.ui.composite.export.PivotParameters;
import com.ocs.dynamo.ui.composite.export.PivotedExportDialog;

public class PivotExportDialogTest extends BaseMockitoTest {

    @Mock
    private ExportService exportService;

    private EntityModelFactory emf = new EntityModelFactoryImpl();

    @Before
    public void setup() {
        MockVaadin.setup();
    }

    @Test
    public void test() {
        EntityModel<TestEntity> em = emf.getModel(TestEntity.class);
        PivotParameters pars = new PivotParameters();

        Mockito.when(exportService.exportCsvPivot(em, null, null, pars)).thenReturn(new byte[] { 1, 2, 3 });
        Mockito.when(exportService.exportExcelPivot(em, null, null, null, pars)).thenReturn(new byte[] { 1, 2, 3 });

        PivotedExportDialog<Integer, TestEntity> dialog = new PivotedExportDialog<>(exportService, em, null, null, null,
                new PivotParameters());
        dialog.build();
        dialog.open();

    }
}
