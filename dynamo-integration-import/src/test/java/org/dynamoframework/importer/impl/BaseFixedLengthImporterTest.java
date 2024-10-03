package org.dynamoframework.importer.impl;

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

import org.apache.commons.io.FileUtils;
import org.dynamoframework.exception.OCSImportException;
import org.dynamoframework.importer.impl.PersonDTO.Gender;
import org.dynamoframework.utils.DateUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BaseFixedLengthImporterTest {

	private BaseFixedLengthImporter importer = new BaseFixedLengthImporter();

	@Test
	public void testCountRows() throws IOException {
		byte[] bytes = readFile("importertest.csv");
		int rows = importer.countRows(bytes, 0);
		assertEquals(7, rows);
	}

	/**
	 * Test the import of a (correct) CSV file
	 *
	 * @throws IOException
	 */
	@Test
	public void testReadFile() throws IOException {
		byte[] bytes = readFile("importertest.fixed");
		List<String[]> lines = importer.readFixedLengthFile(bytes,
			List.of(10, 10, 10, 10, 10, 10, 10, 10, 10));

		PersonDTO dto = importer.processRow(0, lines.get(0), PersonDTO.class);
		assertNotNull(dto);
		assertEquals("Bas", dto.getName());
		assertEquals(1, dto.getNumber().intValue());
		assertEquals(2.4, dto.getFactor().doubleValue(), 0.001);
		assertEquals("abc", dto.getRandom());
		assertEquals(Gender.M, dto.getGender());
		assertEquals(1.50, dto.getPercentage().doubleValue(), 0.001);
		assertEquals(DateUtils.createLocalDate("01022016"), dto.getDate());

		// check that default values are set
		dto = importer.processRow(1, lines.get(1), PersonDTO.class);
		assertNotNull(dto);
		assertEquals("Unknown", dto.getName());
		assertEquals(2, dto.getNumber().intValue());
		assertEquals(1.0, dto.getFactor().doubleValue(), 0.001);
		assertEquals(DateUtils.createLocalDate("01022015"), dto.getDate());

		// check negative values
		dto = importer.processRow(1, lines.get(2), PersonDTO.class);
		assertNotNull(dto);
		assertEquals("Endy", dto.getName());
		assertEquals(-3, dto.getNumber().intValue());
	}

	@Test
	public void testParseFieldLengths() {
		List<Integer> list = importer.parseFieldLengths("1,2,3,4");
		assertEquals(List.of(1, 2, 3, 4), list);
	}

	@Test
	public void testParseFieldLengths_Error() {
		try {
			importer.parseFieldLengths("1,a,3,4");
		} catch (OCSImportException ex) {
			assertEquals("Invalid field length entered", ex.getMessage());
		}
	}

	private byte[] readFile(String fileName) throws IOException {
		return FileUtils.readFileToByteArray(new File("src/test/resources/" + fileName));
	}
}
