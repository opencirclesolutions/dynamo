package com.ocs.dynamo.utils;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.annotation.Model;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.ServiceLocator;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.test.MockUtil;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class FormatUtilsTest extends BaseMockitoTest {

    private static final Locale LOCALE = new Locale.Builder().setLanguage("nl").build();

    private static EntityModelFactory factory = new EntityModelFactoryImpl();

    @Mock
    private static MessageService messageService;

    @Mock
    private static ServiceLocator serviceLocator;

    @BeforeEach
    void beforeEach() {
        ReflectionTestUtils.setField(FormatUtils.class, "locator", serviceLocator);
        ReflectionTestUtils.setField(factory, "serviceLocator", serviceLocator);
        when(serviceLocator.getEntityModelFactory())
                .thenReturn(factory);
        when(serviceLocator.getMessageService())
                .thenReturn(messageService);
        MockUtil.mockMessageService(messageService);
    }

    @Test
    public void testFormatPropertyValue() {

        EntityModel<TestEntity> model = factory.getModel(TestEntity.class);

        // simple string
        assertEquals("Bob", FormatUtils.formatPropertyValue(model.getAttributeModel("name"),
                "Bob", ", ", LOCALE));

        // boolean (without overrides)
        assertEquals("true", FormatUtils.formatPropertyValue(model.getAttributeModel("someBoolean"), true, ", ", LOCALE));
        assertEquals("false", FormatUtils.formatPropertyValue(model.getAttributeModel("someBoolean"), false, ", ", LOCALE));

        // boolean (with overrides)
        assertEquals("On", FormatUtils.formatPropertyValue(model.getAttributeModel("someBoolean2"), true, ", ", LOCALE));
        assertEquals("Off", FormatUtils.formatPropertyValue(model.getAttributeModel("someBoolean2"), false, ", ", LOCALE));

        // enumeration
        assertEquals("A", FormatUtils.formatPropertyValue(model.getAttributeModel("someEnum"),
                TestEntity.TestEnum.A, ", ", LOCALE));

        // BigDecimal
        assertEquals("12,40", FormatUtils.formatPropertyValue(model.getAttributeModel("discount"), BigDecimal.valueOf(12.4), ", ", LOCALE));
        assertEquals("1.042,40", FormatUtils.formatPropertyValue(model.getAttributeModel("discount"), BigDecimal.valueOf(1042.4), ", ", LOCALE));
        assertEquals("1.042,40%", FormatUtils.formatPropertyValue(model.getAttributeModel("rate"), BigDecimal.valueOf(1042.4), ", ", LOCALE));

        // US formatting (reverse separators)
        assertEquals("1,000.40",
                FormatUtils.formatPropertyValue(model.getAttributeModel("discount"),
                        BigDecimal.valueOf(1000.4), ", ", Locale.US));

        // date
        assertEquals("12/10/2015",
                FormatUtils.formatPropertyValue(model.getAttributeModel("birthDate"),
                        DateUtils.createLocalDate("12102015"), ", ", LOCALE));

        // integer (with grouping)
        assertEquals("1.234", FormatUtils.formatPropertyValue(model.getAttributeModel("someInt"), 1234, ", ", LOCALE));

        // long
        assertEquals("1.234", FormatUtils.formatPropertyValue(model.getAttributeModel("age"),
                1234L, ", ", LOCALE));
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

        String result = FormatUtils.formatPropertyValue(model.getAttributeModel("entity1"), e1,
                ", ", LOCALE);
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
        e3.setEntities(Set.of(t1, t2, t3));

        String result = FormatUtils.formatEntityCollection(null, e3.getEntities(), ", ", LOCALE);
        assertTrue(result.contains("a1"));
        assertTrue(result.contains("a2"));
        assertTrue(result.contains("a3"));
    }

    @Model(displayProperty = "name")
    static class Entity1 extends AbstractEntity<Integer> {

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

    @Getter
    @Setter
    static class Entity2 extends AbstractEntity<Integer> {

        private static final long serialVersionUID = -6048664928800386501L;

        private Integer id;

        private Integer size;

        private Entity1 entity1;

    }

    @Getter
    @Setter
    static class Entity3 extends AbstractEntity<Integer> {

        private static final long serialVersionUID = -6793879377561210713L;

        private Integer id;

        private Set<Entity1> entities = new HashSet<>();

    }

}
