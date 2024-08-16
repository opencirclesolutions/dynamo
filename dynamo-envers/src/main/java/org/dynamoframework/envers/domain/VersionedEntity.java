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
package org.dynamoframework.envers.domain;

import java.time.ZonedDateTime;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.annotation.Attribute;

import lombok.Getter;
import lombok.Setter;

/**
 * A versioned entity. This is a wrapper around the snapshot of the original
 * entity and adds some additional properties related to auditing (e.g. revision
 * number, time stamp of change, user responsible for the save)
 * 
 * @author bas.rutten
 *
 * @param <ID> the type of the ID of the entity
 * @param <T>  the type of the entity
 */
@Getter
@Setter
public abstract class VersionedEntity<ID, T extends AbstractEntity<ID>> extends AbstractEntity<RevisionKey<ID>> {

	private static final long serialVersionUID = 4784364096429184957L;

	/**
	 * The Envers revision number
	 */
	@Attribute(sortable = true, displayName = "Revision number")
	private int revision;

	/**
	 * The entity
	 */
	@Attribute(sortable = false)
	private T entity;

	/**
	 * The revision time stamp
	 */
	@Attribute(sortable = true)
	private ZonedDateTime revisionTimeStamp;

	/**
	 * The revision user
	 */
	@Attribute(sortable = true)
	private String user;

	private RevisionKey<ID> id;

	@Attribute(sortable = true)
	private RevisionType revisionType;

	/**
	 * Constructor
	 * 
	 * @param entity   the original entity
	 * @param revision the revision number
	 */
	protected VersionedEntity(T entity, int revision) {
		this.entity = entity;
		this.revision = revision;
		this.id = new RevisionKey<>(entity.getId(), revision);
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
