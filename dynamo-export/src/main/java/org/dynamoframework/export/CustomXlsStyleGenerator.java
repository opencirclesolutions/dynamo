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
package org.dynamoframework.export;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;
import org.dynamoframework.domain.AbstractEntity;
import org.dynamoframework.domain.model.AttributeModel;

import java.io.Serializable;

/**
 * Interface for a custom Excel style generator
 * 
 * @author bas.rutten
 *
 * @param <ID> the type of the primary key
 * @param <T>  the type of the entity
 */
@FunctionalInterface
public interface CustomXlsStyleGenerator<ID extends Serializable, T extends AbstractEntity<ID>> {

	/**
	 * Returns the cell style for a certain cell
	 * 
	 * @param workbook the workbook
	 * @param entity   the entity that is displayed in the row
	 * @param value    the cell value
	 * @param am       the attribute model. Can be empty
	 * @return the desired style
	 */
	CellStyle getCustomCellStyle(Workbook workbook, T entity, Object value, AttributeModel am, Object pivotColumnKey);
}
