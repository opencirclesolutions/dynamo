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
package com.ocs.dynamo.importer.template;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.log4j.Logger;

import com.ocs.dynamo.exception.OCSImportException;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.util.SystemPropertyUtils;

/**
 * Template class for importing data from a text (CSV or fixed width) file and translating it to an
 * entity
 * 
 * @author bas.rutten
 * 
 * @param <ID>
 *            the type of the ID of the entity
 * @param <T>
 *            the type of the entity
 */
public abstract class TextImportTemplate<ID, T> {

    private static final Logger LOG = Logger.getLogger(TextImportTemplate.class);

    private List<String[]> lines;

    private boolean checkForDuplicates;

    private List<String> errors;

    private MessageService messageService;

    private Set<ID> keys = new HashSet<>();

    /**
     * Constructor
     * 
     * @param lines
     *            the lines that make up the text file
     * @param errors
     *            the errors that have occurred so far
     * @param checkForDuplicates
     *            whether to check for duplicate rows
     */
    public TextImportTemplate(MessageService messageService, List<String[]> lines, List<String> errors,
            boolean checkForDuplicates) {
        this.lines = lines;
        this.errors = errors;
        this.checkForDuplicates = checkForDuplicates;
        this.messageService = messageService;
    }

    /**
     * Indicates whether the row is appropriate and can be processed
     * 
     * @param line
     * @return
     */
    protected abstract boolean isAppropriateRow(String[] line);

    /**
     * Processes a row
     * 
     * @param rowNum
     *            the row number of the row
     * @param row
     *            the row (as an array of strings denoting the individual field values)
     * @return
     */
    protected abstract T process(int rowNum, String[] row);

    /**
     * 
     * @param t
     * @return
     */
    protected abstract ID getKeyFromRow(T t);

    /**
     * 
     * @return
     */
    public List<T> execute() {
        List<T> results = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String[] row = lines.get(i);
            if (row != null && i > 0 && isAppropriateRow(row)) {
                try {
                    executeRow(i, row, results);
                } catch (OCSImportException ex) {
                    LOG.error(ex.getMessage(), ex);
                    // catch errors on a record by record level
                    errors.add(String.format("Row %d: %s", i + 1, ex.getMessage()));
                }
            }
        }

        return results;
    }

    /**
     * Processes a single row
     * 
     * @param i
     *            the index of the row
     * @param row
     *            the field values that together from the row
     * @param results
     *            the list of current results
     */
    @SuppressWarnings("unchecked")
    private void executeRow(int i, String[] row, List<T> results) {
        T t = process(i, row);
        ID key = getKeyFromRow(t);

        if (checkForDuplicates) {

            // lower case comparison for Strings
            if (key instanceof String) {
                key = (ID) ((String) key).toLowerCase();
            }

            if (!keys.contains(key)) {
                keys.add(key);
                results.add(t);
            } else {
                errors.add(messageService.getMessage("ocs.duplicate.row",
                        new Locale(SystemPropertyUtils.getDefaultDateLocale()), i + 1, key));
            }
        } else {
            results.add(t);
        }
    }
}
