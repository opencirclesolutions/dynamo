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
package com.ocs.dynamo.showcase.movies;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeDateType;
import com.ocs.dynamo.domain.model.annotation.Attribute;
import com.ocs.dynamo.domain.model.annotation.AttributeOrder;
import com.ocs.dynamo.domain.model.annotation.Model;

/**
 * Movie JPA entity, does not need much explanation.
 */
@Entity
@Table(name = "MOVIE")
// This property will be displayed by default when referring to the entity.
@Model(displayProperty = "title")
// Override the default ordering for attribute placement for the given attributes.
@AttributeOrder(attributeNames = { "id", "title", "releaseDate" })
public class Movie extends AbstractEntity<Integer> {

    /** Classes version. */
    private static final long serialVersionUID = -6342590031746525068L;

    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** The Movie Title contains a Dynamo annotation to make it case-sensitive searchable. */
    @Attribute(searchable = true, searchCaseSensitive = true)
    private String title;

    /**
     * The Movie Release Date contains a Dynamo annotation to make it searchable and indicating it's
     * a date only.
     */
    @Attribute(searchable = true, dateType = AttributeDateType.DATE)
    @Column(name = "RELEASE_DATE")
    private Date releaseDate;

    /**
     * The Movie Action Genre contains a Dynamo annotation to make it searchable and use another
     * value representation.
     */
    @Attribute(searchable = true, trueRepresentation = "Yeah!", falseRepresentation = "Nope...")
    @Column(name = "ACTION")
    private Boolean action;

    /** The Movie Kids Genre contains a Dynamo annotation to make it searchable but not sortable. */
    @Attribute(searchable = true, sortable = false)
    @Column(name = "KIDS")
    private Boolean kids;

    /**
     * The Movie Sci-Fi Genre contains a Dynamo annotation to make it searchable and to use another
     * label than the default.
     */
    @Attribute(searchable = true, displayName = "Sci-fi")
    @Column(name = "SCI_FI")
    private Boolean sciFi;

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title
     *            the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the releaseDate
     */
    public Date getReleaseDate() {
        return releaseDate;
    }

    /**
     * @param releaseDate
     *            the releaseDate to set
     */
    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    /**
     * @return the action
     */
    public Boolean getAction() {
        return action;
    }

    /**
     * @param action
     *            the action to set
     */
    public void setAction(Boolean action) {
        this.action = action;
    }

    /**
     * @return the kids
     */
    public Boolean getKids() {
        return kids;
    }

    /**
     * @param kids
     *            the kids to set
     */
    public void setKids(Boolean kids) {
        this.kids = kids;
    }

    /**
     * @return the sciFi
     */
    public Boolean getSciFi() {
        return sciFi;
    }

    /**
     * @param sciFi
     *            the sciFi to set
     */
    public void setSciFi(Boolean sciFi) {
        this.sciFi = sciFi;
    }
}
