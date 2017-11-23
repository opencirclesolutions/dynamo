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
package com.ocs.dynamo.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.ocs.dynamo.domain.model.VisibilityType;
import com.ocs.dynamo.domain.model.annotation.Attribute;

/**
 * Base class for entities that store auditing information
 * 
 * @author bas.rutten
 * @param <ID>
 *            type of the primary key
 */
@MappedSuperclass
public abstract class AbstractAuditableEntity<ID> extends AbstractEntity<ID> {

	private static final long serialVersionUID = 3347137920794563022L;

	@Attribute(readOnly = true)
	@Column(name = "created_by")
	private String createdBy;

	@Attribute(readOnly = true)
	@Column(name = "created_on")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdOn;

	@Attribute(readOnly = true, showInTable = VisibilityType.HIDE)
	@Column(name = "changed_by")
	private String changedBy;

	@Attribute(readOnly = true, showInTable = VisibilityType.HIDE)
	@Column(name = "changed_on")
	@Temporal(TemporalType.TIMESTAMP)
	private Date changedOn;

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public String getChangedBy() {
		return changedBy;
	}

	public void setChangedBy(String changedBy) {
		this.changedBy = changedBy;
	}

	public Date getChangedOn() {
		return changedOn;
	}

	public void setChangedOn(Date changedOn) {
		this.changedOn = changedOn;
	}

}
