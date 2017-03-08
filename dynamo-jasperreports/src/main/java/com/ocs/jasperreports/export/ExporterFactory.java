package com.ocs.jasperreports.export;

import com.ocs.jasperreports.ReportGenerator;
import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRPptxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.Exporter;
import net.sf.jasperreports.export.SimpleDocxExporterConfiguration;
import net.sf.jasperreports.export.SimpleDocxReportConfiguration;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import net.sf.jasperreports.export.SimplePdfReportConfiguration;
import net.sf.jasperreports.export.SimplePptxExporterConfiguration;
import net.sf.jasperreports.export.SimplePptxReportConfiguration;
import net.sf.jasperreports.export.SimpleXlsxExporterConfiguration;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;

public class ExporterFactory {

	public static Exporter getExporter(ReportGenerator.Format format) {
		switch (format) {
		case EXCEL:
			return ExporterFactory.getExcelExporter();
		case POWERPOINT:
			return ExporterFactory.getPptxExporter();
		case DOC:
			return ExporterFactory.getDocxExporter();
		default:
			// PDF
			return ExporterFactory.getPdfExporter();
		}
	}

	private static JRPptxExporter getPptxExporter() {
		final JasperReportsContext jasperReportsContext = DefaultJasperReportsContext.getInstance();
		final JRPptxExporter exporter = new JRPptxExporter(jasperReportsContext);

		final SimplePptxExporterConfiguration exporterConfiguration = new SimplePptxExporterConfiguration();
		exporter.setConfiguration(exporterConfiguration);

		final SimplePptxReportConfiguration reportConfiguration = new SimplePptxReportConfiguration();
		exporter.setConfiguration(reportConfiguration);

		return exporter;
	}

	private static JRDocxExporter getDocxExporter() {
		final JasperReportsContext jasperReportsContext = DefaultJasperReportsContext.getInstance();
		final JRDocxExporter exporter = new JRDocxExporter(jasperReportsContext);

		final SimpleDocxExporterConfiguration exporterConfiguration = new SimpleDocxExporterConfiguration();
		exporter.setConfiguration(exporterConfiguration);

		final SimpleDocxReportConfiguration reportConfiguration = new SimpleDocxReportConfiguration();
		exporter.setConfiguration(reportConfiguration);

		return exporter;
	}

	private static JRPdfExporter getPdfExporter() {
		final JasperReportsContext jasperReportsContext = DefaultJasperReportsContext.getInstance();
		final JRPdfExporter exporter = new JRPdfExporter(jasperReportsContext);

		final SimplePdfExporterConfiguration exporterConfiguration = new SimplePdfExporterConfiguration();
		exporter.setConfiguration(exporterConfiguration);

		final SimplePdfReportConfiguration reportConfiguration = new SimplePdfReportConfiguration();
		exporter.setConfiguration(reportConfiguration);

		return exporter;
	}

	private static JRXlsxExporter getExcelExporter() {
		final JasperReportsContext jasperReportsContext = DefaultJasperReportsContext.getInstance();
		final JRXlsxExporter exporter = new JRXlsxExporter(jasperReportsContext);

		final SimpleXlsxExporterConfiguration exporterConfiguration = new SimpleXlsxExporterConfiguration();
		exporter.setConfiguration(exporterConfiguration);

		final SimpleXlsxReportConfiguration reportConfiguration = new SimpleXlsxReportConfiguration();
		exporter.setConfiguration(reportConfiguration);

		return exporter;
	}
}
