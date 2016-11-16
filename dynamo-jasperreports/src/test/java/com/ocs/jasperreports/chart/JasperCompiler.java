package com.ocs.jasperreports.chart;

import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRCompiler;
import net.sf.jasperreports.engine.design.JRJdtCompiler;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.xml.JRReportSaxParserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;

public class JasperCompiler {
	private static final Logger LOG = LoggerFactory.getLogger(JasperCompiler.class);

	public static void main(String[] args) throws Exception {
		//JasperReport jasperReport = JasperCompiler.compile("best_worst_graph_table_subreport.jrxml", "best_worst_performing_stores.jrxml");
		JasperReport jasperReport = JasperCompiler.compile("program_performance_graph_subreport.jrxml", "program_performance.jrxml");
		//JasperReport jasperReport = JasperCompiler.compile("SAS_Store_performance_quadrant.jrxml");
		//JasperReport jasperReport = JasperCompiler.compile("program_performance.jrxml");
		JasperCompiler.export(jasperReport, "output2.pdf");
	}

	private static JasperReport compile(String... jrxmls) throws Exception {
		DefaultJasperReportsContext jrContext = DefaultJasperReportsContext.getInstance();

		jrContext.setProperty(JRReportSaxParserFactory.COMPILER_XML_VALIDATION, "true");
		jrContext.setProperty(JRCompiler.COMPILER_PREFIX, JRJdtCompiler.class.getName());
		jrContext.setProperty(JRCompiler.COMPILER_KEEP_JAVA_FILE, "false");

		File lastReport = null;
		for (String jrxml : jrxmls) {
			File source = new ClassPathResource(jrxml).getFile();

			try (InputStream in = new FileInputStream(source)) {
				lastReport = new File(source.getParentFile(), jrxml.substring(0, jrxml.indexOf(".jrxml")) + ".jasper");
				if (lastReport.exists()) {
					lastReport.delete();
				}
				try (OutputStream out = new FileOutputStream(lastReport)) {
					LOG.info("Compiling {} to {}", source.getAbsolutePath(), lastReport.getAbsolutePath());
					long start = System.nanoTime();
					JasperCompileManager.compileReportToStream(in, out);
					LOG.info("Took {} ms", (System.nanoTime() - start)/1000000);
				}
			} catch (Exception e) {
				LOG.error("Could not compile " + source.getName() + " because " + e.getMessage(), e);

				throw new JRException("Could not compile " + source.getName(), e);
			}
		}
		long start = System.nanoTime();
		final JasperReport jasperReport = (JasperReport) JRLoader.loadObject(lastReport);
		LOG.info("Loading {} took {} ms", lastReport.getName(), (System.nanoTime() - start)/1000000);
		return jasperReport;
	}

	private static void export(JasperReport jasperReport, String outputFile) throws Exception {
		// String dbUrl = props.getProperty("jdbc.url");
		String dbUrl = "jdbc:postgresql://localhost:5432/sas";
		// String dbDriver = props.getProperty("jdbc.driver");
		String dbDriver = "org.postgresql.Driver";
		// String dbUname = props.getProperty("db.username");
		String dbUname = "postgres";
		// String dbPwd = props.getProperty("db.password");
		String dbPwd = "password";

		// Load the JDBC driver
		Class.forName(dbDriver);
		// Get the connection
		Connection conn = DriverManager.getConnection(dbUrl, dbUname, dbPwd);

		// Generate jasper print

		long start = System.nanoTime();
		JasperPrint jprint = JasperFillManager.fillReport(jasperReport, null, conn);
		LOG.info("Filling {} took {} ms", jasperReport.getName(), (System.nanoTime() - start)/1000000);

		// Export pdf file
		start = System.nanoTime();
		final File file = new File(outputFile);
		JasperExportManager.exportReportToPdfFile(jprint, outputFile);
		LOG.info("Exporting {} to {} took {} ms", jasperReport.getName(), file.getAbsoluteFile(), (System.nanoTime() - start)/1000000);


		Desktop.getDesktop().open(file);
	}

}
