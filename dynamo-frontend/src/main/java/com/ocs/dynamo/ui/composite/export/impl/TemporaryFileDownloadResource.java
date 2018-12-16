package com.ocs.dynamo.ui.composite.export.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.vaadin.server.DownloadStream;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.UI;

/**
 * The Class TemporaryFileDownloadResource.
 * 
 * Code obtained from:
 * http://vaadin.com/forum/-/message_boards/view_message/159583
 */
public class TemporaryFileDownloadResource extends StreamResource {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 476307190141362413L;

	/** The filename. */
	private final String filename;

	/** The content type. */
	private String contentType;

	/**
	 * Instantiates a new temporary file download resource.
	 * 
	 * @param application the application
	 * @param fileName    the file name
	 * @param contentType the content type
	 * @param tempFile    the temp file
	 * @throws FileNotFoundException the file not found exception
	 */
	public TemporaryFileDownloadResource(final UI application, final String fileName, final String contentType,
			final File tempFile) throws FileNotFoundException {
		super(new FileStreamResource(tempFile), fileName);
		this.filename = fileName;
		this.contentType = contentType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vaadin.terminal.StreamResource#getStream()
	 */
	@Override
	public DownloadStream getStream() {
		final DownloadStream stream = new DownloadStream(getStreamSource().getStream(), contentType, filename);
		stream.setParameter("Content-Disposition", "attachment;filename=" + filename);
		// This magic incantation should prevent anyone from caching the data
		stream.setParameter("Cache-Control", "private,no-cache,no-store");
		// In theory <=0 disables caching. In practice Chrome, Safari (and, apparently,
		// IE) all
		// ignore <=0. Set to 1s
		stream.setCacheTime(1000);
		return stream;
	}

	/**
	 * The Class FileStreamResource.
	 */
	private static class FileStreamResource implements StreamResource.StreamSource {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 3801605481686085335L;

		/**
		 * The input stream. Made it transient per:
		 * https://github.com/jnash67/tableexport-for-vaadin/issues/28
		 */
		private final transient InputStream inputStream;

		/**
		 * Instantiates a new file stream resource.
		 * 
		 * @param fileToDownload the file to download
		 * @throws FileNotFoundException the file not found exception
		 */
		public FileStreamResource(final File fileToDownload) throws FileNotFoundException {
			inputStream = new DeletingFileInputStream(fileToDownload);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.vaadin.terminal.StreamResource.StreamSource#getStream()
		 */
		@Override
		public InputStream getStream() {
			return inputStream;
		}
	}

}
