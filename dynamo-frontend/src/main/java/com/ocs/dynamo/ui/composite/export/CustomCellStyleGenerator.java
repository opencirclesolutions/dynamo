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
package com.ocs.dynamo.ui.composite.export;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;

import com.ocs.dynamo.domain.model.AttributeModel;

/**
 * A custom cell style generator that can be used to configure the styling of a
 * cell that is generated when exporting the contents of a table or grid to XLS
 * 
 * @author bas.rutten
 *
 */
@FunctionalInterface
public interface CustomCellStyleGenerator {

	/**
	 * Callback method that is called in order to calculate a unique style for a
	 * cell
	 * 
	 * @param workbook       the XLS workbook being created
	 * @param item           the item from the table or the grid
	 * @param rootItemId     the ID fot he item
	 * @param propId         the ID of the property that is displayed in the cell
	 * @param value          the value of the property
	 * @param attributeModel the attribute model that is used to format the property
	 * @return
	 */
	CellStyle getCustomCellStyle(Workbook workbook, Object object, Object rootItemId, Object propId, Object value,
			AttributeModel attributeModel);
}
