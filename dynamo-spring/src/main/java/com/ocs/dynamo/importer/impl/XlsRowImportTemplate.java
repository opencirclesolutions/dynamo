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
package com.ocs.dynamo.importer.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.ocs.dynamo.exception.OCSImportException;
import com.ocs.dynamo.importer.dto.AbstractDTO;
import com.ocs.dynamo.service.MessageService;

/**
 * A template for processing an Excel file that contains row-based data and transforming it into a
 * collection of DTOs
 * 
 * @author bas.rutten
 *
 * @param <ID>
 *            the type of the key of the DTO
 * @param <T>
 *            the type of the DTO
 */
public abstract class XlsRowImportTemplate<ID, T extends AbstractDTO> {

	/**
	 * The importer
	 */
	private BaseXlsImporter importer;

	/**
	 * Index of the column (zero based) from which to read the values
	 */
	private int colIndex;

	/**
	 * Byte representation of the files
	 */
	private byte[] bytes;

	/**
	 * Index of the sheet from which to read the values
	 */
	private int sheetIndex;

	/**
	 * Whether to check for duplicates
	 */
	private boolean checkForDuplicates;

	/**
	 * List of errors/warnings that have occurred so far
	 */
	private List<String> errors;

	/**
	 * Index of the first row from which to start reading
	 */
	private int firstRowNumber;

	/**
	 * Length (number of rows) in a single record
	 */
	private int recordLength;

	/**
	 * Keys of the records processed so far
	 */
	private Set<ID> keys = new HashSet<ID>();

	/**
	 * The message service
	 */
	private MessageService messageService;

	/**
	 * The class of the DTO object to create
	 */
	private Class<T> clazz;

	/**
	 * Constructor
	 * 
	 * @param importer
	 *            the XLS importer that is used to do the actual reading from the file
	 * @param messageService
	 * @param bytes
	 * @param errors
	 * @param clazz
	 * @param sheetIndex
	 * @param firstRowNumber
	 * @param colIndex
	 * @param recordLength
	 * @param checkForDuplicates
	 */
	public XlsRowImportTemplate(BaseXlsImporter importer, MessageService messageService, byte[] bytes,
	        List<String> errors, Class<T> clazz, int sheetIndex, int firstRowNumber, int colIndex, int recordLength,
	        boolean checkForDuplicates) {
		this.messageService = messageService;
		this.importer = importer;
		this.bytes = bytes;
		this.sheetIndex = sheetIndex;
		this.errors = errors;
		this.checkForDuplicates = checkForDuplicates;
		this.firstRowNumber = firstRowNumber;
		this.colIndex = colIndex;
		this.clazz = clazz;
		this.recordLength = recordLength;
	}

	@SuppressWarnings("unchecked")
	public List<T> execute() throws IOException {

		List<T> results = new ArrayList<>();

		try (Workbook wb = importer.createWorkbook(bytes)) {
			Sheet sheet = wb.getSheetAt(sheetIndex);

			int i = 0;

			while (i <= sheet.getLastRowNum()) {
				if (i >= firstRowNumber) {
					try {
						// check for non-empty separator row
						if (importer.isRowEmpty(sheet.getRow(i))) {
							break;
						} else {
							i++;
						}

						T t = importer.processRows(sheet, i, colIndex, clazz);

						if (t != null && extractKey(t) != null) {
							ID key = extractKey(t);

							// in case of a string, compare by lower case
							if (key instanceof String) {
								key = (ID) ((String) key).toLowerCase();
							}

							if (checkForDuplicates) {
								if (!keys.contains(key)) {
									keys.add(key);
									results.add(t);
								} else {
									errors.add(messageService.getMessage("ocs.duplicate.row", i + 1, key));
								}
							} else {
								results.add(t);
							}
						}
					} catch (OCSImportException ex) {
						// catch errors on a record by record level
						errors.add(String.format("Row %d: %s", i + 1, ex.getMessage()));
					}
					i += recordLength;
				} else {
					// row is before the start of the input, skip row and try the next
					i++;
				}
			}

			return results;
		}
	}

	/**
	 * Retrieves the key value from a record (used for duplicate checking)
	 * 
	 * @param record
	 *            the record
	 * @return the value of the key
	 */
	protected abstract ID extractKey(T record);

}
