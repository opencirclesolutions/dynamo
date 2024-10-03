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

import org.dynamoframework.importer.dto.AbstractDTO;
import org.dynamoframework.importer.impl.BaseTextImporter;
import org.dynamoframework.service.MessageService;

import java.util.List;

/**
 * Default class for importing from text-based files
 *
 * @param <ID> the type of the ID of the entity
 * @param <T>  the type of the entity
 * @author Patrick Deenen (patrick@opencircle.solutions)
 */
public class DefaultTextImportTemplate<ID, T extends AbstractDTO> extends TextImportTemplate<ID, T> {

	private BaseTextImporter importer;

	private Class<T> type;

	/**
	 * Constructor
	 *
	 * @param type
	 * @param messageService
	 * @param importer
	 * @param lines
	 * @param errors
	 */
	public DefaultTextImportTemplate(Class<T> type, MessageService messageService,
									 BaseTextImporter importer, List<String[]> lines, List<String> errors) {
		super(messageService, lines, errors, false);
		this.importer = importer;
		this.type = type;
	}

	@Override
	protected boolean isAppropriateRow(String[] line) {
		return true;
	}

	@Override
	protected ID getKeyFromRow(T row) {
		return null;
	}

	@Override
	protected T process(int rowNum, String[] row) {
		return importer.processRow(rowNum, row, type);
	}
}
