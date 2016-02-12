package nl.ocs.ui.composite.table.export;

import nl.ocs.domain.model.AttributeModel;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * 
 * @author bas.rutten
 *
 */
public interface CustomCellStyleGenerator {

	/**
	 * Callback method that is called in order to calculate a unique style for a cell
	 * 
	 * @param propId
	 * @param value
	 * @return
	 */
	public CellStyle getCustomCellStyle(Workbook workbook, Object propId, Object value,
			AttributeModel attributeModel);
}
