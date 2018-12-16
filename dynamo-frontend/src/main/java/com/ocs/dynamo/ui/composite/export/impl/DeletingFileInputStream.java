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
package com.ocs.dynamo.ui.composite.export.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * This input stream deletes the given file when the InputStream is closed;
 * intended to be used with temporary files.
 * 
 * Code obtained from:
 * http://vaadin.com/forum/-/message_boards/view_message/159583
 * 
 */
class DeletingFileInputStream extends FileInputStream {

	protected File file = null;

	/**
	 * Instantiates a new deleting file input stream.
	 * 
	 * @param file the file
	 * @throws FileNotFoundException the file not found exception
	 */
	public DeletingFileInputStream(final File file) throws FileNotFoundException {
		super(file);
		this.file = file;
	}

	/**
	 * Closes the stream
	 * 
	 * @throws IOException
	 */
	@Override
	public void close() throws IOException {
		super.close();
		file.delete();
	}
}
