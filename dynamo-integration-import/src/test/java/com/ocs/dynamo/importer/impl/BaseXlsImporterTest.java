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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.ocs.dynamo.exception.OCSImportException;
import com.ocs.dynamo.importer.impl.PersonDTO.Gender;
import com.ocs.dynamo.utils.DateUtils;

@Disabled
public class BaseXlsImporterTest {

	private BaseXlsImporter importer = new BaseXlsImporter();

	/**
	 * Test that a simple row count works
	 * 
	 * @throws IOException
	 */
	@Test
	public void testCountRows() throws IOException {
		byte[] bytes = readFile("importertest.xlsx");
		int rows = importer.countRows(bytes, 0);
		assertEquals(7, rows);
	}

	/**
	 * Test that creating a simple (non-streaming) workbook works
	 * 
	 * @throws IOException
	 */
	@Test
	public void testCreateWorkbookBoolean() throws IOException {
		byte[] bytes = readFile("importertest_boolean.xlsx");
		Workbook wb = importer.createWorkbook(bytes);

		Sheet sheet = wb.getSheetAt(0);

		// check true
		PersonDTO dto = importer.processRow(0, sheet.getRow(0), PersonDTO.class);
		assertNotNull(dto);
		assertTrue(dto.getAbool());

		// check false
		dto = importer.processRow(1, sheet.getRow(1), PersonDTO.class);
		assertNotNull(dto);
		assertFalse(dto.getAbool());

		// check default
		dto = importer.processRow(6, sheet.getRow(6), PersonDTO.class);
		assertNotNull(dto);
		assertFalse(dto.getAbool());

	}

	/**
	 * Test that creating a simple (non-streaming) workbook works
	 * 
	 * @throws IOException
	 */
	@Test
	public void testCreateWorkbook() throws IOException {
		byte[] bytes = readFile("importertest.xlsx");
		Workbook wb = importer.createWorkbook(bytes);

		Sheet sheet = wb.getSheetAt(0);

		PersonDTO dto = importer.processRow(0, sheet.getRow(0), PersonDTO.class);
		assertNotNull(dto);
		assertEquals(0, dto.getRowNum());
		assertEquals("Bas", dto.getName());
		assertEquals(1, dto.getNumber().intValue());
		assertEquals(2.4, dto.getFactor().doubleValue(), 0.001);
		assertEquals("abc", dto.getRandom());
		assertEquals(Gender.M, dto.getGender());
		assertEquals(1.50, dto.getPercentage().doubleValue(), 0.001);
		assertTrue(dto.getAbool());
		assertEquals(DateUtils.createLocalDate("04042014"), dto.getDate());
		assertEquals(6.66, dto.getRating().doubleValue(), 0.001);

		// check that default values are set
		dto = importer.processRow(1, sheet.getRow(1), PersonDTO.class);
		assertNotNull(dto);
		assertEquals(1, dto.getRowNum());
		assertEquals("Unknown", dto.getName());
		assertEquals(2, dto.getNumber().intValue());
		assertEquals(1.0, dto.getFactor().doubleValue(), 0.001);
		assertFalse(dto.getAbool());
		assertEquals(DateUtils.createLocalDate("05052015"), dto.getDate());

		// check negative values
		dto = importer.processRow(2, sheet.getRow(2), PersonDTO.class);
		assertNotNull(dto);
		assertEquals(2, dto.getRowNum());
		assertEquals("Endy", dto.getName());
		assertEquals(-3, dto.getNumber().intValue());
		assertEquals(DateUtils.createLocalDate("01012015"), dto.getDate());
	}

	/**
	 * @throws IOException
	 */
	@Test
	public void testCreateWorkbook_WrongNumericValue() throws IOException {
		byte[] bytes = readFile("importertest_wrongnumeric.xlsx");
		Workbook wb = importer.createWorkbook(bytes);

		Sheet sheet = wb.getSheetAt(0);

		try {
			importer.processRow(0, sheet.getRow(0), PersonDTO.class);
		} catch (OCSImportException ex) {
			assertEquals("Found an invalid numeric value: xyz", ex.getMessage());
		}

	}

	/**
	 * Test that an exception is raised if a row doesn't have enough columns
	 * 
	 * @throws IOException
	 */
	@Test
	public void testCreateWorkbook_NotEnoughRows() throws IOException {
		byte[] bytes = readFile("importertest _notenoughrows.xlsx");
		Workbook wb = importer.createWorkbook(bytes);

		Sheet sheet = wb.getSheetAt(0);

		try {
			importer.processRow(0, sheet.getRow(0), PersonDTO.class);
		} catch (OCSImportException ex) {
			assertEquals("Row doesn't have enough columns", ex.getMessage());
		}

	}

	/**
	 * Test that an exception occurs if a value for a required field is missing
	 * 
	 * @throws IOException
	 */
	@Test
	public void testCreateWorkbook_RequiredValueMissing() throws IOException {
		byte[] bytes = readFile("importertest_required_missing.xlsx");
		Workbook wb = importer.createWorkbook(bytes);

		Sheet sheet = wb.getSheetAt(0);

		try {
			importer.processRow(0, sheet.getRow(1), PersonDTO.class);
		} catch (OCSImportException ex) {
			assertEquals("Required value for field 'number' is missing", ex.getMessage());
		}
	}

	/**
	 * Test that an exception occurs if an illegal negative value is found
	 * 
	 * @throws IOException
	 */
	@Test
	public void testCreateWorkbook_CannotBeNegative() throws IOException {
		byte[] bytes = readFile("importertest_notnegative.xlsx");
		Workbook wb = importer.createWorkbook(bytes);

		Sheet sheet = wb.getSheetAt(0);

		try {
			importer.processRow(0, sheet.getRow(1), PersonDTO.class);
		} catch (OCSImportException ex) {
			assertEquals("Negative value -1.2 found for field 'factor'", ex.getMessage());
		}
	}

	/**
	 * Test that creating a simple (non-streaming) workbook works
	 * 
	 * @throws IOException
	 */
	@Test
	public void testCreateReader() throws IOException {
		byte[] bytes = readFile("importertest.xlsx");
		Workbook wb = importer.createReader(bytes, 100);

		// "hasNext" has to be called to actually populate the iterator
		Iterator<Row> it = wb.getSheetAt(0).iterator();
		assertTrue(it.hasNext());

		PersonDTO dto = importer.processRow(0, it.next(), PersonDTO.class);
		assertNotNull(dto);
		assertEquals("Bas", dto.getName());
		assertEquals(1, dto.getNumber().intValue());
		assertEquals(2.4, dto.getFactor().doubleValue(), 0.001);
		assertEquals("abc", dto.getRandom());

		// check that default values are set
		dto = importer.processRow(1, it.next(), PersonDTO.class);
		assertNotNull(dto);
		assertEquals("Unknown", dto.getName());
		assertEquals(2, dto.getNumber().intValue());
		assertEquals(1.0, dto.getFactor().doubleValue(), 0.001);

		// check negative values
		dto = importer.processRow(1, it.next(), PersonDTO.class);
		assertNotNull(dto);
		assertEquals("Endy", dto.getName());
		assertEquals(-3, dto.getNumber().intValue());

	}

	/**
	 * Test whether a certain row contains a cell with a certain string value
	 * 
	 * @throws IOException
	 */
	@Test
	public void testContainsStringValue() throws IOException {
		byte[] bytes = readFile("importertest.xlsx");
		Workbook wb = importer.createWorkbook(bytes);

		Sheet sheet = wb.getSheetAt(0);

		assertTrue(importer.containsStringValue(sheet.getRow(0), "Bas"));
		assertFalse(importer.containsStringValue(sheet.getRow(0), "Bob"));
		assertFalse(importer.containsStringValue(null, "Bas"));
	}

	@Test
	public void testIsRowEmpty() throws IOException {
		assertTrue(importer.isRowEmpty(null));

		byte[] bytes = readFile("importertest.xlsx");
		Workbook wb = importer.createWorkbook(bytes);

		Sheet sheet = wb.getSheetAt(0);
		assertFalse(importer.isRowEmpty(sheet.getRow(0)));

		assertTrue(importer.isRowEmpty(sheet.getRow(7)));
	}

	private byte[] readFile(String fileName) throws IOException {
		return FileUtils.readFileToByteArray(new File("src/test/resources/" + fileName));
	}

}
