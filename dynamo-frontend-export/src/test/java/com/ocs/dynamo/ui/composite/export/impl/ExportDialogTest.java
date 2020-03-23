package com.ocs.dynamo.ui.composite.export.impl;

import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.ui.composite.export.ExportDialog;
import com.ocs.dynamo.ui.composite.export.ExportService;
import com.ocs.dynamo.ui.composite.type.ExportMode;

public class ExportDialogTest extends BaseMockitoTest {

    @Mock
    private ExportService exportService;

    private EntityModelFactory emf = new EntityModelFactoryImpl();

    @BeforeEach
    public void setup() {
        MockVaadin.setup();
    }

    @Test
    public void test() {
        EntityModel<TestEntity> em = emf.getModel(TestEntity.class);

        when(exportService.exportCsv(em, ExportMode.FULL, null, null)).thenReturn(new byte[] { 1, 2, 3 });
        when(exportService.exportExcel(em, ExportMode.FULL, null, null, null)).thenReturn(new byte[] { 1, 2, 3 });

        ExportDialog<Integer, TestEntity> dialog = new ExportDialog<>(exportService, em, ExportMode.FULL, null, null, null);
        dialog.build();
        dialog.open();

    }
}
