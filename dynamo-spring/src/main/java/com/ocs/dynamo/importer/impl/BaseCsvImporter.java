package com.ocs.dynamo.importer.impl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.input.CharSequenceReader;

import com.ocs.dynamo.constants.OCSConstants;
import com.ocs.dynamo.exception.OCSImportException;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Base class for importing CSV files
 * 
 * @author bas.rutten
 * 
 */
public class BaseCsvImporter extends BaseTextImporter {

	/**
	 * Counts the number of rows in the file
	 */
	@Override
	public int countRows(byte[] bytes, int row, int column) {
		List<String[]> lines = readCsvFile(bytes, ";");
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
	protected List<String[]> readCsvFile(byte[] bytes, String separator) {
		try (CSVReader reader = new CSVReader(new CharSequenceReader(new String(bytes,
				Charset.forName(OCSConstants.UTF_8))), separator.charAt(0))) {
			return reader.readAll();
		} catch (IOException ex) {
			throw new OCSImportException(ex.getMessage(), ex);
		}
	}
}
