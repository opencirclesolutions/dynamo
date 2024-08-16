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

import java.io.Serializable;
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Primary key object for versioned entities - key consists of the key of the
 * original entity plus the revision number
 * 
 * @author bas.rutten
 *
 * @param <ID>
 */
@ToString
@Getter
@Setter
public class RevisionKey<ID> implements Serializable {

	private static final long serialVersionUID = -1151671376478031983L;

	private int revision;

	private ID id;

	/**
	 * Constructor
	 * 
	 * @param id       the ID of the original entity
	 * @param revision the revision number
	 */
	public RevisionKey(ID id, int revision) {
		this.id = id;
		this.revision = revision;
	}

	@Override
	public int hashCode() {
		return id.hashCode() + Objects.hashCode(revision);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean equals(Object obj) {
		if (!(obj instanceof RevisionKey)) {
			return false;
		}
		RevisionKey<ID> other = (RevisionKey<ID>) obj;
		return Objects.equals(this.id, other.id) && Objects.equals(this.revision, other.revision);
	}
}
