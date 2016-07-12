package com.ocs.dynamo.utils;

import java.math.BigDecimal;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;

public class ConvertUtilTest {

    private EntityModelFactory emf = new EntityModelFactoryImpl();

    @Test
    public void testConvertSearchValue() {
        EntityModel<TestEntity> model = emf.getModel(TestEntity.class);

        Object obj = ConvertUtil.convertSearchValue(model.getAttributeModel("age"), "12",
                new Locale("NL"));
        Assert.assertTrue(obj instanceof Long);
        Assert.assertEquals(12L, ((Long) obj).longValue());

        obj = ConvertUtil.convertSearchValue(model.getAttributeModel("discount"), "12,34",
                new Locale("NL"));
        Assert.assertTrue(obj instanceof BigDecimal);
        Assert.assertEquals(12.34, ((BigDecimal) obj).doubleValue(), 0.0001);

        obj = ConvertUtil.convertSearchValue(model.getAttributeModel("id"), "17", new Locale("NL"));
        Assert.assertTrue(obj instanceof Integer);
        Assert.assertEquals(17, ((Integer) obj).intValue());
      

    }
}
