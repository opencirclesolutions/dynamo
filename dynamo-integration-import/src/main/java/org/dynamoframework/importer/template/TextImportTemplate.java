package org.dynamoframework.importer.template;

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

import lombok.extern.slf4j.Slf4j;
import org.dynamoframework.exception.OCSImportException;
import org.dynamoframework.service.MessageService;
import org.dynamoframework.configuration.DynamoPropertiesHolder;

import java.util.*;

/**
 * Template class for importing data from a text (CSV or fixed width) file and
 * translating it to an entity
 *
 * @param <ID> the type of the ID of the entity
 * @param <T>  the type of the entity
 * @author bas.rutten
 */
@Slf4j
public abstract class TextImportTemplate<ID, T> {

	private final List<String[]> lines;

	private final boolean checkForDuplicates;

	private final List<String> errors;

	private final MessageService messageService;

	private final Set<ID> keys = new HashSet<>();

	/**
	 * Constructor
	 *
	 * @param lines              the lines that make up the text file
	 * @param errors             the errors that have occurred so far
	 * @param checkForDuplicates whether to check for duplicate rows
	 */
	protected TextImportTemplate(MessageService messageService, List<String[]> lines, List<String> errors,
								 boolean checkForDuplicates) {
		this.lines = lines;
		this.errors = errors;
		this.checkForDuplicates = checkForDuplicates;
		this.messageService = messageService;
	}

	/**
	 * Indicates whether the row is appropriate and can be processed
	 *
	 * @param row the representation of the row
	 * @return true if this is the case, false otherwise
	 */
	protected abstract boolean isAppropriateRow(String[] row);

	/**
	 * Processes a row
	 *
	 * @param rowNum the row number of the row
	 * @param row    the row (as an array of strings denoting the individual field
	 *               values)
	 * @return the result of the processing
	 */
	protected abstract T process(int rowNum, String[] row);

	/**
	 * Extracts a primary key value from a row
	 *
	 * @param row the row
	 * @return the key value
	 */
	protected abstract ID getKeyFromRow(T row);

	/**
	 * Executes the template
	 *
	 * @return the result of the processing
	 */
	public List<T> execute() {
		List<T> results = new ArrayList<>();
		for (int i = 0; i < lines.size(); i++) {
			String[] row = lines.get(i);
			if (row != null && i > 0 && isAppropriateRow(row)) {
				try {
					executeRow(i, row, results);
				} catch (OCSImportException ex) {
					log.error(ex.getMessage(), ex);
					// catch errors on a record by record level
					errors.add(String.format("Row %d: %s", i + 1, ex.getMessage()));
				}
			}
		}

		return results;
	}

	/**
	 * Processes a single row
	 *
	 * @param i       the index of the row
	 * @param row     the field values that together from the row
	 * @param results the list of current results
	 */
	@SuppressWarnings("unchecked")
	private void executeRow(int i, String[] row, List<T> results) {
		T t = process(i, row);
		ID key = getKeyFromRow(t);

		if (checkForDuplicates) {

			// lower case comparison for Strings
			if (key instanceof String) {
				key = (ID) ((String) key).toLowerCase();
			}

			if (!keys.contains(key)) {
				keys.add(key);
				results.add(t);
			} else {
				errors.add(messageService.getMessage("dynamoframework.duplicate.row", Locale.US, i + 1, key));
			}
		} else {
			results.add(t);
		}
	}
}
