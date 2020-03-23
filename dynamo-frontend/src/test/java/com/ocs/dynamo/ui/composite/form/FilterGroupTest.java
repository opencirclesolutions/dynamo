package com.ocs.dynamo.ui.composite.form;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.FieldFactory;
import com.ocs.dynamo.domain.model.FieldFactoryContext;
import com.ocs.dynamo.filter.AndPredicate;
import com.ocs.dynamo.filter.EqualsPredicate;
import com.ocs.dynamo.filter.GreaterOrEqualPredicate;
import com.ocs.dynamo.filter.SimpleStringPredicate;
import com.ocs.dynamo.ui.FrontendIntegrationTest;
import com.ocs.dynamo.ui.composite.form.ModelBasedSearchForm.FilterType;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.textfield.TextField;

public class FilterGroupTest extends FrontendIntegrationTest {

    @Autowired
    private EntityModelFactory emf;

    private EntityModel<TestEntity> model;

    private FieldFactory factory = FieldFactory.getInstance();

    private boolean listened = false;

    @BeforeEach
    public void setUp() {
        model = emf.getModel(TestEntity.class);
        listened = false;
    }

    private Component constructField(String name) {
        AttributeModel am = model.getAttributeModel(name);
        FieldFactoryContext context = FieldFactoryContext.create().setSearch(true).setAttributeModel(am);
        return factory.constructField(context);
    }

    @Test
    public void testTextFieldStringLike() {

        TextField tf = (TextField) constructField("name");
        FilterGroup<TestEntity> fg = new FilterGroup<>(model.getAttributeModel("name"), FilterType.LIKE, null, tf, null);
        fg.addListener(event -> {
            assertTrue(event.getNewFilter() instanceof SimpleStringPredicate);
            listened = true;
        });

        tf.setValue("bob");
        assertTrue(listened);
    }

    @Test
    public void testLongFieldBetween() {
        TextField tf = (TextField) constructField("age");
        FilterGroup<TestEntity> fg = new FilterGroup<>(model.getAttributeModel("age"), FilterType.BETWEEN, null, tf, null);
        fg.addListener(event -> {
            assertTrue(event.getNewFilter() instanceof GreaterOrEqualPredicate);
            listened = true;
        });

        tf.setValue("4");
        assertTrue(listened);
    }

    @Test
    public void testLongFieldBetween2() {
        TextField main = (TextField) constructField("age");
        TextField aux = (TextField) constructField("age");

        FilterGroup<TestEntity> fg = new FilterGroup<>(model.getAttributeModel("age"), FilterType.BETWEEN, null, main, aux);
        fg.addListener(event -> {
            if (!listened) {
                assertTrue(event.getNewFilter() instanceof GreaterOrEqualPredicate);
                listened = true;
            } else {
                // if both filters set, then we return an "And" filter that combines both
                assertTrue(event.getNewFilter() instanceof AndPredicate);
            }
        });

        main.setValue("4");
        assertTrue(listened);

        aux.setValue("10");
    }

//	@Test
//	public void testIntSlider() {
//		Slider main = (Slider) constructField("someIntSlider");
//		Slider aux = (Slider) constructField("someIntSlider");
//
//		FilterGroup<TestEntity> fg = new FilterGroup<>(model.getAttributeModel("someIntSlider"), FilterType.BETWEEN,
//				null, main, aux);
//		fg.addListener(event -> {
//			listened = true;
//		});
//
//		main.setValue(100.);
//		assertTrue(listened);
//
//		// check that value is reset to minimum
//		fg.reset();
//		assertEquals(main.getMin(), main.getValue(), 0.01);
//		assertEquals(aux.getMin(), main.getValue(), 0.01);
//	}

    @Test
    public void testTextFieldStringEqual() {

        TextField tf = (TextField) constructField("name");
        FilterGroup<TestEntity> fg = new FilterGroup<>(model.getAttributeModel("name"), FilterType.EQUAL, null, tf, null);
        fg.addListener(event -> {
            assertTrue(event.getNewFilter() instanceof EqualsPredicate);
            listened = true;
        });

        tf.setValue("bob");
        assertTrue(listened);
    }

    @Test
    public void testReset() {

        TextField tf = (TextField) constructField("name");
        FilterGroup<TestEntity> fg = new FilterGroup<>(model.getAttributeModel("name"), FilterType.LIKE, null, tf, null);
        tf.setValue("bob");

        // test the resetting the field works
        fg.reset();
        assertEquals("", tf.getValue());
    }

}
