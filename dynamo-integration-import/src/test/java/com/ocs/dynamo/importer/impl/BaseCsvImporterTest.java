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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.ocs.dynamo.exception.OCSImportException;
import com.ocs.dynamo.importer.impl.PersonDTO.Gender;
import com.ocs.dynamo.utils.DateUtils;

@Disabled
public class BaseCsvImporterTest {

	BaseCsvImporter importer = new BaseCsvImporter();

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
		byte[] bytes = readFile("importertest.csv");
		List<String[]> lines = importer.readCsvFile(bytes, ";", "'");

		PersonDTO dto = importer.processRow(0, lines.get(0), PersonDTO.class);
		assertNotNull(dto);
		assertEquals("Bas", dto.getName());
		assertEquals(1, dto.getNumber().intValue());
		assertEquals(2.4, dto.getFactor().doubleValue(), 0.001);
		assertEquals("abc", dto.getRandom());
		assertEquals(Gender.M, dto.getGender());
		assertEquals(1.50, dto.getPercentage().doubleValue(), 0.001);
		assertEquals(6.66, dto.getRating().doubleValue(), 0.001);

		// check that default values are set
		dto = importer.processRow(1, lines.get(1), PersonDTO.class);
		assertNotNull(dto);
		assertEquals("Unknown", dto.getName());
		assertEquals(2, dto.getNumber().intValue());
		assertEquals(1.0, dto.getFactor().doubleValue(), 0.001);

		// check negative values and default dates
		dto = importer.processRow(1, lines.get(2), PersonDTO.class);
		assertNotNull(dto);
		assertEquals("Endy", dto.getName());
		assertEquals(-3, dto.getNumber().intValue());
		assertEquals(DateUtils.createLocalDate("01012015"), dto.getDate());
	}

	@Test
	public void testReadFile_NotNumeric() throws IOException {
		try {
			byte[] bytes = readFile("importertest_wrongnumeric.csv");
			List<String[]> lines = importer.readCsvFile(bytes, ";", "'");

			importer.processRow(0, lines.get(0), PersonDTO.class);
			fail();
		} catch (OCSImportException ex) {
			assertEquals("abc cannot be converted to a number", ex.getMessage());
		}
	}

	private byte[] readFile(String fileName) throws IOException {
		return FileUtils.readFileToByteArray(new File("src/test/resources/" + fileName));
	}
}
