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
package com.ocs.jasperreports.export;

import net.sf.jasperreports.export.OutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;
import net.sf.jasperreports.export.WriterExporterOutput;

import java.io.OutputStream;

/**
 * Combine the Writer and OutputStream based Exporters so we can use 1 generic type for all report
 * types Report Exporters can use one of both but we don't want to keep track of this specialization
 */
public class WriterOutputStreamExporterOutput extends SimpleWriterExporterOutput
        implements WriterExporterOutput, OutputStreamExporterOutput {

    private final OutputStream outputStream;

    /**
     * Create writer based on the outputStream and keep a reference to it
     *
     * @param outputStream
     *            stream to write to
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
