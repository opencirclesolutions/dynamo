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
package com.ocs.dynamo.functional.domain;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.VisibilityType;
import com.ocs.dynamo.domain.model.annotation.Attribute;
import com.ocs.dynamo.domain.model.annotation.Model;

/**
 * Base class for reference information.
 * 
 * @author Patrick Deenen (patrick@opencircle.solutions)
 *
 */
@Inheritance
@DiscriminatorColumn(name = "TYPE")
@Entity
@Model(displayProperty = "name", sortOrder = "name asc")
public abstract class Domain extends AbstractEntity<Integer> {

    public static final String NAME = "name";

    public static final String CODE = "code";

    private static final long serialVersionUID = 1598343469161718498L;

    @Id
    @SequenceGenerator(name = "DOMAIN_ID_GENERATOR", sequenceName = "DOMAIN_ID_SEQ")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DOMAIN_ID_GENERATOR")
    private Integer id;

    /**
     * By default, we only use "name" so the code is hidden
     */
    @Attribute(visible = VisibilityType.HIDE)
    private String code;

    @NotNull
    @Attribute(main = true, maxLength = 60)
    private String name;

    public Domain() {
    }

    /**
     * Constructor
     * 
     * @param code
     * @param name
     */
    public Domain(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        return ObjectUtils.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Domain)) {
            return false;
        }

        if (!this.getClass().equals(obj.getClass())) {
            return false;
        }

        Domain other = (Domain) obj;
        if (this.id != null && other.id != null) {
            // first, check if the IDs match
            return ObjectUtils.equals(this.id, other.id);
        } else {
            // if this is not the case, check for code and type
            return ObjectUtils.equals(this.code, other.code);
        }

    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toStringExclude(this, "parent");
    }
}
