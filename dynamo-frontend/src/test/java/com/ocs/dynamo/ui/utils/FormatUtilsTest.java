package com.ocs.dynamo.ui.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.persistence.ElementCollection;

import org.assertj.core.util.Sets;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.TestEntity.TestEnum;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.annotation.Attribute;
import com.ocs.dynamo.domain.model.annotation.Model;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.utils.DateUtils;

public class FormatUtilsTest extends BaseMockitoTest {

    private static final Locale LOCALE = new Locale("nl");

    private EntityModelFactory factory = new EntityModelFactoryImpl();

    @BeforeAll
    public static void beforeClass() {
        System.setProperty(DynamoConstants.SP_SERVICE_LOCATOR_CLASS_NAME, "com.ocs.dynamo.ui.SpringTestServiceLocator");
        System.setProperty(DynamoConstants.SP_DEFAULT_LOCALE, "de");
    }

    @Test
    public void testFormatPropertyValue() {

        EntityModel<TestEntity> model = factory.getModel(TestEntity.class);

        // simple string
        assertEquals("Bob", FormatUtils.formatPropertyValue(null, factory, model.getAttributeModel("name"), "Bob", LOCALE, ", "));

        // boolean (without overrides)
        assertEquals("true", FormatUtils.formatPropertyValue(null, factory, model.getAttributeModel("someBoolean"), true, LOCALE, ", "));
        assertEquals("false", FormatUtils.formatPropertyValue(null, factory, model.getAttributeModel("someBoolean"), false, LOCALE, ", "));

        // boolean (with overrides)
        assertEquals("On", FormatUtils.formatPropertyValue(null, factory, model.getAttributeModel("someBoolean2"), true, LOCALE, ", "));
        assertEquals("Off", FormatUtils.formatPropertyValue(null, factory, model.getAttributeModel("someBoolean2"), false, LOCALE, ", "));

        // enumeration
        assertEquals("Value A",
                FormatUtils.formatPropertyValue(null, factory, model.getAttributeModel("someEnum"), TestEnum.A, LOCALE, ", "));

        // BigDecimal
        assertEquals("12,40", FormatUtils.formatPropertyValue(null, factory, model.getAttributeModel("discount"), BigDecimal.valueOf(12.4),
                LOCALE, ", "));
        assertEquals("1.042,40", FormatUtils.formatPropertyValue(null, factory, model.getAttributeModel("discount"),
                BigDecimal.valueOf(1042.4), LOCALE, ", "));
        assertEquals("1.042,40%",
                FormatUtils.formatPropertyValue(null, factory, model.getAttributeModel("rate"), BigDecimal.valueOf(1042.4), LOCALE, ", "));

        // US formatting (reverse separators)
        assertEquals("1,000.40", FormatUtils.formatPropertyValue(null, factory, model.getAttributeModel("discount"),
                BigDecimal.valueOf(1000.4), Locale.US, ", "));

        // date
        assertEquals("12/10/2015", FormatUtils.formatPropertyValue(null, factory, model.getAttributeModel("birthDate"),
                DateUtils.createLocalDate("12102015"), LOCALE, ", "));

        // date (as week)
        assertEquals("2015-42", FormatUtils.formatPropertyValue(null, factory, model.getAttributeModel("birthWeek"),
                DateUtils.createLocalDate("12102015"), LOCALE, ", "));

        // integer (with grouping)
        assertEquals("1.234", FormatUtils.formatPropertyValue(null, factory, model.getAttributeModel("someInt"), 1234, LOCALE, ", "));

        // long
        assertEquals("1.234", FormatUtils.formatPropertyValue(null, factory, model.getAttributeModel("age"), 1234L, LOCALE, ", "));
    }

    @Test
    public void testFormatMasterEntity() {
        EntityModel<Entity2> model = factory.getModel(Entity2.class);
        AttributeModel at = model.getAttributeModel("entity1");
        assertNotNull(at);

        Entity1 e1 = new Entity1();
        e1.setId(1);
        e1.setName("some name");
        Entity2 e2 = new Entity2();
        e2.setId(2);
        e2.setSize(2);
        e2.setEntity1(e1);

        String result = FormatUtils.formatPropertyValue(null, factory, model.getAttributeModel("entity1"), e1, LOCALE, ", ");
        assertEquals("some name", result);
    }

    @Test
    public void testFormatEntityCollection() {

        Entity1 t1 = new Entity1();
        t1.setId(1);
        t1.setName("a1");

        Entity1 t2 = new Entity1();
        t2.setId(2);
        t2.setName("a2");

        Entity1 t3 = new Entity1();
        t3.setId(3);
        t3.setName("a3");

        Entity3 e3 = new Entity3();
        e3.setEntities(Sets.newLinkedHashSet(t1, t2, t3));

        String result = FormatUtils.formatEntityCollection(factory, null, e3.getEntities(), ", ");
        assertEquals("a1, a2, a3", result);
    }

    /**
     * Test the formatting of the elements inside an element collection
     */
    @Test
    public void testFormatEntityCollection_ElementCollection() {

        Entity4 e = new Entity4();
        e.setDecimals(Set.of(BigDecimal.valueOf(5), BigDecimal.valueOf(6)));

        String result = FormatUtils.formatEntityCollection(factory, factory.getModel(Entity4.class).getAttributeModel("decimals"),
                e.getDecimals(), ",");
        assertTrue(result.contains("5,00"));
        assertTrue(result.contains("6,00"));
    }

    /**
     * Test the formatting of the elements inside an element collection (of
     * percentages)
     */
    @Test
    public void testFormatEntityCollection_ElementCollectionPercentage() {

        Entity4 e = new Entity4();
        e.setDecimalPercentages(Set.of(BigDecimal.valueOf(5), BigDecimal.valueOf(6)));

        String result = FormatUtils.formatEntityCollection(factory, factory.getModel(Entity4.class).getAttributeModel("decimalPercentages"),
                e.getDecimalPercentages(), ",");
        assertTrue(result.contains("5,00%"));
        assertTrue(result.contains("6,00%"));
    }

    @Model(displayProperty = "name")
    class Entity1 extends AbstractEntity<Integer> {

        private static final long serialVersionUID = -706695912687382812L;

        private Integer id;

        private String name;

        @Override
        public Integer getId() {
            return id;
        }

        @Override
        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    class Entity2 extends AbstractEntity<Integer> {

        private static final long serialVersionUID = -6048664928800386501L;

        private Integer id;

        private Integer size;

        private Entity1 entity1;

        @Override
        public Integer getId() {
            return id;
        }

        @Override
        public void setId(Integer id) {
            this.id = id;
        }

        public Integer getSize() {
            return size;
        }

        public void setSize(Integer size) {
            this.size = size;
        }

        public Entity1 getEntity1() {
            return entity1;
        }

        public void setEntity1(Entity1 entity1) {
            this.entity1 = entity1;
        }
    }

    class Entity3 extends AbstractEntity<Integer> {

        private static final long serialVersionUID = -6793879377561210713L;

        private Integer id;

        private Set<Entity1> entities = new HashSet<>();

        @Override
        public Integer getId() {
            return id;
        }

        @Override
        public void setId(Integer id) {
            this.id = id;

        }

        public Set<Entity1> getEntities() {
            return entities;
        }

        public void setEntities(Set<Entity1> entities) {
            this.entities = entities;
        }
    }

    class Entity4 extends AbstractEntity<Integer> {

        private static final long serialVersionUID = -6617462805267353476L;

        private Integer id;

        @Override
        public Integer getId() {
            return id;
        }

        @Override
        public void setId(Integer id) {
            this.id = id;

        }

        @ElementCollection
        private Set<BigDecimal> decimals = new HashSet<>();

        @ElementCollection
        @Attribute(percentage = true)
        private Set<BigDecimal> decimalPercentages = new HashSet<>();

        public Set<BigDecimal> getDecimals() {
            return decimals;
        }

        public void setDecimals(Set<BigDecimal> decimals) {
            this.decimals = decimals;
        }

        public Set<BigDecimal> getDecimalPercentages() {
            return decimalPercentages;
        }

        public void setDecimalPercentages(Set<BigDecimal> decimalPercentages) {
            this.decimalPercentages = decimalPercentages;
        }

    }
}
