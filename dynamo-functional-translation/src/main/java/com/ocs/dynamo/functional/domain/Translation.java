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

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EditableType;
import com.ocs.dynamo.domain.model.VisibilityType;
import com.ocs.dynamo.domain.model.annotation.Attribute;
import com.ocs.dynamo.domain.model.annotation.Model;
import com.ocs.dynamo.utils.StringUtils;
import org.hibernate.annotations.DiscriminatorOptions;

/**
 * @author Patrick Deenen
 * 
 *         The persistent class for the translation database table.
 * 
 */
@Entity
@Table(name = "translation")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
@Model(displayProperty = "fullDescription")
@DiscriminatorOptions(force=true)
public abstract class Translation<E> extends AbstractEntity<Integer> {

	private static final long serialVersionUID = 3155835503400960383L;

	@Id
	@SequenceGenerator(name = "translation_id_seq", sequenceName = "translation_id_seq")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "translation_id_seq")
	private Integer id;

	@Column(insertable = false, updatable = false)
	@Attribute(visible = VisibilityType.HIDE)
	private String type;

	@NotNull
	@Attribute(visible = VisibilityType.HIDE)
	private String field;

	// uni-directional many-to-one association to Domain
	@NotNull
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "locale")
	@Attribute(showInTable = VisibilityType.SHOW)
	private Locale locale;

	@NotNull
	@Attribute(showInTable = VisibilityType.SHOW)
	private String translation;

	@Override
	public Integer getId() {
		return this.id;
	}

	@Override
	public void setId(Integer id) {
		this.id = id;
	}

	public String getType() {
		return this.type;
	}

	public String getField() {
		return this.field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public Locale getLocale() {
		return this.locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public String getTranslation() {
		return this.translation;
	}

	public void setTranslation(String translation) {
		this.translation = translation;
	}

	@NotNull
	@Attribute(visible = VisibilityType.HIDE)
	public abstract E getEntity();

	public abstract void setEntity(E entity);

	@Attribute(editable = EditableType.READ_ONLY, visible = VisibilityType.HIDE)
	public String getFullDescription() {
		return StringUtils.camelCaseToHumanFriendly(getField(), false) + " - " + getLocale().getName() + " - "
				+ getTranslation();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof Translation)) {
			return false;
		}
		Translation<?> other = (Translation<?>) obj;
		if (this.id == null || other.id == null) {
			return false;
		}
		return this.id.equals(other.id);
	}

}
