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
package com.ocs.dynamo.envers.domain;

import java.time.ZonedDateTime;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.annotation.Attribute;

/**
 * A versioned entity. This is a wrapper around the snapshot of the original
 * entity and adds some additional properties related to auditing (e.g. revision
 * number, time stamp of change, user responsible for the save)
 * 
 * @author bas.rutten
 *
 * @param <AbstractEntity>
 */
public abstract class VersionedEntity<ID, T extends AbstractEntity<ID>> extends AbstractEntity<RevisionKey<ID>> {

	private static final long serialVersionUID = 4784364096429184957L;

	@Attribute(sortable = true, displayName = "Revision number")
	private int revision;

	@Attribute(sortable = false)
	private T entity;

	@Attribute(sortable = true)
	private ZonedDateTime revisionTimeStamp;

	@Attribute(sortable = true)
	private String user;

	private RevisionKey<ID> id;

	@Attribute(sortable = true)
	private RevisionType revisionType;

	/**
	 * Constructor
	 * 
	 * @param entity
	 *            the original entity
	 * @param revision
	 *            the revision number
	 */
	public VersionedEntity(T entity, int revision) {
		this.entity = entity;
		this.revision = revision;
		this.id = new RevisionKey<>(entity.getId(), revision);
	}

	public T getEntity() {
		return entity;
	}

	@Override
	public RevisionKey<ID> getId() {
		return id;
	}

	public int getRevision() {
		return revision;
	}

	public ZonedDateTime getRevisionTimeStamp() {
		return revisionTimeStamp;
	}

	public String getUser() {
		return user;
	}

	public void setEntity(T entity) {
		this.entity = entity;
	}

	@Override
	public void setId(RevisionKey<ID> id) {
		this.id = id;
	}

	public void setRevision(int revision) {
		this.revision = revision;
	}

	public void setRevisionTimeStamp(ZonedDateTime revisionTimeStamp) {
		this.revisionTimeStamp = revisionTimeStamp;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String toString() {
		return ReflectionToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public RevisionType getRevisionType() {
		return revisionType;
	}

	public void setRevisionType(RevisionType revisionType) {
		this.revisionType = revisionType;
	}

}
