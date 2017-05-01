package com.ocs.dynamo.ui.composite.form;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.ModelBasedFieldFactory;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.test.BaseIntegrationTest;
import com.ocs.dynamo.ui.composite.form.ModelBasedSearchForm.FilterType;
import com.vaadin.data.util.filter.And;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.ui.Slider;
import com.vaadin.ui.TextField;

public class FilterGroupTest extends BaseIntegrationTest {

    @Autowired
    private EntityModelFactory emf;

    @Autowired
    private MessageService messageService;

    private EntityModel<TestEntity> model;

    private ModelBasedFieldFactory<TestEntity> factory;

    private boolean listened = false;

    @Before
    public void setUp() {
        model = emf.getModel(TestEntity.class);
        factory = ModelBasedFieldFactory.getSearchInstance(model, messageService);
        listened = false;
    }

    @Test
    public void testTextFieldStringLike() {

        TextField tf = (TextField) factory.createField("name");
        FilterGroup fg = new FilterGroup(model.getAttributeModel("name"), FilterType.LIKE, null, tf, null);
        fg.addListener(event -> {
            Assert.assertTrue(event.getNewFilter() instanceof SimpleStringFilter);
            listened = true;
        });

        tf.setValue("bob");
        Assert.assertTrue(listened);
    }

    @Test
    public void testLongFieldBetween() {
        TextField tf = (TextField) factory.createField("age");
        FilterGroup fg = new FilterGroup(model.getAttributeModel("age"), FilterType.BETWEEN, null, tf, null);
        fg.addListener(event -> {
            Assert.assertTrue(event.getNewFilter() instanceof Compare.GreaterOrEqual);
            listened = true;
        });

        tf.setValue("4");
        Assert.assertTrue(listened);
    }

    @Test
    public void testLongFieldBetween2() {
        TextField main = (TextField) factory.createField("age");
        TextField aux = (TextField) factory.createField("age");

        FilterGroup fg = new FilterGroup(model.getAttributeModel("age"), FilterType.BETWEEN, null, main, aux);
        fg.addListener(event -> {
            if (!listened) {
                Assert.assertTrue(event.getNewFilter() instanceof Compare.GreaterOrEqual);
                listened = true;
            } else {
                // if both filters set, then we return an "And" filter that combines both
                Assert.assertTrue(event.getNewFilter() instanceof And);
            }
        });

        main.setValue("4");
        Assert.assertTrue(listened);

        aux.setValue("10");
    }

    @Test
    public void testIntSlider() {
        Slider main = (Slider) factory.createField("someIntSlider");
        Slider aux = (Slider) factory.createField("someIntSlider");

        FilterGroup fg = new FilterGroup(model.getAttributeModel("someIntSlider"), FilterType.BETWEEN, null, main, aux);
        fg.addListener(event -> {
            listened = true;
        });

        main.setValue(100.);
        Assert.assertTrue(listened);

        // check that value is reset to minimum
        fg.reset();
        Assert.assertEquals(main.getMin(), main.getValue(), 0.01);
        Assert.assertEquals(aux.getMin(), main.getValue(), 0.01);
    }

    @Test
    public void testTextFieldStringEqual() {

        TextField tf = (TextField) factory.createField("name");
        FilterGroup fg = new FilterGroup(model.getAttributeModel("name"), FilterType.EQUAL, null, tf, null);
        fg.addListener(event -> {
            Assert.assertTrue(event.getNewFilter() instanceof Compare.Equal);
            listened = true;
        });

        tf.setValue("bob");
        Assert.assertTrue(listened);
    }

    @Test
    public void testReset() {

        TextField tf = (TextField) factory.createField("name");
        FilterGroup fg = new FilterGroup(model.getAttributeModel("name"), FilterType.LIKE, null, tf, null);

        tf.setValue("bob");

        // test the resetting the field works
        fg.reset();
        Assert.assertNull(tf.getValue());
    }

}
