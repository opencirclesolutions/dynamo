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
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.input.CharSequenceReader;

import com.ocs.dynamo.exception.OCSImportException;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

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
    public int countRows(byte[] bytes, int sheetIndex) {
        List<String[]> lines = readCsvFile(bytes, SystemPropertyUtils.getCsvSeparator(), SystemPropertyUtils.getCsvQuoteChar());
        return lines.size();
    }

    /**
     * Reads a CSV file into a List of String arrays
     * 
     * @param bytes     the raw content of the CSV file
     * @param separator the record separator
     * @param quote     the quote char
     * @return
     */
    protected List<String[]> readCsvFile(byte[] bytes, String separator, String quote) {
        try (CharSequenceReader seq = new CharSequenceReader(new String(bytes, StandardCharsets.UTF_8));
                CSVReader reader = new CSVReaderBuilder(seq)
                        .withCSVParser(new CSVParserBuilder().withSeparator(separator.charAt(0)).withQuoteChar(quote.charAt(0)).build())
                        .build()) {
            return reader.readAll();
        } catch (IOException | CsvException ex) {
            throw new OCSImportException(ex.getMessage(), ex);
        }
    }

}
