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
package com.ocs.dynamo.ui.composite.grid;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.annotation.Attribute;
import com.ocs.dynamo.domain.model.annotation.Model;
import com.ocs.dynamo.domain.model.annotation.SearchMode;

@Entity
@Model(displayProperty = "name")
public class Person extends AbstractEntity<Integer> {

    private static final long serialVersionUID = 5251680526340104915L;

    @Id
    @GeneratedValue
    private Integer id;

    @Attribute(searchable = SearchMode.ALWAYS)
    private String name;

    private Integer age;

    private BigDecimal weight;

    @Attribute(url = true)
    private String url;

    @Attribute(percentage = true)
    private BigDecimal percentage;

    public Person(Integer id, String name, Integer age, BigDecimal weight, BigDecimal percentage) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.weight = weight;
        this.percentage = percentage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    public void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
