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
package org.dynamoframework.importer.impl;

import org.apache.commons.io.input.CharSequenceReader;
import org.dynamoframework.exception.OCSImportException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for Importers that can be used to import fixed length files
 * 
 * @author bas.rutten
 */
public class BaseFixedLengthImporter extends BaseTextImporter {

	/**
	 * Counts the number of rows in the file
	 * 
	 * @param bytes
	 *            the byte content of the file
	 * @param sheetIndex
	 *            the index of the sheet (ignored)
	 */
	@Override
	public int countRows(byte[] bytes, int sheetIndex) {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)))) {
			int count = 0;

			String line = reader.readLine();
			while (line != null) {
				count++;
				line = reader.readLine();
			}
			return count;
		} catch (IOException ex) {
			throw new OCSImportException(ex.getMessage(), ex);
		}
	}

	/**
	 * Parses the comma-separated list of field lengths
	 * 
	 * @param fieldLengths
	 * @return
	 */
	protected List<Integer> parseFieldLengths(String fieldLengths) {
		try {
			String[] lengths = fieldLengths.split(",");
			List<Integer> result = new ArrayList<>();

			for (String s : lengths) {
				Integer len = Integer.valueOf(s);
				result.add(len);
			}

			return result;
		} catch (NumberFormatException ex) {
			throw new OCSImportException("Invalid field length entered", ex);
		}
	}

	/**
	 * Reads a byte array into a CSV file
	 * 
	 * @param bytes
	 *            the raw representation of the file
	 * @param fieldLengths
	 *            the field lengths
	 * @return
	 * @throws IOException
	 */
	protected List<String[]> readFixedLengthFile(byte[] bytes, List<Integer> fieldLengths) {
		try (BufferedReader reader = new BufferedReader(
		        new CharSequenceReader(new String(bytes, StandardCharsets.UTF_8)))) {
			List<String[]> result = new ArrayList<>();

			String line = reader.readLine();
			while (line != null) {
				List<String> temp = new ArrayList<>();
				int start = 0;
				for (Integer len : fieldLengths) {
					if (start + len <= line.length()) {
						// there is space
						String field = line.substring(start, start + len);
						temp.add(field.trim());
					} else if (start <= line.length()) {
						String field = line.substring(start, line.length());
						temp.add(field.trim());
					}
					start += len;
				}

				result.add(temp.toArray(new String[0]));
				line = reader.readLine();
			}
			return result;
		} catch (IOException ex) {
			throw new OCSImportException(ex.getMessage(), ex);
		}
	}
}
