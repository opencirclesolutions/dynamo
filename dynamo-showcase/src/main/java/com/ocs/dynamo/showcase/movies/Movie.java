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

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeDateType;
import com.ocs.dynamo.domain.model.AttributeSelectMode;
import com.ocs.dynamo.domain.model.VisibilityType;
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

    @NotNull
    @Attribute(searchable = true, main = true)
    private String title;

    /** The Movie IMDB URL contains a Dynamo annotation to make it clickable. */
    @Attribute(url = true)
    @Column(name = "IMDB_URL")
    private String imdbUrl;

    /**
     * The Movie Release Date contains a Dynamo annotation to make it searchable and indicating it's
     * a date only.
     */
    @Attribute(searchable = true, dateType = AttributeDateType.DATE)
    @Column(name = "RELEASE_DATE")
    private Date releaseDate;

    /**
     * The Movie Kids Genre contains a Dynamo annotation to make it searchable but not sortable and
     * to apply another label than the default.
     */
    @Attribute(searchable = true, sortable = false, displayName = "MPAA Rating")
    @Enumerated(EnumType.STRING)
    @Column(name = "MPAA_RATING")
    private MovieRating rating;

    /**
     * The Movie Gross contains a Dynamo annotation to make it searchable and use another value
     * representation.
     */
    @NotNull
    private BigDecimal gross;

    /**
     * Display the JPA Element collection in a table and make it editable.
     */
    @Attribute(showInTable = VisibilityType.SHOW, complexEditable = true)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "TAG")
    @Column(name = "Name")
    private Set<String> tags;

    /**
     * Make the documentary classification searchable and to use another value representation than
     * the default.
     */
    @Attribute(searchable = true, trueRepresentation = "Yep!", falseRepresentation = "Nope!")
    private Boolean documentary;

    /**
     * Display the country in a table and create a lookup table for it.
     */
    @Attribute(complexEditable = true, searchable = true, selectMode = AttributeSelectMode.LOOKUP, displayName = "Country", showInTable = VisibilityType.SHOW)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "COUNTRY_ID")
    private Country country;

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
     * @return the rating
     */
    public MovieRating getRating() {
        return rating;
    }

    /**
     * @param rating
     *            the rating to set
     */
    public void setRating(MovieRating rating) {
        this.rating = rating;
    }

    /**
     * @return the gross
     */
    public BigDecimal getGross() {
        return gross;
    }

    /**
     * @param gross
     *            the gross to set
     */
    public void setGross(BigDecimal gross) {
        this.gross = gross;
    }

    /**
     * @return the tags
     */
    public Set<String> getTags() {
        return tags;
    }

    /**
     * @param tags
     *            the tags to set
     */
    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    /**
     * @return the documentary
     */
    public Boolean getDocumentary() {
        return documentary;
    }

    /**
     * @param documentary
     *            the documentary to set
     */
    public void setDocumentary(Boolean documentary) {
        this.documentary = documentary;
    }

    /**
     * @return the country
     */
    public Country getCountry() {
        return country;
    }

    /**
     * @param country
     *            the country to set
     */
    public void setCountry(Country country) {
        this.country = country;
    }

    /**
     * @return the imdbUrl
     */
    public String getImdbUrl() {
        return imdbUrl;
    }

    /**
     * @param imdbUrl
     *            the imdbUrl to set
     */
    public void setImdbUrl(String imdbUrl) {
        this.imdbUrl = imdbUrl;
    }

}
