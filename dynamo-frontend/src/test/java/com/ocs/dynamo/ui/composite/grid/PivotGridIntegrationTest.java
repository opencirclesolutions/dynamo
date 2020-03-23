package com.ocs.dynamo.ui.composite.grid;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.function.Function;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.TestEntity.TestEnum;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.ui.FrontendIntegrationTest;
import com.ocs.dynamo.ui.provider.IdBasedDataProvider;
import com.ocs.dynamo.ui.provider.PivotDataProvider;

public class PivotGridIntegrationTest extends FrontendIntegrationTest {

    @Inject
    private TestEntityService testEntityService;

    @Inject
    private EntityModelFactory entityModelFactory;

    private TestEntity entity1;

    private TestEntity entity2;

    private TestEntity entity3;

    @BeforeEach
    public void setup() {
        entity1 = new TestEntity("Bob", 45L);
        entity1.setSomeEnum(TestEnum.A);
        entity1 = testEntityService.save(entity1);

        entity2 = new TestEntity("Bob", 45L);
        entity2.setSomeEnum(TestEnum.B);
        entity2 = testEntityService.save(entity1);

        entity3 = new TestEntity("Bob", 45L);
        entity3.setSomeEnum(TestEnum.C);
        entity3 = testEntityService.save(entity1);
    }

    @Test
    public void test() {
        IdBasedDataProvider<Integer, TestEntity> provider = new IdBasedDataProvider<>(testEntityService,
                entityModelFactory.getModel(TestEntity.class));

        PivotDataProvider<Integer, TestEntity> pivotProvider = new PivotDataProvider<>(provider, "name", "someEnum",
                Lists.newArrayList("name"), Lists.newArrayList("age"), () -> 10);

        PivotGrid<Integer, TestEntity> pg = new PivotGrid<Integer, TestEntity>(pivotProvider,
                Lists.newArrayList(TestEnum.A, TestEnum.B, TestEnum.C), Function.identity(), (a, b) -> a.toString());
        assertNotNull(pg.getColumnByKey("name"));
        assertNotNull(pg.getColumnByKey("A_age"));
        assertNotNull(pg.getColumnByKey("B_age"));
        assertNotNull(pg.getColumnByKey("C_age"));
    }
}
