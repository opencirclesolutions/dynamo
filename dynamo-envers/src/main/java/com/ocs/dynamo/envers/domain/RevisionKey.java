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

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;

/**
 * Primary key object for versioned entities - key consists of the key of the
 * original entity plus the revision number
 * 
 * @author bas.rutten
 *
 * @param <ID>
 */
public class RevisionKey<ID> implements Serializable {

	private static final long serialVersionUID = -1151671376478031983L;

	private int revision;

	private ID id;

	/**
	 * Constructor
	 * 
	 * @param id
	 *            the ID of the original entity
	 * @param revision
	 *            the revision number
	 */
	public RevisionKey(ID id, int revision) {
		this.id = id;
		this.revision = revision;
	}

	public int getRevision() {
		return revision;
	}

	public void setRevision(int revision) {
		this.revision = revision;
	}

	public ID getId() {
		return id;
	}

	public void setId(ID id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		return id.hashCode() + ObjectUtils.hashCode(revision);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean equals(Object obj) {
		if (!(obj instanceof RevisionKey)) {
			return false;
		}
		RevisionKey<ID> other = (RevisionKey<ID>) obj;
		return ObjectUtils.equals(this.id, other.id) && ObjectUtils.equals(this.revision, other.revision);
	}
}
