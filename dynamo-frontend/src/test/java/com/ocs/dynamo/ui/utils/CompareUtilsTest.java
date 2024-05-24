package com.ocs.dynamo.ui.utils;

import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.TestEntity2;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.impl.MessageServiceImpl;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.test.MockUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.lenient;

public class CompareUtilsTest extends BaseMockitoTest {

    private final EntityModelFactory factory = new EntityModelFactoryImpl();

    @Mock
    private MessageService messageService = new MessageServiceImpl();

    @BeforeEach
    public void setUp() {
        MockUtil.mockMessageService(messageService);
    }

    @Test
    public void testCompare() {
        TestEntity e1 = new TestEntity();
        TestEntity e2 = new TestEntity();

        lenient().when(messageService.getMessage(anyString(), nullable(Locale.class)))
                .thenAnswer(invocation -> invocation.getArguments()[0]);

        lenient().when(messageService.getMessage(anyString(), nullable(Locale.class),
                     Mockito.any(Object[].class)))
                .thenAnswer(invocation -> invocation.getArguments()[0]);


        // there are no changes
        List<String> changes = CompareUtils.compare(e1, e2, factory.getModel(TestEntity.class), factory, messageService);
        assertEquals(0, changes.size());

        e1.setAge(12L);
        changes = CompareUtils.compare(e1, e2, factory.getModel(TestEntity.class), factory, messageService);
        assertEquals(1, changes.size());
        assertEquals("ocs.value.changed", changes.get(0));

        e1.setAge(null);
        e2.setName("Kevin");
        changes = CompareUtils.compare(e1, e2, factory.getModel(TestEntity.class), factory, messageService);
        assertEquals(1, changes.size());
        assertEquals("ocs.value.changed", changes.get(0));
    }

    @Test
    public void testCompare_Ignore() {
        TestEntity e1 = new TestEntity();
        TestEntity e2 = new TestEntity();

        e1.setName("Bob");

        // name has changed
        List<String> changes = CompareUtils.compare(e1, e2, factory.getModel(TestEntity.class), factory, messageService);
        assertEquals(1, changes.size());

        // ignore the name change
        changes = CompareUtils.compare(e1, e2, factory.getModel(TestEntity.class), factory, messageService, "name");
        assertEquals(0, changes.size());
    }

    @Test
    public void testCompare_CollectionRemove() {
        TestEntity e1 = new TestEntity();
        TestEntity e2 = new TestEntity();

        e1.addTestEntity2(new TestEntity2());

        List<String> changes = CompareUtils.compare(e1, e2, factory.getModel(TestEntity.class), factory, messageService);
        assertEquals(1, changes.size());
        assertEquals("ocs.value.removed", changes.get(0));
    }

    @Test
    public void testCompare_CollectionAdd() {
        TestEntity e1 = new TestEntity();
        TestEntity e2 = new TestEntity();

        e2.addTestEntity2(new TestEntity2());

        List<String> changes = CompareUtils.compare(e1, e2, factory.getModel(TestEntity.class), factory, messageService);
        assertEquals(1, changes.size());
        assertEquals("ocs.value.added", changes.get(0));
    }
}
