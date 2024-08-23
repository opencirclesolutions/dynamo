package org.dynamoframework.export;

/*-
 * #%L
 * Dynamo Framework
 * %%
 * Copyright (C) 2014 - 2024 Open Circle Solutions
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.apache.poi.ss.usermodel.CellStyle;
import org.dynamoframework.domain.AbstractEntity;
import org.dynamoframework.domain.model.AttributeModel;

import java.io.Serializable;

/**
 * An interface for an Excel style generator to be used during export of data
 * sets to Excel
 * 
 * @author bas.rutten
 *
 * @param <ID> the type of the primary key
 * @param <T>  the type of the entity
 */
public interface XlsStyleGenerator<ID extends Serializable, T extends AbstractEntity<ID>> {

	/**
	 * Returns the style to use for a cell in the header row
	 * 
	 * @param index the index of the column
	 * @return
	 */
	CellStyle getHeaderStyle(int index);

	/**
	 * Returns the cell style for a certain cell
	 * 
	 * @param index  the column index of the cell
	 * @param entity the entity for which a value is displayed in the cell
	 * @param value  the value of the cell
	 * @param am     the attribute model
	 * @return
	 */
	CellStyle getCellStyle(int index, T entity, Object value, AttributeModel am);

	/**
	 * 
	 * @param type
	 * @param am
	 * @return
	 */
	CellStyle getTotalsStyle(Class<?> type, AttributeModel am);

}
