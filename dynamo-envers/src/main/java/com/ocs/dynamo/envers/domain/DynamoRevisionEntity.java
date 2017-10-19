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

import java.time.Instant;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.envers.DefaultRevisionEntity;

import com.mysema.query.annotations.QueryExclude;
import com.ocs.dynamo.envers.listener.DynamoRevisionListener;

/**
 * Custom revision entity for use with Envers - stores user name in addition to
 * default fields
 */
@Table(name = "revisionentity")
@Entity
@org.hibernate.envers.RevisionEntity(DynamoRevisionListener.class)
@QueryExclude
public class DynamoRevisionEntity extends DefaultRevisionEntity {

	private static final long serialVersionUID = -5069794800046313271L;

	private String username;

	public String getUsername() {
		return username;
	}

	public void setUsername(final String username) {
		this.username = username;
	}

	@Override
	public String toString() {
		return String.format("DynamoRevisionEntity(id=%d,timestamp=%s,username=%s)", getId(),
				Instant.ofEpochMilli(getTimestamp()).toString(), getUsername());
	}
}
