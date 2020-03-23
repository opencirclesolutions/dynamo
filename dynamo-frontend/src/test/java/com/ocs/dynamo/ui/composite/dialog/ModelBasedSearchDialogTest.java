package com.ocs.dynamo.ui.composite.dialog;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.google.common.collect.Lists;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.filter.EqualsPredicate;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.ui.FrontendIntegrationTest;
import com.vaadin.flow.function.SerializablePredicate;

public class ModelBasedSearchDialogTest extends FrontendIntegrationTest {

    @Inject
    private EntityModelFactory entityModelFactory;

    @Inject
    private TestEntityService testEntityService;

    private TestEntity e1;

    private TestEntity e2;

    @BeforeEach
    public void setup() {
        MockVaadin.setup();
        e1 = new TestEntity("Bob", 11L);
        e1 = testEntityService.save(e1);

        e2 = new TestEntity("Harry", 12L);
        e2 = testEntityService.save(e2);
    }

    @Test
    public void testCreateSingleSelect() {
        ModelBasedSearchDialog<Integer, TestEntity> dialog = new ModelBasedSearchDialog<>(testEntityService,
                entityModelFactory.getModel(TestEntity.class), new ArrayList<>(), null, false, true);
        dialog.build();
        dialog.select(e1);
    }

    @Test
    public void testCreateSingleSelectWithFilter() {
        List<SerializablePredicate<TestEntity>> filters = new ArrayList<>();
        filters.add(new EqualsPredicate<TestEntity>("name", "Bob"));

        ModelBasedSearchDialog<Integer, TestEntity> dialog = new ModelBasedSearchDialog<>(testEntityService,
                entityModelFactory.getModel(TestEntity.class), filters, null, false, true);
        dialog.build();
    }

    @Test
    public void testCreateMultiSelect() {
        ModelBasedSearchDialog<Integer, TestEntity> dialog = new ModelBasedSearchDialog<>(testEntityService,
                entityModelFactory.getModel(TestEntity.class), new ArrayList<>(), null, true, true);
        dialog.build();
        dialog.select(Lists.newArrayList(e1, e2));
    }

}
