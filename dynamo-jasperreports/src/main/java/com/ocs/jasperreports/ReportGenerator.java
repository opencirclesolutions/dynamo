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
package com.ocs.jasperreports;

import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.jasperreports.export.ExporterFactory;
import com.ocs.jasperreports.renderer.HtmlSvgImageFontExporter;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.Exporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterConfiguration;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimpleHtmlReportConfiguration;
import net.sf.jasperreports.j2ee.servlets.BaseHttpServlet;
import org.apache.commons.lang.ObjectUtils;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Class to support the generation of jasperreports
 *
 * @author Patrick Deenen (patrick@opencircle.solutions)
 */
@Service
public class ReportGenerator {

	private DataSource dataSource;

	@Inject
	public ReportGenerator(DataSource dataSource) {
		this();
		this.dataSource = dataSource;
	}

	public enum Format {
		HTML("html"), PDF("pdf"), EXCEL("xls"), POWERPOINT("pptx"), DOC("doc");

		private final String extension;

		Format(String extension) {
			this.extension = extension;
		}

		public String getExtension() {
			return extension;
		}
	}

	/**
	 * Create instance for report creation
	 */
	public ReportGenerator() {
		setTempFolder(null);
	}

	/**
	 * Set temporary folder for compiling reports
	 *
	 * @param tempFolder
	 */
	private void setTempFolder(String tempFolder) {
		if (tempFolder == null || !new File(tempFolder).isDirectory()) {
			System.setProperty("jasper.reports.compile.temp", System.getProperty("java.io.tmpdir"));
		} else {
			System.setProperty("jasper.reports.compile.temp", tempFolder);
		}
	}

	/**
	 * Load the template (defined by templatePath) and return a JasperDesign object representing the
	 * template
	 *
	 * @return JasperDesign
	 */
	public JasperReport loadTemplate(String templatePath) {
		try {
			InputStream in = JRLoader.getLocationInputStream(templatePath);
			return (JasperReport) JRLoader.loadObject(in);
		} catch (JRException e) {
			throw new OCSRuntimeException("Failed to find jasper report template!", e);
		}

	}

	public JasperPrint fillReport(JasperReport jasperReport, Map<String, Object> parameters, JRDataSource jrDataSource) {
		try {
			if (jrDataSource != null) {
				return JasperFillManager.fillReport(jasperReport, parameters, jrDataSource);
			} else {
				try (Connection connection = dataSource.getConnection()) {
					return JasperFillManager.fillReport(jasperReport, parameters, connection);
				}
			}
		} catch (SQLException | JRException e) {
			throw new OCSRuntimeException("Failed to fill jasper report!", e);
		}
	}

	/**
	 * Convenience method which loads, fills and executes the report as HTML in a string
	 *
	 * @return The HTML report
	 */
	public String executeReportAsHtml(JasperReport jasperReport, Map<String, Object> parameters,
	        JRDataSource jrDataSource, HttpSession session, Locale locale) {
		// Start execution
		StringBuilder sb = executeReport(jasperReport, parameters, jrDataSource, Format.HTML, session, locale, null);
		return sb.toString();
	}

	/**
	 * Convenience method which loads, fills and executes the report
	 *
	 *
	 * @param locale
	 * @param outputStream
	 * @throws JRException
	 */
	public StringBuilder executeReport(JasperReport jasperReport, Map<String, Object> parameters,
	        JRDataSource jrDataSource, Format format, HttpSession session, Locale locale, OutputStream outputStream) {
		// Add format to parameters
		Map<String, Object> copyParameters = new HashMap<>(parameters);

		copyParameters.put(Format.class.getSimpleName(), format.name());

		// only set when not explicit given earlier
		if (!copyParameters.containsKey(JRParameter.REPORT_LOCALE)) {
			copyParameters.put(JRParameter.REPORT_LOCALE, locale);
		}

		final boolean showMargins = (boolean) ObjectUtils.defaultIfNull(parameters.get("showMargins"), Boolean.TRUE);

		// Fill report
		final JasperPrint jasperPrint = fillReport(jasperReport, copyParameters, jrDataSource);
		// Export report
		switch (format) {
		case HTML:
			return exportReportToHTML(jasperPrint, session, showMargins);
		default:
			// Other formats
			exportReport(jasperPrint, format, outputStream);
			break;
		}
		return null;
	}

	/**
	 * Export the current report results to HTML
	 *
	 * @param jasperPrint
	 *            The jasperPrint
	 * @param session
	 *            The user session
	 */
	private StringBuilder exportReportToHTML(JasperPrint jasperPrint, HttpSession session, boolean showMargins) {
		StringBuilder sb = new StringBuilder();
		try {
			HtmlExporter exporter = new HtmlSvgImageFontExporter();
			exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
			SimpleHtmlExporterOutput out = new SimpleHtmlExporterOutput(sb);
			out.setImageHandler(new UniqueWebHtmlResourceHandler("servlets/image?image={0}"));
			exporter.setExporterOutput(out);

			SimpleHtmlExporterConfiguration exporterConfig = new SimpleHtmlExporterConfiguration();
			if (!showMargins) {
				exporterConfig.setBetweenPagesHtml("");
				exporterConfig.setHtmlHeader("");
				exporterConfig.setHtmlFooter("");
			}
			exporter.setConfiguration(exporterConfig);

			final SimpleHtmlReportConfiguration reportConfiguration = new SimpleHtmlReportConfiguration();
			reportConfiguration.setEmbeddedSvgUseFonts(true);
			reportConfiguration.setIgnoreHyperlink(false);
			reportConfiguration.setIgnorePageMargins(true);
			exporter.setConfiguration(reportConfiguration);

			// FIXME The communication through a session object is not scalable
			session.setAttribute(BaseHttpServlet.DEFAULT_JASPER_PRINT_SESSION_ATTRIBUTE, jasperPrint);

			exporter.exportReport();
		} catch (JRException e) {
			throw new OCSRuntimeException("Failed to export jasper report to HTML!", e);
		}
		return sb;
	}

	/**
	 * Export the current report results to an specified format
	 *
	 * @param jasperPrint
	 *            The jasperPrint
	 * @param format
	 *            Format to render
	 * @param outputStream
	 *            The output stream to which the report is written
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void exportReport(JasperPrint jasperPrint, Format format, OutputStream outputStream) {
		try {
			Exporter exporter = ExporterFactory.getExporter(format, jasperPrint, outputStream);
			exporter.exportReport();
		} catch (JRException e) {
			throw new OCSRuntimeException("Failed to export jasper report to PDF!", e);
		}
	}

}
