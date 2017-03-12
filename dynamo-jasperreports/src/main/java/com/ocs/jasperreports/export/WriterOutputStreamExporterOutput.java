package com.ocs.jasperreports.export;

import net.sf.jasperreports.export.OutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;
import net.sf.jasperreports.export.WriterExporterOutput;

import java.io.OutputStream;

/**
 * Combine the Writer and OutputStream based Exporters so we can use 1 generic type for all report types
 * Report Exporters can use one of both but we don't want to keep track of this specialization
 */
public class WriterOutputStreamExporterOutput extends SimpleWriterExporterOutput
		implements WriterExporterOutput, OutputStreamExporterOutput {

	private final OutputStream outputStream;

	/**
	 * Create writer based on the outputStream and keep a reference to it
	 *
	 * @param outputStream stream to write to
	 */
	public WriterOutputStreamExporterOutput(OutputStream outputStream) {
		super(outputStream);
		this.outputStream = outputStream;
	}

	/**
	 * @return the outputStream
	 */
	@Override
	public OutputStream getOutputStream() {
		return outputStream;
	}
}
