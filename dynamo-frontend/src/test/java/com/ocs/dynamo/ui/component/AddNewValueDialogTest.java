package com.ocs.dynamo.ui.component;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.ocs.dynamo.domain.TestDomain;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.test.MockUtil;

public class AddNewValueDialogTest extends BaseMockitoTest {

    private boolean afterEntity = false;

    @Mock
    private MessageService messageService;

    @Mock
    private BaseService<Integer, TestDomain> baseService;

    private EntityModelFactory factory = new EntityModelFactoryImpl();

    @BeforeEach
    public void setup() {
        MockVaadin.setup();
        MockUtil.mockMessageService(messageService);
        when(baseService.createNewEntity()).thenReturn(new TestDomain());
    }

    @Test
    public void testCreateSuccesfully() {
        afterEntity = false;

        EntityModel<TestEntity> parent = factory.getModel(TestEntity.class);
        EntityModel<TestDomain> em = factory.getModel(TestDomain.class);
        AddNewValueDialog<Integer, TestDomain> dialog = new AddNewValueDialog<Integer, TestDomain>(em,
                parent.getAttributeModel("testDomain"), baseService, messageService) {

            private static final long serialVersionUID = -7549550521712714056L;

            @Override
            protected void afterNewEntityAdded(TestDomain entity) {
                afterEntity = true;
            }

        };
        dialog.build();
        dialog.open();

        dialog.getValueField().setValue("value");
        dialog.getOkButton().click();
        assertTrue(afterEntity);
    }
    
    @Test
    public void testCreateNotSuccessFull() {
        afterEntity = false;

        EntityModel<TestEntity> parent = factory.getModel(TestEntity.class);
        EntityModel<TestDomain> em = factory.getModel(TestDomain.class);
        AddNewValueDialog<Integer, TestDomain> dialog = new AddNewValueDialog<Integer, TestDomain>(em,
                parent.getAttributeModel("testDomain"), baseService, messageService) {

            private static final long serialVersionUID = -7549550521712714056L;

            @Override
            protected void afterNewEntityAdded(TestDomain entity) {
                afterEntity = true;
            }

        };
        dialog.buildAndOpen();

        // no value set
        dialog.getOkButton().click();
        assertFalse(afterEntity);
    }
}
