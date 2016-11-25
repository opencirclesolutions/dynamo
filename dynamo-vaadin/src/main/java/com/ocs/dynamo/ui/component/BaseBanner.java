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
package com.ocs.dynamo.ui.component;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Image;

/**
 * Base class for the application banner that contains an image to the left
 * 
 * @author bas.rutten
 *
 */
public class BaseBanner extends DefaultHorizontalLayout {

    private static final long serialVersionUID = -6796904385035752461L;

    /**
     * The image component
     */
    private final Image image;

    /**
     * The path to the image
     */
    private final String imagePath;

    /**
     * Constructor
     * 
     * @param imagePath
     *            the path to the image
     */
    public BaseBanner(String imagePath) {
        super(true, false, false);
        this.imagePath = imagePath;

        setId("banner");
        setSizeFull();

        image = new Image(null, new ThemeResource(imagePath));
        image.setWidth(null);
        addComponent(image);
        setComponentAlignment(image, Alignment.MIDDLE_CENTER);
    }

    public Image getImage() {
        return image;
    }

    public String getImagePath() {
        return imagePath;
    }

}
