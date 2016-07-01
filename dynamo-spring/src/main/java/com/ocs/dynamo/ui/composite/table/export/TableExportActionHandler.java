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
package com.ocs.dynamo.ui.composite.table.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.ui.ServiceLocator;
import com.ocs.dynamo.ui.composite.table.ModelBasedTreeTable;
import com.ocs.dynamo.ui.composite.table.TableUtils;
import com.ocs.dynamo.utils.StringUtil;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.Grid.HeaderRow;
import com.vaadin.ui.Table;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.UI;

/**
 * Action Handler which adds export functionality to a table.
 * 
 * @author Patrick Deenen
 */
public class TableExportActionHandler implements Handler {

    private static final Logger LOG = Logger.getLogger(TableExportActionHandler.class);

    /**
     * @author bas.rutten
     */
    class ModelExcelExport extends ExcelExport {

        private static final long serialVersionUID = 5811530790417796915L;

        /**
         * Cell style for BigDecimal percentage fields
         */
        private CellStyle bigDecimalPercentageStyle;

        /**
         * Cell style for BigDecimal fields
         */
        private CellStyle bigDecimalStyle;

        /**
         * Cell style for integer fields
         */
        private CellStyle integerStyle;

        /**
         * Cell style for normal (text) fields
         */
        private CellStyle normal;

        /**
         * Constructor
         * 
         * @param table
         */
        ModelExcelExport(Table table) {
            super(table, new XSSFWorkbook(), messageService.getMessage("ocs.export"),
                    TableExportActionHandler.this.reportTitle, null,
                    TableExportActionHandler.this.totalsRow);

            DataFormat format = workbook.createDataFormat();

            // create the cell styles only once- this is a huge performance
            // gain!
            integerStyle = workbook.createCellStyle();
            integerStyle.setAlignment(CellStyle.ALIGN_RIGHT);
            setBorder(integerStyle, CellStyle.BORDER_THIN);
            integerStyle.setDataFormat(format.getFormat("#,#"));

            bigDecimalStyle = workbook.createCellStyle();
            bigDecimalStyle.setAlignment(CellStyle.ALIGN_RIGHT);
            setBorder(bigDecimalStyle, CellStyle.BORDER_THIN);
            bigDecimalStyle.setDataFormat(format.getFormat("#,##0.00"));

            bigDecimalPercentageStyle = workbook.createCellStyle();
            bigDecimalPercentageStyle.setAlignment(CellStyle.ALIGN_RIGHT);
            setBorder(bigDecimalPercentageStyle, CellStyle.BORDER_THIN);
            bigDecimalPercentageStyle.setDataFormat(format.getFormat("#,##0.00%"));

            normal = workbook.createCellStyle();
            normal.setAlignment(CellStyle.ALIGN_LEFT);
            setBorder(normal, CellStyle.BORDER_THIN);
        }

        /**
         * Sets a certain border for a cell style
         * 
         * @param style
         *            the cell style
         * @param border
         *            the border type
         */
        private void setBorder(CellStyle style, short border) {
            style.setBorderBottom(border);
            style.setBorderTop(border);
            style.setBorderLeft(border);
            style.setBorderRight(border);
        }

        /**
         * Overruled from parent class to support model based formatting.
         */
        @Override
        protected void addDataRow(Sheet sheetToAddTo, Object rootItemId, int row) {
            final Row sheetRow = sheetToAddTo.createRow(row);
            Property<?> prop;
            Object propId;
            Object value;
            Cell sheetCell;

            List<Object> props = getPropIds();

            // look up the item once (much faster!)
            Item item = getTableHolder().getContainerDataSource().getItem(rootItemId);
            for (int col = 0; col < props.size(); col++) {
                propId = props.get(col);
                prop = getProperty(item, rootItemId, propId);
                value = prop == null ? null : prop.getValue();

                sheetCell = sheetRow.createCell(col);

                CellStyle custom = null;
                CellStyle standard = normal;

                AttributeModel am = findAttributeModel(propId);
                if (value != null) {

                    // custom formatting for certain cells
                    if (cellStyleGenerator != null) {
                        custom = cellStyleGenerator.getCustomCellStyle(workbook, propId, value, am);
                    }

                    // for numbers we do not use the default formatting since
                    // that would produce strings and we
                    // want actual numerical values
                    if (value instanceof Integer) {
                        sheetCell.setCellValue(((Integer) value).doubleValue());
                        standard = integerStyle;
                    } else if (value instanceof BigDecimal) {
                        boolean isPercentage = am != null && am.isPercentage();
                        if (isPercentage) {
                            // percentages in the application are just numbers,
                            // but in Excel they are fractions that
                            // are displayed as percentages -> so, divide by 100
                            double temp = ((BigDecimal) value)
                                    .divide(HUNDRED, 10, RoundingMode.HALF_UP)
                                    .setScale(am.getPrecision() + 2, RoundingMode.HALF_UP)
                                    .doubleValue();
                            sheetCell.setCellValue(temp);
                            standard = bigDecimalPercentageStyle;
                        } else {
                            // just display as a number
                            sheetCell.setCellValue(((BigDecimal) value).setScale(SCALE,
                                    RoundingMode.HALF_UP).doubleValue());
                            standard = bigDecimalStyle;
                        }
                    } else if (am != null) {
                        // if it's an actual model attribute, then defer to the
                        // normal formatting functionality

                        // replace all HTML breaks
                        if (value instanceof String) {
                            value = StringUtil.replaceHtmlBreaks((String) value);
                        }

                        // if there is only one entity model, use that one.
                        EntityModel<?> onlyModel = entityModels.size() == 1 ? entityModels.get(0)
                                : null;
                        sheetCell.setCellValue(TableUtils.formatPropertyValue(entityModelFactory,
                                onlyModel != null ? onlyModel : am.getEntityModel(),
                                messageService, propId, value));
                    } else {
                        // if everything else fails, use the string
                        // representation
                        String v = value == null ? "" : value.toString();
                        v = StringUtil.replaceHtmlBreaks(v);
                        sheetCell.setCellValue(v);
                    }
                }

                if (custom != null) {
                    sheetCell.setCellStyle(custom);
                } else if (standard != null) {
                    sheetCell.setCellStyle(standard);
                }
            }
        }

        /**
         * Add row recursively - overwritten to provide true unlimited recursion, rather than just
         * one level
         * 
         * @param sheetToAddTo
         * @param rootItemId
         * @param row
         * @return
         */
        private int addDataRowRecursively(final Sheet sheetToAddTo, final Object rootItemId,
                final int row) {
            int numberAdded = 0;
            int localRow = row;

            // add the row itself
            addDataRow(sheetToAddTo, rootItemId, row);
            numberAdded++;

            // iterate over the children
            if (((Container.Hierarchical) getTableHolder().getContainerDataSource())
                    .hasChildren(rootItemId)) {
                final Collection<?> children = ((Container.Hierarchical) getTableHolder()
                        .getContainerDataSource()).getChildren(rootItemId);

                int childCount = 0;
                for (final Object child : children) {

                    // process recursively
                    childCount = addDataRowRecursively(sheetToAddTo, child, localRow + 1);

                    // increment the row counter by the number of added rows
                    // (remember we already incremented it by one)
                    localRow += childCount;
                    numberAdded += childCount;
                }

                if (displayTotals) {
                    addDataRow(hierarchicalTotalsSheet, rootItemId, localRow);
                }

                // apply grouping
                if (numberAdded > 1) {
                    sheet.groupRow(row + 1, (row + numberAdded) - 1);
                    sheet.setRowGroupCollapsed(row + 1, true);
                }
            }
            return numberAdded;
        }

        /**
         * 
         */
        @Override
        protected int addHierarchicalDataRows(final Sheet sheetToAddTo, final int row) {
            final Collection<?> roots;
            int localRow = row;
            roots = ((Container.Hierarchical) getTableHolder().getContainerDataSource())
                    .rootItemIds();
            /*
             * For Hierarchical Containers, the outlining/grouping in the sheet is with the summary
             * row at the top and the grouped/outlined subcategories below.
             */
            sheet.setRowSumsBelow(false);
            int count = 0;
            for (final Object rootId : roots) {
                count = addDataRowRecursively(sheetToAddTo, rootId, localRow);
                localRow = localRow + count;
            }
            return localRow;
        }

        /**
         * Adds a total rows to the bottom of the sheet - overwritten so that the columns can be
         * property aligned
         */
        @Override
        protected void addTotalsRow(final int currentRow, final int startRow) {
            totalsRow = sheet.createRow(currentRow);
            totalsRow.setHeightInPoints(30);
            Cell cell;
            CellRangeAddress cra;
            for (int col = 0; col < getPropIds().size(); col++) {
                final Object propId = getPropIds().get(col);
                cell = totalsRow.createCell(col);
                cell.setCellStyle(getCellStyle(currentRow, startRow, col, true));
                final Short poiAlignment = getTableHolder().getCellAlignment(propId);
                CellUtil.setAlignment(cell, workbook, poiAlignment);
                final Class<?> propType = getPropertyType(propId);
                if (isNumeric(propType)) {

                    if (Integer.class.equals(propType)) {
                        cell.setCellStyle(integerStyle);
                    } else if (BigDecimal.class.equals(propType)) {
                        cell.setCellStyle(bigDecimalStyle);
                    }

                    cra = new CellRangeAddress(startRow, currentRow - 1, col, col);
                    if (isHierarchical()) {
                        // 9 & 109 are for sum. 9 means include hidden cells,
                        // 109 means exclude.
                        // this will show the wrong value if the user expands an
                        // outlined category, so
                        // we will range value it first
                        cell.setCellFormula("SUM("
                                + cra.formatAsString(hierarchicalTotalsSheet.getSheetName(), true)
                                + ")");
                    } else {
                        cell.setCellFormula("SUM(" + cra.formatAsString() + ")");
                    }
                } else {
                    if (0 == col) {
                        cell.setCellValue(createHelper.createRichTextString(messageService
                                .getMessage("ocs.total")));
                    }
                }
            }
        }

        /**
         * Looks up an attribute model in the list of available models
         * 
         * @param propId
         * @return
         */
        protected AttributeModel findAttributeModel(Object propId) {
            if (entityModels != null) {
                for (EntityModel<?> em : entityModels) {
                    AttributeModel am = em.getAttributeModel(propId.toString());
                    if (am != null) {
                        return am;
                    }
                }
            }
            return null;
        }

        /**
         * 
         * @param item
         * @param rootItemId
         * @param propId
         * @return
         */
        protected Property<?> getProperty(Item item, Object rootItemId, Object propId) {
            Property<?> prop;
            if (getTableHolder().isGeneratedColumn(propId)) {
                prop = getTableHolder().getPropertyForGeneratedColumn(propId, rootItemId);
            } else {
                prop = item.getItemProperty(propId);
                if (useTableFormatPropertyValue && getTableHolder().isExportableFormattedProperty()) {
                    prop = getPropertyInner(rootItemId, propId, prop);
                }

            }
            return prop;
        }

        /**
         * @param rootItemId
         * @param propId
         * @param prop
         * @return
         */
        private Property<?> getPropertyInner(Object rootItemId, Object propId, Property<?> prop) {
            final String formattedProp = getTableHolder().getFormattedPropertyValue(rootItemId,
                    propId, prop);
            Property<?> result = null;
            if (prop == null || prop.getValue() == null) {
                // property not found, use the formatted value instead
                result = new ObjectProperty<String>(formattedProp, String.class);
            } else {
                final Object val = prop.getValue();
                if (!val.toString().equals(formattedProp)) {
                    result = new ObjectProperty<String>(formattedProp, String.class);
                }
            }
            return result;
        }

        /**
         * Reintroduced method because it is private in the parent class
         * 
         * @param propId
         * @return
         */
        private Class<?> getPropertyType(final Object propId) {
            Class<?> classType;
            if (getTableHolder().isGeneratedColumn(propId)) {
                classType = getTableHolder().getPropertyTypeForGeneratedColumn(propId);
            } else {
                classType = getTableHolder().getContainerDataSource().getType(propId);
            }
            return classType;
        }

        /**
         * Returns the property IDs that are to be included in the sheet. Overwritten so that grid
         * properties are displayed in the correct order
         */
        @Override
        public List<Object> getPropIds() {
            if (columnIds != null) {
                List<Object> result = new ArrayList<>();
                for (String s : columnIds) {
                    result.add(s);
                }
                return result;
            }

            return super.getPropIds();
        }

        /**
         * Is the property a numeric property
         * 
         * @param propType
         * @return
         */
        private boolean isNumeric(Class<?> propType) {
            return Number.class.isAssignableFrom(propType);
        }

        /**
         * Overwritten so we can use the correct mime type and make sure the UI is not null
         */
        @Override
        public boolean sendConverted() {
            File tempFile = null;
            FileOutputStream fileOut = null;
            try {
                tempFile = File.createTempFile("tmp", ".xlsx");
                fileOut = new FileOutputStream(tempFile);
                workbook.write(fileOut);
                if (null == mimeType) {
                    setMimeType(MIME_TYPE);
                }
                UI tableUI = getTableHolder().getUI();

                return super.sendConvertedFileToUser(tableUI != null ? tableUI : ui, tempFile,
                        exportFileName);
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
                return false;
            } finally {
                if (tempFile != null) {
                    tempFile.deleteOnExit();
                }
                try {
                    if (fileOut != null) {
                        fileOut.close();
                    }
                } catch (final IOException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
    }

    private static final BigDecimal HUNDRED = new BigDecimal(100);

    private static final String MIME_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private static final int SCALE = 5;

    private static final long serialVersionUID = -276477801970861419L;

    private Action actionExport;

    private List<String> columnIds;

    private EntityModelFactory entityModelFactory;

    private List<EntityModel<?>> entityModels;

    private MessageService messageService;

    private String reportTitle;

    private boolean totalsRow;

    private UI ui;

    private CustomCellStyleGenerator cellStyleGenerator;

    /**
     * Constructor (for a model based export)
     * 
     * @param ui
     * @param entityModelFactory
     * @param entityModels
     * @param messageService
     * @param reportTitle
     * @param columnIds
     * @param totalsRow
     *            whether to include a totals row
     * @param cellStyleGenerator
     *            custom cell style generator (for making changes to the exported file)
     */
    public TableExportActionHandler(UI ui, EntityModelFactory entityModelFactory,
            List<EntityModel<?>> entityModels, MessageService messageService, String reportTitle,
            List<String> columnIds, boolean totalsRow, CustomCellStyleGenerator cellStyleGenerator) {
        this(ui, messageService, columnIds, reportTitle, totalsRow, cellStyleGenerator);
        this.entityModelFactory = entityModelFactory;
        this.entityModels = entityModels;
    }

    /**
     * Constructor
     * 
     * @param ui
     *            the current UI object
     * @param messageService
     *            the message service
     * @param columnIds
     *            the IDs of the columsn to export
     * @param reportTitle
     *            the title of the report
     * @param totalsRow
     */
    public TableExportActionHandler(UI ui, MessageService messageService, List<String> columnIds,
            String reportTitle, boolean totalsRow, CustomCellStyleGenerator cellStyleGenerator) {
        this.messageService = messageService;
        this.columnIds = columnIds;
        this.cellStyleGenerator = cellStyleGenerator;

        // try to get message from message bundle
        this.reportTitle = messageService.getMessageNoDefault(reportTitle);
        if (this.reportTitle == null) {
            // if that fails, use the title itself
            this.reportTitle = reportTitle;
        }
        this.ui = ui;
        this.totalsRow = totalsRow;
        actionExport = new Action(messageService.getMessage("ocs.export"));
    }

    /**
     * Export a grid - this is achieved by wrapping the data source from the grid in a table
     * 
     * @param grid
     */
    public void exportFromGrid(Grid grid) {
        Table table = new Table();

        table.setContainerDataSource(grid.getContainerDataSource());

        // copy header captions from grid to table
        for (Column c : grid.getColumns()) {
            Object oid = c.getPropertyId();

            String caption = "";
            for (int i = 0; i < grid.getHeaderRowCount(); i++) {
                HeaderRow r = grid.getHeaderRow(i);

                if (caption.length() > 0) {
                    caption += " ";
                }

                try {
                    caption += r.getCell(oid).getText();
                } catch (Exception ex) {
                    // if it is not text, then it is HTML (very ugly, but seems
                    // to
                    // be the only way)
                    caption += r.getCell(oid).getHtml();
                }
            }
            // replace HTML line breaks by space
            if (caption != null) {
                caption = caption.replaceAll("<\\w+/>", " ");
            }

            table.setColumnHeader(c.getPropertyId(), caption);
        }
        handleAction(actionExport, table, null);
    }

    /**
     * 
     */
    @Override
    public Action[] getActions(Object target, Object sender) {
        return new Action[] { actionExport };
    }

    /**
     * Handles the action
     * 
     * @param action
     *            the action
     * @param sender
     *            the component that sent the request (the table or the grid)
     * @param target
     *            the target of the action - not used in this case
     */
    @Override
    public void handleAction(Action action, Object sender, Object target) {

        if (action == actionExport && sender != null && sender instanceof Table) {

            TableExportService service = ServiceLocator.getService(TableExportService.class);

            ExcelExport export = null;
            export = new ModelExcelExport((Table) sender);
            export.setReportTitle(reportTitle);
            export.setRowHeaders(((Table) sender).getVisibleColumns().length > 1);
            if (sender instanceof TreeTable) {
                export.getTableHolder().setHierarchical(true);
                // a model based tree table has multiple levels - the built-in
                // totals calculation doesn't work in that case
                export.setDisplayTotals(!(sender instanceof ModelBasedTreeTable));
            }

            String fcd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            export.setExportFileName((reportTitle + " " + fcd + ".xlsx").replace(' ', '_'));

            // the original code uses the mime type for Excel 2003, this is
            // not what we want
            export.setMimeType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            service.export(export);
        }
    }

}
