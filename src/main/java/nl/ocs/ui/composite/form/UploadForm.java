package nl.ocs.ui.composite.form;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import nl.ocs.constants.OCSConstants;
import nl.ocs.ui.composite.type.ScreenMode;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;

/**
 * A form that contains a file upload component and a progress bar
 * 
 * @author bas.rutten
 * 
 */
@SuppressWarnings("serial")
public abstract class UploadForm extends ProgressForm<byte[]> {

	/**
	 * Callback object for handling a file upload
	 * 
	 * @author bas.rutten
	 * 
	 */
	private class UploadReceiver implements SucceededListener, Receiver {

		private static final long serialVersionUID = -8672072143565385035L;

		private ByteArrayOutputStream stream;

		@Override
		public OutputStream receiveUpload(String filename, String mimeType) {
			stream = new ByteArrayOutputStream();
			return stream;
		}

		@Override
		public void uploadSucceeded(final SucceededEvent event) {
			final byte[] bytes = stream.toByteArray();
			UploadForm.this.fileName = event.getFilename();
			prepare(bytes);
		}
	}

	private static final long serialVersionUID = -4717815709838453902L;

	private String fileName;

	private ScreenMode screenMode;

	private boolean showCancelButton;

	/**
	 * Constructor
	 * 
	 * @param progressMode
	 * @param screenMode
	 * @param showCancelButton
	 *            whether to include a cancel button
	 */
	public UploadForm(ProgressMode progressMode, ScreenMode screenMode, boolean showCancelButton) {
		super(progressMode);
		this.screenMode = screenMode;
		this.showCancelButton = showCancelButton;
	}

	/**
	 * The method that is executed after the cancel button is clicked
	 */
	protected void cancel() {
		// override in subclass if needed
	}

	/**
	 * Constructs the screen-specific form content
	 * 
	 * @param layout
	 */
	protected void doBuildForm(Layout layout) {
		// override in subclass
	}

	@Override
	protected void doBuildLayout(Layout main) {
		FormLayout form = new FormLayout();
		form.setMargin(true);
		if (ScreenMode.VERTICAL.equals(screenMode)) {
			form.setStyleName(OCSConstants.CSS_CLASS_HALFSCREEN);
		}

		main.addComponent(form);

		// add custom components
		doBuildForm(form);

		// add file upload field
		UploadReceiver receiver = new UploadReceiver();

		Upload upload = new Upload(message("ocs.uploadform.title"), receiver);
		upload.addSucceededListener(receiver);
		form.addComponent(upload);

		if (showCancelButton) {
			Button cancelButton = new Button(message("ocs.cancel"));
			cancelButton.addClickListener(new Button.ClickListener() {

				@Override
				public void buttonClick(ClickEvent event) {
					cancel();
				}
			});
			main.addComponent(cancelButton);
		}
	}

	/**
	 * Estimates the size of the uploaded file
	 * 
	 * @param uploaded
	 * @return
	 */
	@Override
	protected int estimateSize(byte[] uploaded) {
		return 0;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}
