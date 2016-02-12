package nl.ocs.ui.component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.Button;

/**
 * A button that starts a file download process when clicked
 * 
 * @author bas.rutten
 * 
 */
public abstract class DownloadButton extends Button {

	private static final long serialVersionUID = -7163648327567831406L;

	/**
	 * Constructor
	 * 
	 * @param caption
	 *            the caption of the button
	 * @param fileName
	 *            the name of the file to download
	 */
	public DownloadButton(String caption, String fileName) {
		super(caption);
		StreamResource resource = new StreamResource(new StreamSource() {

			private static final long serialVersionUID = -4870779918745663459L;

			@Override
			public InputStream getStream() {
				byte[] content = doCreateContent();
				if (content != null) {
					return new ByteArrayInputStream(content);
				}
				return null;
			}

		}, fileName);

		FileDownloader downloader = new FileDownloader(resource);
		downloader.extend(this);
	}

	/**
	 * Creates the file content (as a byte array)
	 * 
	 * @return
	 */
	protected abstract byte[] doCreateContent();

}
