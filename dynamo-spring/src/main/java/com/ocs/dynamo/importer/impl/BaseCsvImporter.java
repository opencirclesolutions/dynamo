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
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.input.CharSequenceReader;

import au.com.bytecode.opencsv.CSVReader;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.exception.OCSImportException;

/**
 * Base class for importing CSV files
 * 
 * @author bas.rutten
 */
public class BaseCsvImporter extends BaseTextImporter {

	/**
	 * Counts the number of rows in the file
	 */
	@Override
	public int countRows(byte[] bytes, int row, int column) {
		List<String[]> lines = readCsvFile(bytes, ";", "'");
		return lines.size();
	}

	/**
	 * Reads a byte array into a CSV file
	 * 
	 * @param bytes
	 *            the byte array
	 * @return
	 * @throws IOException
	 */
	protected List<String[]> readCsvFile(byte[] bytes, String separator, String quote) {
		try (CSVReader reader = new CSVReader(new CharSequenceReader(new String(bytes,
		        Charset.forName(DynamoConstants.UTF_8))), separator.charAt(0), quote.charAt(0))) {
			return reader.readAll();
		} catch (IOException ex) {
			throw new OCSImportException(ex.getMessage(), ex);
		}
	}

}
