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

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionEntity;

import org.dynamoframework.envers.listener.DynamoRevisionListener;
import com.querydsl.core.annotations.QueryExclude;

import lombok.Getter;
import lombok.Setter;

/**
 * Custom revision entity for use with Envers - stores username in addition to
 * default fields
 */
@Table(name = "RevisionEntity")
@Entity
@RevisionEntity(DynamoRevisionListener.class)
@QueryExclude
public class DynamoRevisionEntity extends DefaultRevisionEntity {

	private static final long serialVersionUID = -5069794800046313271L;

	@Getter
	@Setter
	private String username;

	@Override
	public String toString() {
		return String.format("DynamoRevisionEntity(id=%d,timestamp=%s,username=%s)", getId(),
				Instant.ofEpochMilli(getTimestamp()).toString(), getUsername());
	}
}
