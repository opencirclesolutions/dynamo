/**
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
package com.ocs.dynamo.domain.model.impl;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.annotation.Attribute;

/**
 * JUnit test for EntityModelImplTest.
 */
public class EntityModelImplTest {

    @SuppressWarnings({ "serial", "unused" })
    private class Entity extends AbstractEntity<Integer> {

        private Integer id;

        @Attribute(required = true)
        private String requiredAttribute;

        @Attribute(searchable = true, requiredForSearching = true)
        private String requiredForSearchingAttribute;

        @Attribute(requiredForSearching = true)
        private String notSearchableAttribute;

        @Override
        public Integer getId() {
            return id;
        }

        @Override
        public void setId(Integer id) {
            this.id = id;
        }

        /**
         * @return the requiredAttribute
         */
        public String getRequiredAttribute() {
            return requiredAttribute;
        }

        /**
         * @param requiredAttribute
         *            the requiredAttribute to set
         */
        public void setRequiredAttribute(String requiredAttribute) {
            this.requiredAttribute = requiredAttribute;
        }

        /**
         * @return the requiredForSearchingAttribute
         */
        public String getRequiredForSearchingAttribute() {
            return requiredForSearchingAttribute;
        }

        /**
         * @param requiredForSearchingAttribute
         *            the requiredForSearchingAttribute to set
         */
        public void setRequiredForSearchingAttribute(String requiredForSearchingAttribute) {
            this.requiredForSearchingAttribute = requiredForSearchingAttribute;
        }

        /**
         * @return the notSearchableAttribute
         */
        public String getNotSearchableAttribute() {
            return notSearchableAttribute;
        }

        /**
         * @param notSearchableAttribute
         *            the notSearchableAttribute to set
         */
        public void setNotSearchableAttribute(String notSearchableAttribute) {
            this.notSearchableAttribute = notSearchableAttribute;
        }
    }

    // Given: JUnit Entity.
    private EntityModel<Entity> subject = new EntityModelFactoryImpl().getModel(Entity.class);

    @Test
    public void getRequiredAttributeModels() {
        // Test: Select required attributes.
        List<AttributeModel> requiredModels = subject.getRequiredAttributeModels();

        // Assert: Only 1 required attribute in entity model.
        assertEquals(1, requiredModels.size());

        // Assert: The required attribute.
        assertEquals("requiredAttribute", requiredModels.get(0).getName());
    }

    @Test
    public void getRequiredForSearchingAttributeModels() {
        // Test: Select required attributes.
        List<AttributeModel> requiredModels = subject.getRequiredForSearchingAttributeModels();

        // Assert: Only 1 required for searching attribute in entity model.
        assertEquals(1, requiredModels.size());

        // Assert: The required attribute.
        assertEquals("requiredForSearchingAttribute", requiredModels.get(0).getName());
    }

}
