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
package com.ocs.dynamo.ui.composite;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.impl.ModelBasedFieldFactory;
import com.ocs.dynamo.jasperreports.JRContainerDataSource;
import com.ocs.dynamo.jasperreports.JRIndexedContainerDataSource;
import com.ocs.dynamo.jasperreports.JRUtils;
import com.ocs.dynamo.ui.composite.layout.BaseCustomComponent;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.jasperreports.ReportGenerator;
import com.ocs.jasperreports.ReportGenerator.Format;
import com.vaadin.data.Container;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Container.Indexed;
import com.vaadin.data.Property;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.server.VaadinSession;
import com.vaadin.server.WrappedHttpSession;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperReport;

/**
 * Custom component to render HTML versions of JasperReports and export to other formats
 * 
 * @author Patrick Deenen (patrick@opencircle.solutions)
 *
 */
public class JRReportViewer<T> extends BaseCustomComponent {

	/**
	 * 
	 * @author bas.rutten
	 *
	 */
	public interface ReportDefinition {
		/**
		 * @return the name of the report template
		 */
		String getReportTemplateName();

		/**
		 * @return whether a direct datasource connection needs to be loaded
		 */
		boolean requiresDatabaseConnection();

		/**
		 * @return whether an external script needs to be loaded
		 */
		boolean requiresExternalScript();
	}

	protected static final String NO_DATA_FOUND_KEY = "ocs.no.data.found";

	protected static final String REPORT_AREA_ID = "reportArea";

	protected static final String REPORT_EXTENSION = ".jasper";

	protected static final String REPORT_NA_KEY = "ocs.report.not.available";

	private static final long serialVersionUID = 6981827314136814213L;
	/**
	 * Indicates whether external script has already been loaded (currently only works for one
	 * external script!)
	 */
	private boolean alreadyLoaded = false;
	private Container container;
	private Map<String, Object> currentParameters;
	private EntityModel<T> entityModel;
	private Button exportPDF;
	private JasperReport jasperReport;
	private JRDataSource jrDataSource;
	private ComponentContainer main;
	private Label reportArea;
	private Enum<? extends ReportDefinition> reportDefinition;
	private ReportGenerator reportGenerator;
	private AbstractSelect reportSelection;
	private CheckBox showMargins;
	private String templatePath;
	private Component toolbar;
	private boolean splitlayout = false;

	/**
	 * Constructor
	 * 
	 * @param reportGenerator
	 * @param reportDefinition
	 * @param entityModel
	 * @param templatePath
	 */
	public JRReportViewer(ReportGenerator reportGenerator, Enum<? extends ReportDefinition> reportDefinition,
	        EntityModel<T> entityModel, String templatePath) {
		this.reportGenerator = reportGenerator;
		this.reportDefinition = reportDefinition;
		this.entityModel = entityModel;
		this.templatePath = templatePath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.ocs.ui.Buildable#build()
	 */
	@Override
	public void build() {
		this.setId(this.getClass().getSimpleName());
		toolbar = buildToolbar();
		reportArea = buildReportArea();

		main = buildMain();
		setCompositionRoot(main);
	}

	protected ComponentContainer buildMain() {
		ComponentContainer main = null;
		if (isSplitlayout()) {
			// Create split layout for reports
			HorizontalSplitPanel sp = new HorizontalSplitPanel(toolbar, reportArea);
			sp.setSizeFull();
			sp.setSplitPosition(30, Unit.PERCENTAGE);
			main = sp;
		} else {
			VerticalLayout vl = new VerticalLayout();
			vl.setMargin(true);
			vl.setSpacing(true);
			vl.addComponent(toolbar);
			vl.addComponent(reportArea);
			main = vl;
		}
		return main;
	}

	protected Component buildExportSelection() {
		// TODO These links should become a drop down menu for multiple formats
		HorizontalLayout exportbar = new HorizontalLayout();
		exportbar.setSpacing(true);
		exportPDF = new Button("Export as PDF");
		exportPDF.setEnabled(false);
		FileDownloader downloader = new FileDownloader(createResourceForExport(Format.PDF));
		downloader.extend(exportPDF);
		exportbar.addComponent(exportPDF);

		// TODO Export in other formats
		return exportbar;
	}

	protected Label buildReportArea() {
		Label label = new Label(getMessageService().getMessage(REPORT_NA_KEY));
		label.setContentMode(ContentMode.HTML);
		label.setId(REPORT_AREA_ID);
		return label;
	}

	protected Component buildReportOptions() {
		showMargins = new CheckBox("Show margins", false);
		return showMargins;
	}

	protected Component buildReportSelection() {
		FormLayout content = new FormLayout();
		content.setSpacing(true);
		// Create reporting selection
		reportSelection = ModelBasedFieldFactory.getInstance(entityModel, getMessageService()).createEnumCombo(
		        reportDefinition.getClass(), ComboBox.class);
		reportSelection.setCaption(getMessageService().getMessage(
		        entityModel.getReference() + "." + reportDefinition.getClass().getSimpleName()));
		reportSelection.setNullSelectionAllowed(false);
		reportSelection.setRequired(true);
		reportSelection.select(reportSelection.getItemIds().iterator().next());
		reportSelection.setSizeFull();
		reportSelection.addValueChangeListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = -3358229370015557129L;

			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				if (exportPDF != null) {
					exportPDF.setEnabled(false);
				}
			}
		});
		// Add combo
		content.addComponent(reportSelection);
		if (reportDefinition.getClass().getEnumConstants().length <= 1) {
			reportSelection.setVisible(false);
		}
		return content;
	}

	/**
	 * 
	 * @return
	 */
	protected Component buildToolbar() {
		// TODO add custom component with previous/next page buttons and export pdf
		HorizontalLayout content = new HorizontalLayout();
		content.setSizeFull();
		content.setSpacing(true);
		content.addComponent(buildReportSelection());
		content.addComponent(buildExportSelection());
		content.addComponent(buildReportOptions());
		Panel panel = new Panel();
		panel.setContent(content);
		return panel;
	}

	public void clear() {
		reportArea.setValue(getMessageService().getMessage(REPORT_NA_KEY));
	}

	/**
	 * Transforms the report into a StreamResource for downloading
	 * 
	 * @param format
	 * @return
	 */
	protected StreamResource createResourceForExport(final Format format) {
		return new StreamResource(new StreamSource() {
			private static final long serialVersionUID = -5207351556320212325L;

			@Override
			public InputStream getStream() {
				if (jasperReport != null) {
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					reportGenerator.setShowMargins(showMargins.getValue());
					reportGenerator.executeReport(jasperReport, currentParameters, jrDataSource, format,
					        ((WrappedHttpSession) VaadinSession.getCurrent().getSession()).getHttpSession(),
					        VaadinSession.getCurrent().getLocale(), os);
					return new ByteArrayInputStream(os.toByteArray());
				} else {
					return null;
				}
			}

		}, "report." + format.name());
	}

	public void displayReport(Filter filter, Map<String, Object> parameters) {
		// TODO make the report generation asynchronous and display the first page when it is ready
		// and not after last
		// page is ready
		currentParameters = parameters;

		// Load template
		ReportDefinition rd = (ReportDefinition) getReportSelection().getValue();
		String path = getFullPath(rd);
		jasperReport = reportGenerator.loadTemplate(path);

		// Set parameters
		Map<String, Object> params = JRUtils.createParametersFromFilter(jasperReport, filter);
		params.putAll(currentParameters);
		currentParameters.putAll(params);

		// Set datasource
		jrDataSource = null;
		if (!rd.requiresDatabaseConnection()) {
			if (container instanceof Indexed) {
				jrDataSource = new JRIndexedContainerDataSource((Indexed) container);
			} else {
				jrDataSource = new JRContainerDataSource(container);
			}
		}

		// Generate report
		reportGenerator.setShowMargins(showMargins.getValue());
		String html = reportGenerator.executeReportAsHtml(jasperReport, params, jrDataSource,
		        ((WrappedHttpSession) VaadinSession.getCurrent().getSession()).getHttpSession(), VaadinSession
		                .getCurrent().getLocale());
		if (html == null || "".equals(html) || (container != null && container.size() <= 0)) {
			reportArea.setValue(getMessageService().getMessage(NO_DATA_FOUND_KEY));
			exportPDF.setEnabled(false);
		} else {
			if (rd.requiresExternalScript()) {
				VaadinUtils.loadScript(REPORT_AREA_ID, html, rd.requiresExternalScript(), alreadyLoaded);

				// only load external script for the map
				alreadyLoaded = true;
			} else {
				reportArea.setValue(html);
			}
			exportPDF.setEnabled(true);
		}
	}

	public Button getExportPDF() {
		return exportPDF;
	}

	protected String getFullPath(ReportDefinition rd) {
		return (StringUtils.isEmpty(templatePath) ? "" : templatePath) + rd.getReportTemplateName() + REPORT_EXTENSION;
	}

	public ComponentContainer getMain() {
		return main;
	}

	public Component getReportArea() {
		return reportArea;
	}

	public Enum<? extends ReportDefinition> getReportDefinition() {
		return reportDefinition;
	}

	public AbstractSelect getReportSelection() {
		return reportSelection;
	}

	public ReportDefinition getReportSelectionValue() {
		return (ReportDefinition) getReportSelection().getValue();
	}

	public CheckBox getShowMargins() {
		return showMargins;
	}

	public Component getToolbar() {
		return toolbar;
	}

	public void setContainer(Container container, boolean defineProperties) {
		this.container = container;
		if (defineProperties) {
			// Prepare the container by adding all properties needed for the reports
			for (ReportDefinition type : (ReportDefinition[]) reportDefinition.getClass().getEnumConstants()) {
				if (!type.requiresDatabaseConnection()) {
					JRUtils.addContainerPropertiesFromReport(container, reportGenerator.loadTemplate(getFullPath(type)));
				}
			}
		}
	}

	public boolean isSplitlayout() {
		return splitlayout;
	}

	public void setSplitlayout(boolean splitlayout) {
		this.splitlayout = splitlayout;
	}
}
