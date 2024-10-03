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
package org.dynamoframework.envers.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.dynamoframework.dao.BaseDao;
import org.dynamoframework.domain.AbstractEntity;
import org.dynamoframework.envers.domain.RevisionKey;
import org.dynamoframework.envers.domain.VersionedEntity;

/**
 * Interface for Data Access Object for versioned entity
 *
 * @param <ID> the type of the ID of the base entity
 * @param <T>  the type of the base entity
 * @param <U>  the type of the versioned entity
 * @author bas.rutten
 */
public interface VersionedEntityDao<ID, T extends AbstractEntity<ID>, U extends VersionedEntity<ID, T>>
	extends BaseDao<RevisionKey<ID>, U> {

	/**
	 * Returns a list of revisions for an entity
	 *
	 * @param id the ID of the original entity
	 * @return a list of revisions of the entity
	 */
	List<U> findRevisions(ID id);

	/**
	 * Finds the revision number corresponding to a certain date
	 *
	 * @param ldt the date
	 * @return
	 */
	Number findRevisionNumber(LocalDateTime ldt);
}
