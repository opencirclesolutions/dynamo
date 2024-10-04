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

import org.dynamoframework.importer.impl.BaseCsvImporter;
import org.dynamoframework.importer.impl.BaseTextImporter;
import org.dynamoframework.service.MessageService;
import org.dynamoframework.test.BaseMockitoTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TextImportTemplateTest extends BaseMockitoTest {

	@Mock
	private MessageService messageService;

	private BaseTextImporter importer = new BaseCsvImporter();

	@Test
	public void testBasic() {
		List<String[]> lines = new ArrayList<>();
		lines.add(new String[]{"name", "number"});
		lines.add(new String[]{"1", "Kevin"});
		lines.add(new String[]{"2", "Stuart"});

		List<String> errors = new ArrayList<>();

		TextImportTemplate<Integer, TestDTO> template = new TextImportTemplate<Integer, TestDTO>(messageService, lines,
			errors, false) {

			@Override
			protected TestDTO process(int rowNum, String[] row) {
				return importer.processRow(rowNum, row, TestDTO.class);
			}

			@Override
			protected boolean isAppropriateRow(String[] line) {
				return true;
			}

			@Override
			protected Integer getKeyFromRow(TestDTO t) {
				return null;
			}
		};
		List<TestDTO> result = template.execute();
		assertEquals(2, result.size());
		// duplicate error reported
		assertEquals(0, errors.size());
	}

	@Test
	public void testException() {
		List<String[]> lines = new ArrayList<>();
		lines.add(new String[]{"name", "number"});

		// first value cannot be converted to a number
		lines.add(new String[]{"a", "Kevin"});
		lines.add(new String[]{"2", "Stuart"});

		List<String> errors = new ArrayList<>();

		TextImportTemplate<Integer, TestDTO> template = new TextImportTemplate<Integer, TestDTO>(messageService, lines, errors, false) {

			@Override
			protected TestDTO process(int rowNum, String[] row) {
				return importer.processRow(rowNum, row, TestDTO.class);
			}

			@Override
			protected boolean isAppropriateRow(String[] line) {
				return true;
			}

			@Override
			protected Integer getKeyFromRow(TestDTO t) {
				return null;
			}
		};
		List<TestDTO> result = template.execute();
		assertEquals(1, result.size());
		// conversion error reported
		assertEquals(1, errors.size());
	}

	@Test
	public void testDuplicateCheck() {
		List<String[]> lines = new ArrayList<>();
		lines.add(new String[]{"name", "number"});
		lines.add(new String[]{"1", "Kevin"});
		lines.add(new String[]{"1", "Stuart"});

		List<String> errors = new ArrayList<>();

		TextImportTemplate<Integer, TestDTO> template = new TextImportTemplate<Integer, TestDTO>(messageService, lines, errors, true) {

			@Override
			protected TestDTO process(int rowNum, String[] row) {
				return importer.processRow(rowNum, row, TestDTO.class);
			}

			@Override
			protected boolean isAppropriateRow(String[] line) {
				return true;
			}

			@Override
			protected Integer getKeyFromRow(TestDTO t) {
				return t.getFirst();
			}
		};
		List<TestDTO> result = template.execute();
		assertEquals(1, result.size());

		// duplicate error reported
		assertEquals(1, errors.size());
	}

}
