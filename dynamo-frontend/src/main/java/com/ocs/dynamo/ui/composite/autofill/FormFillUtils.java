/*
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.ocs.dynamo.ui.composite.autofill;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.filter.Compare;
import com.ocs.dynamo.filter.Like;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.ServiceLocator;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.component.*;
import com.ocs.dynamo.ui.composite.layout.TabWrapper;
import com.ocs.dynamo.utils.ClassUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.*;
import com.vaadin.flow.component.timepicker.TimePicker;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@UtilityClass
@Slf4j
public class FormFillUtils {

    private final ServiceLocator serviceLocator = ServiceLocatorFactory.getServiceLocator();

    /**
     * Record with information of the hierarchy of the components to be filled and
     * the value types of each one of them.
     *
     * @param componentInfoList      the components' information
     * @param componentsJSONMap      the components' hierarchy where parent and children are
     *                               described in a map
     * @param componentsTypesJSONMap the components' value types
     */
    public record ComponentsMapping(List<ComponentInfo> componentInfoList,
                                    Map<String, Object> componentsJSONMap,
                                    Map<String, String> componentsTypesJSONMap) implements Serializable {
    }

    /**
     * Record with information of a component to be filled.
     *
     * @param id        the component id
     * @param type      the component type
     * @param component the component
     */
    public record ComponentInfo(String id, String type, Component component) implements Serializable {
    }

    /**
     * Creates the mapped structures with the information required to generate the
     * prompt and fill the components after the response. This includes the hierarchy
     * of components and the value types of each component.
     *
     * @param component the component target (can be a component or a container of components)
     * @return the mapped structures with the information
     */
    public static ComponentsMapping createMapping(Component component,
                                                  EntityModel<?> entityModel) {
        List<ComponentInfo> componentInfoList = getComponentInfo(component);
        return new ComponentsMapping(componentInfoList,
                buildHierarchy(componentInfoList),
                buildTypes(componentInfoList, entityModel));
    }

    /**
     * Get all the components detailed information from a parent component.
     * {@link ComponentInfo} includes the component id, type and the component itself.
     *
     * @param component target
     * @return List of all component children information
     */
    public static List<ComponentInfo> getComponentInfo(Component component) {
        List<ComponentInfo> componentInfoList = new ArrayList<>();

        if (isSupportedAndAccepted(component)) {
            componentInfoList.add(new ComponentInfo(component.getId().orElse(null), component.getClass().getSimpleName(), component));
        }
        findChildComponents(component, componentInfoList);
        return componentInfoList;
    }

    private static void findChildComponents(Component component, List<ComponentInfo> componentInfoList) {
        component.getChildren().forEach(childComponent -> {

            if (isSupportedAndAccepted(childComponent)) {
                componentInfoList.add(new ComponentInfo(childComponent.getId().orElse(null), childComponent.getClass().getSimpleName(), childComponent));
            }

            if (component instanceof TabWrapper wrapper) {
                for (int i = 0; i < wrapper.getTabCount(); i++) {
                    // in case of a tab wrapper, we need to process the actual components, not the tabs
                    Component tabComponent = wrapper.getComponentAt(i);
                    findChildComponents(tabComponent, componentInfoList);
                }
            } else {
                findChildComponents(childComponent, componentInfoList);
            }


        });
    }

    private static Map<String, Object> buildHierarchy(List<ComponentInfo> componentInfoList) {
        Map<String, Object> json = new HashMap<>();
        for (ComponentInfo componentInfo : componentInfoList) {
            if (componentInfo.type.equalsIgnoreCase("Column")) {
                continue;
            }
            String id = componentInfo.id;
            if (id != null && !id.isEmpty()) {
                if (componentInfo.component instanceof Grid
                        || componentInfo.component instanceof MultiSelectListBox) {
                    if (componentInfo.component instanceof Grid grid) {
                        if (grid.getBeanType() == null) {
                            log.error("Grid with id {} must define a Bean Type to be used with FormFiller", grid.getId());
                        } else {
                            HashMap<String, Object> columns = new HashMap<>(Arrays
                                    .stream(grid.getBeanType().getDeclaredFields())
                                    .collect(Collectors.toMap(Field::getName, f -> "")));

                            ArrayList<HashMap<String, Object>> listColumns = new ArrayList<>();
                            listColumns.add(columns);
                            json.put(id, listColumns);
                        }
                    } else {
                        json.put(id, new ArrayList<>());
                    }
                } else {
                    json.put(id, "");
                }
            }
        }
        return json;
    }

    /**
     * Get all the components expected types to ask the LLM model.
     * It is important to notice that the type description should be
     * understandable by the LLM, we are not talking about any specific
     * coding language type or class. This type helps the LLM to format
     * the value inside the response JSON.
     *
     * @param componentInfoList a list of components
     * @return the map of expected types per target component.
     */
    @SuppressWarnings("unchecked")
    private static Map<String, String> buildTypes(List<ComponentInfo> componentInfoList,
                                                  EntityModel<?> entityModel) {
        Map<String, String> inputFieldMap = new HashMap<>();

        for (ComponentInfo componentInfo : componentInfoList) {
            AttributeModel am = entityModel.getAttributeModel(componentInfo.id);
            try {
                if ((componentInfo.component instanceof TextField)
                        || (componentInfo.component instanceof TextArea)
                        || (componentInfo.component instanceof EmailField)
                        || (componentInfo.component instanceof PasswordField)) {
                    inputFieldMap.put(componentInfo.id, "a String");
                } else if ((componentInfo.component instanceof NumberField)) {
                    inputFieldMap.put(componentInfo.id, "a Number");
                } else if ((componentInfo.component instanceof IntegerField)) {
                    inputFieldMap.put(componentInfo.id, "a Integer");
                } else if ((componentInfo.component instanceof BigDecimalField)) {
                    inputFieldMap.put(componentInfo.id, "a Double");
                } else if ((componentInfo.component instanceof DatePicker)) {
                    inputFieldMap.put(componentInfo.id, "a date using format 'yyyy-MM-dd'");
                } else if ((componentInfo.component instanceof TimePicker)) {
                    inputFieldMap.put(componentInfo.id, "a time using format 'HH:mm:ss'");
                } else if ((componentInfo.component instanceof DateTimePicker)) {
                    inputFieldMap.put(componentInfo.id, "a date and time using format 'yyyy-MM-ddTHH:mm:ss'");
                } else if (componentInfo.component instanceof ComboBox<?>) {
                    StringJoiner joiner = new StringJoiner("\" OR \"");
                    if (am != null && am.getType().isEnum()) {
                        getEnumConstants(am.getType())
                                .forEach(enumValue -> joiner.add(enumValue.toString()));
                        inputFieldMap.put(componentInfo.id, "an enumeration value from one of these options \"" + joiner + "\"");
                    } else {
                        // fall back to string
                        ((ComboBox<String>) componentInfo.component).getListDataView().getItems().forEach(joiner::add);
                        inputFieldMap.put(componentInfo.id, "a String from one of these options \"" + joiner + "\"");
                    }
                } else if (componentInfo.component instanceof MultiSelectComboBox) {
                    StringJoiner joiner = new StringJoiner("\", \"");
                    ((MultiSelectComboBox<String>) componentInfo.component).getListDataView().getItems().forEach(joiner::add);
                    inputFieldMap.put(componentInfo.id, "a Set of Strings selecting none, one or more of these options  \"" + joiner + "\"");
                } else if ((componentInfo.component instanceof Checkbox)) {
                    inputFieldMap.put(componentInfo.id, "a Boolean");
                } else if ((componentInfo.component instanceof CheckboxGroup<?>)) {
                    StringJoiner joiner = new StringJoiner("\", \"");
                    ((CheckboxGroup<String>) componentInfo.component).getListDataView().getItems().forEach(joiner::add);
                    inputFieldMap.put(componentInfo.id, "a Set of Strings selecting none, one or more of these options  \"" + joiner + "\"");
                } else if ((componentInfo.component instanceof RadioButtonGroup<?>)) {
                    StringJoiner joiner = new StringJoiner("\" OR \"");
                    ((RadioButtonGroup<String>) componentInfo.component).getListDataView().getItems().forEach(joiner::add);
                    inputFieldMap.put(componentInfo.id, "a String from one of these options \"" + joiner + "\"");
                } else if (componentInfo.component instanceof Grid<?> grid) {
                    buildTypesForGrid(inputFieldMap, grid.getBeanType(), am.getNestedEntityModel());
                } else if (componentInfo.component instanceof DetailsEditGrid<?, ?>) {
                    buildTypesForGrid(inputFieldMap, am.getNestedEntityModel().getEntityClass(), am
                            .getNestedEntityModel());
                } else if (componentInfo.component instanceof QuickAddEntityField) {
                    // entity fields, not possible to list all the options due to request size restrictions
                    inputFieldMap.put(componentInfo.id, "a String");
                }
            } catch (RuntimeException ex) {
                log.error("Error while inferring type of component {} of type {}", componentInfo.id, componentInfo.component.getClass().getSimpleName());
            }
        }
        return inputFieldMap;
    }

    /**
     * Build the types for an input grid
     *
     * @param inputFieldMap the mapping from field name to instruction
     * @param clazz         the class of the entities in the grid
     * @param entityModel   entity model used to do the binding
     */
    private static void buildTypesForGrid(Map<String, String> inputFieldMap, Class<?> clazz,
                                          EntityModel<?> entityModel) {
        for (Field field : clazz.getDeclaredFields()) {
            AttributeModel am = entityModel.getAttributeModel(field.getName());
            if (field.getType().getSimpleName().equalsIgnoreCase("Date") || field.getType().getSimpleName().equalsIgnoreCase("LocalDate")) {
                inputFieldMap.put(field.getName(), "a date using format 'yyyy-MM-dd'");
            } else if (field.getType().getSimpleName().equalsIgnoreCase("Time") || field.getType().getSimpleName().equalsIgnoreCase("LocalTime")) {
                inputFieldMap.put(field.getName(), "a time using format 'HH:mm:ss'");
            } else if (field.getType().getSimpleName().equalsIgnoreCase("DateTime") || field.getType().getSimpleName().equalsIgnoreCase("LocalDateTime")) {
                inputFieldMap.put(field.getName(), "a date and time using format 'yyyy-MM-ddTHH:mm:ss'");
            } else if (field.getType().getSimpleName().equalsIgnoreCase("Boolean")) {
                inputFieldMap.put(field.getName(), "a Boolean");
            } else if (isNumberField(field)) {
                inputFieldMap.put(field.getName(), "a Number");
            } else if (field.getType().isEnum()) {
                StringJoiner joiner = new StringJoiner("\" OR \"");
                getEnumConstants(am.getType())
                        .forEach(enumValue -> joiner.add(enumValue.toString()));
                inputFieldMap.put(field.getName(), "an enumeration value from one of these options \"" + joiner + "\"");
            } else {
                inputFieldMap.put(field.getName(), "a String");
            }
        }
    }

    /**
     * Get all children from a parent component
     *
     * @param component target
     * @return Stream of all component children
     */
    public static Stream<Component> getAllChildren(Component component) {
        return Stream.concat(
                Stream.of(component),
                component.getChildren().flatMap(FormFillUtils::getAllChildren));
    }


    /**
     * Fills the component(s) of the target component using the map
     * with components and values.
     *
     * @param components         the lis of components
     * @param mapComponentValues Transformed AI module response to a map with components
     *                           and values.
     * @param entityModel        the entity model used to govern the filling process
     */
    @SuppressWarnings("unchecked")
    public static void fillComponents(List<ComponentInfo> components, Map<String, Object> mapComponentValues,
                                      EntityModel<?> entityModel) {

        for (ComponentInfo componentInfo : components) {
            if (componentInfo.component.getId().orElse(null) == null) {
                log.warn("Component has no id so it will be skipped: {}", componentInfo.component);
                continue;
            }
            String id = componentInfo.component.getId().orElse(null);
            try {
                if (id != null) {
                    Object responseValue = mapComponentValues.get(id);
                    if (responseValue == null) {
                        log.warn("No response value found for component: {}", id);
                        continue;
                    }

                    AttributeModel attributeModel = entityModel.getAttributeModel(componentInfo.id);

                    if (componentInfo.component instanceof DetailsEditGrid detailsEditGrid) {
                        fillDetailsGrid(componentInfo, responseValue, attributeModel, detailsEditGrid);
                    } else if (componentInfo.component instanceof TextField textField) {
                        textField.setValue(responseValue.toString());
                    } else if (componentInfo.component instanceof TextArea textArea) {
                        textArea.setValue(responseValue.toString());
                    } else if (componentInfo.component instanceof NumberField numberField) {
                        numberField.setValue(Double.valueOf(responseValue.toString()));
                    } else if (componentInfo.component instanceof BigDecimalField bdField) {
                        bdField.setValue(BigDecimal.valueOf(Double.parseDouble(responseValue.toString())));
                    } else if (componentInfo.component instanceof IntegerField integerField) {
                        integerField.setValue(Integer.valueOf(responseValue.toString()));
                    } else if (componentInfo.component instanceof EmailField emailField) {
                        emailField.setValue(responseValue.toString());
                    } else if (componentInfo.component instanceof PasswordField passwordField) {
                        passwordField.setValue(responseValue.toString());
                    } else if (componentInfo.component instanceof DatePicker datePicker) {
                        datePicker.setValue(LocalDate.parse(responseValue.toString()));
                    } else if (componentInfo.component instanceof TimePicker timePicker) {
                        timePicker.setValue(LocalTime.parse(responseValue.toString()));
                    } else if (componentInfo.component instanceof DateTimePicker datetimePicker) {
                        datetimePicker.setValue(LocalDateTime.parse(responseValue.toString()));
                    } else if (componentInfo.component instanceof ComboBox comboBox) {
                        if (attributeModel.getType().isEnum()) {
                            getEnumConstants(attributeModel.getType()).filter(val -> val.toString().equals(
                                            responseValue.toString().toUpperCase())).
                                    findFirst().ifPresent(comboBox::setValue);
                        } else if (comboBox.isAllowCustomValue()) {
                            comboBox.setValue(responseValue);
                        } else {
                            Stream<?> items = comboBox.getGenericDataView().getItems();
                            if (items.toList().contains(responseValue)) {
                                comboBox.setValue(responseValue);
                            }
                        }
                    } else if (componentInfo.component instanceof MultiSelectComboBox<?>) {
                        MultiSelectComboBox<String> multiSelectComboBox = (MultiSelectComboBox<String>) componentInfo.component;
                        try {
                            ArrayList<String> list = (ArrayList<String>) responseValue;
                            Set<String> set = new HashSet<>(list);
                            if (multiSelectComboBox.isAllowCustomValue()) {
                                multiSelectComboBox.setValue(set);
                            } else {
                                multiSelectComboBox.setValue(set
                                        .stream()
                                        .filter(multiSelectComboBox.getGenericDataView().getItems().toList()::contains)
                                        .collect(Collectors.toSet()));
                            }
                        } catch (Exception e) {
                            log.error("Error while updating multiSelectComboBox with id: {}", id, e);
                        }
                    } else if (componentInfo.component instanceof Checkbox checkbox) {
                        checkbox.setValue((Boolean) responseValue);
                    } else if (componentInfo.component instanceof CheckboxGroup<?>) {
                        CheckboxGroup<String> checkboxGroup = (CheckboxGroup<String>) componentInfo.component;
                        try {
                            ArrayList<String> list = (ArrayList<String>) responseValue;
                            Set<String> set = new HashSet<>(list);
                            checkboxGroup.setValue(set);
                        } catch (Exception e) {
                            log.error("Error while updating checkbox group with id: {}", id, e);
                        }
                    } else if (componentInfo.component instanceof RadioButtonGroup<?>) {
                        RadioButtonGroup<String> radioButtonGroup = (RadioButtonGroup<String>) componentInfo.component;
                        radioButtonGroup.setValue(responseValue.toString());
                    } else if (componentInfo.component instanceof QuickAddEntityComboBox field) {
                        if (!fillBasedOnAttributeModel(responseValue, attributeModel, field, false)) {
                            field.getComboBox().getListDataView().getItems()
                                    .filter(item -> item.toString().contains(responseValue.toString()))
                                    .findFirst()
                                    .ifPresent(val -> field.setValue((AbstractEntity<?>) val));
                        }

                    } else if (componentInfo.component instanceof QuickAddTokenSelect field) {
                        if (!fillBasedOnAttributeModel(responseValue, attributeModel, field, true)) {
                            field.getTokenSelect().getListDataView().getItems()
                                    .filter(item -> item.toString().contains(responseValue.toString()))
                                    .findFirst()
                                    .ifPresent(val -> field.setValue(List.of((AbstractEntity<?>) val)));
                        }
                    } else if (componentInfo.component instanceof QuickAddListSingleSelect field) {
                        if (!fillBasedOnAttributeModel(responseValue, attributeModel, field, false)) {
                            field.getListSelect().getListDataView().getItems()
                                    .filter(item -> item.toString().contains(responseValue.toString()))
                                    .findFirst()
                                    .ifPresent(field::setValue);
                        }
                    } else if (componentInfo.component instanceof EntityLookupField cb) {
                        boolean collection = attributeModel.getAttributeType() == AttributeType.DETAIL;
                        // for entity lookup field, there is no list of values to iterate over
                        fillBasedOnAttributeModel(responseValue, attributeModel, cb, collection);
                    } else if (componentInfo.component instanceof HasValue<?, ?>) {
                        // Fallback to work even if it is not handled here but has HasValue
                        HasValue<?, String> hasValue = (HasValue<?, String>) componentInfo.component;
                        hasValue.setValue(responseValue.toString());
                    } else
                        log.warn("Component type not supported: {}", componentInfo.component.getClass().getSimpleName());
                }
            } catch (Exception e) {
                log.error("Error while updating component with id: {} Cause: {}", id, e.getMessage());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void fillDetailsGrid(ComponentInfo componentInfo, Object responseValue, AttributeModel attributeModel, DetailsEditGrid detailsEditGrid) {
        Grid<?> grid = detailsEditGrid.getGrid();
        Class<?> clazz = grid.getBeanType() != null ? grid.getBeanType() :
                attributeModel.getNestedEntityModel().getEntityClass();
        try {
            if (responseValue instanceof List) {
                // we can only process the response if it is a list (rather than a map)
                List<Map<String, Object>> items = (List<Map<String, Object>>) responseValue;
                fillDetailsGrid(detailsEditGrid, items, attributeModel.getNestedEntityModel());
            } else {
                log.warn("Response for grid {} could not be interpreted as a list, skipping", componentInfo.id);
            }
        } catch (RuntimeException ex) {
            log.error("Error while updating details grid for type {} because {}", clazz.getSimpleName(), ex.getMessage());
        }
    }

    /**
     * Tries to set the value of an entity field by looking up an entity with a matching
     * property value in the database
     *
     * @param responseValue  the response
     * @param attributeModel the attribute model for the field
     * @param component      the component to update
     * @param collection     whether the component accepts a collection of values
     * @param <ID>           type parameter, type of the entity
     * @param <T>            type parameter, ID of the entity
     * @return true if a value could be set, false otherwise
     */
    @SuppressWarnings({"unchecked"})
    private static <ID extends Serializable, T extends AbstractEntity<ID>> boolean fillBasedOnAttributeModel(Object
                                                                                                                     responseValue, AttributeModel attributeModel, QuickAddEntityField<ID, T, T> component,
                                                                                                             boolean collection) {
        if (attributeModel != null) {
            Class<?> clazz = attributeModel.getNestedEntityModel().getEntityClass();
            String displayProperty = attributeModel.getNestedEntityModel().getDisplayProperty();
            BaseService<ID, T> serviceForEntity = (BaseService<ID, T>) serviceLocator.getServiceForEntity(clazz);

            if (serviceForEntity == null) {
                log.warn("Could not find service for {}", clazz);
                return false;
            }

            if (collection) {
                return fillBasedOnAttributeModel(responseValue, component, displayProperty, serviceForEntity);
            } else {
                List<T> entities = serviceForEntity.find(new Compare.Equal(displayProperty, responseValue));
                if (!entities.isEmpty()) {
                    component.setValue(entities.get(0));
                    return true;
                }

                // look for partial match
                entities = serviceForEntity.find(new Like(displayProperty,
                        "%" + responseValue + "%", false));
                if (!entities.isEmpty()) {
                    component.setValue(entities.get(0));
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Tries to fill an entity field that holds a collection by looking up an entity with a matching
     * * property value in the database
     *
     * @param responseValue    the AI response
     * @param component        the component to update
     * @param displayProperty  the display property of the entity
     * @param serviceForEntity the service used to query the database
     * @return whether to use the attribute model
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static boolean fillBasedOnAttributeModel(Object responseValue, QuickAddEntityField component, String
            displayProperty, BaseService<?, ?> serviceForEntity) {
        List values = new ArrayList();
        if (responseValue instanceof List) {
            values = (List) responseValue;
        } else if (responseValue instanceof String) {
            values = List.of(responseValue.toString().split(","));
        }

        List resultList = new ArrayList();
        values.forEach(value -> {
            List<?> objects = serviceForEntity.find(new Compare.Equal(displayProperty, value.toString().trim()));
            if (!objects.isEmpty()) {
                resultList.add(objects.get(0));
            } else {
                // look for partial match
                objects = serviceForEntity.find(new Like(displayProperty,
                        "%" + value.toString().trim() + "%", false));
                if (!objects.isEmpty()) {
                    resultList.add(objects.get(0));
                }
            }
        });
        if (!resultList.isEmpty()) {
            component.setValue(resultList);
            return true;
        }
        return false;
    }

    /**
     * Tries to fill the values inside an edit grid. This is
     *
     * @param grid        the grid to fill
     * @param items       the nested items (as they are returned from the AI service)
     * @param entityModel the entity model for the entities that are managed by the grid
     * @param <ID>        type parameter, ID of the entity
     * @param <T>         type parameter, type of the entity
     */
    private static <ID extends Serializable, T extends AbstractEntity<ID>> void fillDetailsGrid
    (DetailsEditGrid<ID, T> grid, List<Map<String, Object>> items, EntityModel<?> entityModel) {
        if (items == null) {
            log.warn("Items list is null. Skipping the update for the grid");
            return;
        }

        List<T> gridItems = items.stream().map(itemMap -> {
            // create and bind new entity
            T item = grid.getCreateEntity().get();

            for (Map.Entry<String, Object> entry : itemMap.entrySet()) {
                String propName = entry.getKey();
                Object propValue = entry.getValue();
                Object updatedValue = propValue;
                AttributeModel am = entityModel.getAttributeModel(propName);

                if (am != null && am.getType().isEnum()) {
                    updatedValue = getEnumConstants(am.getType())
                            .filter(val -> val.toString().equals(propValue))
                            .findFirst().orElse(null);
                } else if (am != null && am.getAttributeType() == AttributeType.MASTER) {
                    updatedValue = findEntityForDetailsGrid(propValue, am);
                }

                try {
                    ClassUtils.setFieldValue(item, propName, updatedValue);
                } catch (Exception ex) {
                    log.error("Failed to set field value for '{}': {}", propName, ex.getMessage());
                }
            }
            return item;
        }).toList();

        grid.setValue(gridItems);
    }

    @SuppressWarnings("unchecked")
    private static <ID extends Serializable, T extends AbstractEntity<ID>> T findEntityForDetailsGrid(Object propValue, AttributeModel am) {
        BaseService<ID, T> serviceForEntity = (BaseService<ID, T>) serviceLocator.getServiceForEntity(
                am.getNestedEntityModel().getEntityClass()
        );

        String displayProperty = am.getNestedEntityModel().getDisplayProperty();
        List<T> entities = serviceForEntity.find(new Compare.Equal(am.getNestedEntityModel().getDisplayProperty(),
                propValue));
        if (!entities.isEmpty()) {
            return entities.get(0);
        }

        // look for partial match
        entities = serviceForEntity.find(new Like(displayProperty,
                "%" + propValue + "%", false));
        if (!entities.isEmpty()) {
            return entities.get(0);
        }
        return null;
    }


    public static boolean isSupportedComponent(Component component) {
        return supportedComponentStream().anyMatch(c -> c.equals(component.getClass()));
    }

    private static Stream<Class<? extends Component>> supportedComponentStream() {
        return Stream.of(
                TextField.class,
                TextArea.class,
                NumberField.class,
                BigDecimalField.class,
                IntegerField.class,
                EmailField.class,
                PasswordField.class,
                DatePicker.class,
                TimePicker.class,
                DateTimePicker.class,
                ComboBox.class,
                Checkbox.class,
                CheckboxGroup.class,
                RadioButtonGroup.class,
                MultiSelectComboBox.class,
                QuickAddEntityComboBox.class,
                QuickAddTokenSelect.class,
                QuickAddListSingleSelect.class,
                EntityLookupField.class,
                DetailsEditGrid.class
        );
    }

    private static boolean isReadOnly(Component component) {
        if (component instanceof HasValue<?, ?>) {
            return ((HasValue<?, ?>) component).isReadOnly();
        }
        return component.getElement().getProperty("readonly", false);
    }

    private static boolean isSupportedAndAccepted(Component component) {
        try {
            if (!component.isVisible()) {
                return false;
            }
            if (component instanceof HasEnabled && !((HasEnabled) component).isEnabled()) {
                return false;
            }
            if (isReadOnly(component)) {
                return false;
            }
            if (!isSupportedComponent(component)) {
                return false;
            } else if (component.getId().isEmpty()) {
                log.warn("Component of type {} has no id. Remember to add a meaningful" +
                        " id to the component if you want to fill it with the FromFiller", component.getClass().getSimpleName());
                return false;
            }
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    @SuppressWarnings("rawtypes")
    private static Stream<? extends Enum> getEnumConstants(Class<?> clazz) {
        return Arrays.stream(clazz.asSubclass(Enum.class).getEnumConstants());
    }

    private static boolean isNumberField(Field field) {
        return field.getType().getSimpleName().equalsIgnoreCase("Integer") || field.getType().getSimpleName().equalsIgnoreCase("Long")
                || field.getType().getSimpleName().equalsIgnoreCase("Double") || field.getType().getSimpleName().equalsIgnoreCase("Float")
                || field.getType().getSimpleName().equalsIgnoreCase("BigDecimal");
    }
}

