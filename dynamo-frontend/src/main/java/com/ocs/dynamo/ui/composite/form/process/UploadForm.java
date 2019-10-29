///*
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
// */
//package com.ocs.dynamo.ui.composite.form.process;
//
//import java.io.ByteArrayOutputStream;
//import java.io.OutputStream;
//
//import com.ocs.dynamo.constants.DynamoConstants;
//import com.ocs.dynamo.ui.composite.type.ScreenMode;
//import com.vaadin.flow.component.ComponentEvent;
//import com.vaadin.flow.component.UI;
//import com.vaadin.flow.component.button.Button;
//import com.vaadin.flow.component.formlayout.FormLayout;
//import com.vaadin.flow.component.notification.Notification;
//import com.vaadin.flow.component.orderedlayout.VerticalLayout;
//import com.vaadin.flow.component.upload.Receiver;
//import com.vaadin.flow.component.upload.SucceededEvent;
//import com.vaadin.flow.component.upload.Upload;
//
///**
// * A form that contains a file upload component and a progress bar
// * 
// * @author bas.rutten
// */
//public abstract class UploadForm extends ProgressForm<byte[]> {
//
//    /**
//     * Callback object for handling a file upload
//     * 
//     * @author bas.rutten
//     */
//    private class UploadReceiver implements SucceededListener, Receiver {
//
//        private static final long serialVersionUID = -8672072143565385035L;
//
//        private ByteArrayOutputStream stream;
//
//        @Override
//        public OutputStream receiveUpload(String filename, String mimeType) {
//            stream = new ByteArrayOutputStream();
//            return stream;
//        }
//
//        @Override
//        public void uploadSucceeded(final SucceededEvent event) {
//            final byte[] bytes = stream.toByteArray();
//            if (bytes != null && bytes.length > 0) {
//                UploadForm.this.fileName = event.getFilename();
//                startWork(bytes);
//            } else {
//                showNotification(message("ocs.no.file.selected"), Notification.Type.ERROR_MESSAGE);
//            }
//        }
//    }
//
//    private static final long serialVersionUID = -4717815709838453902L;
//
//    private String fileName;
//
//    private ScreenMode screenMode;
//
//    private boolean showCancelButton;
//
//    private Upload upload;
//
//    /**
//     * Constructor
//     * 
//     * @param progressMode     the desired progress mode
//     * @param showCancelButton whether to include a cancel button
//     */
//    public UploadForm(UI ui, ProgressMode progressMode, boolean showCancelButton) {
//        super(ui, progressMode);
//        this.showCancelButton = showCancelButton;
//    }
//
//    /**
//     * The method that is executed after the cancel button is clicked
//     */
//    protected void cancel() {
//        // override in subclass if needed
//    }
//
//    /**
//     * Constructs the screen-specific form content
//     * 
//     * @param layout
//     */
//    protected void doBuildForm(FormLayout layout) {
//        // override in subclass
//    }
//
//    @Override
//    protected void doBuildLayout(VerticalLayout main) {
//        FormLayout form = new FormLayout();
//       // form.setMargin(true);
//        if (ScreenMode.VERTICAL.equals(screenMode)) {
//            form.setClassName(DynamoConstants.CSS_CLASS_HALFSCREEN);
//        }
//
//        main.add(form);
//
//        // add custom components
//        doBuildForm(form);
//
//        // add file upload field
//        UploadReceiver receiver = new UploadReceiver();
//
//        upload = new Upload(message("ocs.uploadform.title"), receiver);
//
//        upload.addSucceededListener(receiver);
//        form.add(upload);
//
//        if (showCancelButton) {
//            Button cancelButton = new Button(message("ocs.cancel"));
//            cancelButton.addClickListener((ComponentEvent) event -> cancel());
//            main.add(cancelButton);
//        }
//    }
//
//    public String getFileName() {
//        return fileName;
//    }
//
//    public Upload getUpload() {
//        return upload;
//    }
//
//}
