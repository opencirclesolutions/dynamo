package nl.ocs.importer.impl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.input.CharSequenceReader;

import nl.ocs.constants.OCSConstants;
import nl.ocs.exception.OCSImportException;

/**
 * Base class for Importers that can be used to import fixed length files
 * 
 * @author bas.rutten
 *
 */
public class BaseFixedLengthImporter extends BaseTextImporter {

	/**
	 * Counts the number of rows in the file
	 * 
	 * @param bytes
	 *            the content of the file
	 * @param row
	 *            ignored for this type of file
	 * @param column
	 *            ignored for this type of file
	 */
	@Override
	public int countRows(byte[] bytes, int row, int column) {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				new ByteArrayInputStream(bytes)))) {
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
	 * @return
	 * @throws IOException
	 */
	protected List<String[]> readFixedLengthFile(byte[] bytes, List<Integer> fieldLengths) {
		try (BufferedReader reader = new BufferedReader(new CharSequenceReader(new String(bytes,
				OCSConstants.UTF_8)))) {
			List<String[]> result = new ArrayList<String[]>();

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
