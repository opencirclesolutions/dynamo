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
package com.ocs.jasperreports.renderer;

import net.sf.jasperreports.engine.JRAnchor;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRPrintElement;
import net.sf.jasperreports.engine.JRPrintImage;
import net.sf.jasperreports.engine.JRPrintImageAreaHyperlink;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.HtmlResourceHandler;
import net.sf.jasperreports.engine.export.tabulator.TableCell;
import net.sf.jasperreports.engine.type.HorizontalImageAlignEnum;
import net.sf.jasperreports.engine.type.ModeEnum;
import net.sf.jasperreports.engine.type.ScaleImageEnum;
import net.sf.jasperreports.engine.type.VerticalImageAlignEnum;
import net.sf.jasperreports.engine.util.ImageUtil;
import net.sf.jasperreports.engine.util.JRStringUtil;
import net.sf.jasperreports.engine.util.JRTypeSniffer;
import net.sf.jasperreports.engine.util.Pair;
import net.sf.jasperreports.renderers.AreaHyperlinksRenderable;
import net.sf.jasperreports.renderers.DataRenderable;
import net.sf.jasperreports.renderers.DimensionRenderable;
import net.sf.jasperreports.renderers.Renderable;
import net.sf.jasperreports.renderers.ResourceRenderer;
import net.sf.jasperreports.renderers.util.RendererUtil;
import net.sf.jasperreports.renderers.util.SvgDataSniffer;
import net.sf.jasperreports.renderers.util.SvgFontProcessor;
import org.w3c.tools.codec.Base64Encoder;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Dimension2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * This class is used to override the part where the HtmlExporter fails to use the fonts inside the
 * SVG in cases where it's rendered as an image Exports to SVG xml are already compatible with
 * (custom) fonts Changed parts are surrounded with // CUSTOMIZED and // END CUSTOMIZED
 */
public class HtmlSvgImageFontExporter extends HtmlExporter {

    @Override
    protected void writeImage(JRPrintImage image, TableCell cell) throws IOException, JRException {
        startCell(image, cell);

        int availableImageWidth = image.getWidth() - image.getLineBox().getLeftPadding()
                - image.getLineBox().getRightPadding();
        if (availableImageWidth < 0) {
            availableImageWidth = 0;
        }

        int availableImageHeight = image.getHeight() - image.getLineBox().getTopPadding()
                - image.getLineBox().getBottomPadding();
        if (availableImageHeight < 0) {
            availableImageHeight = 0;
        }

        String horizontalAlignment = getImageHorizontalAlignmentStyle(image);
        String verticalAlignment = getImageVerticalAlignmentStyle(image);

        StringBuilder styleBuffer = new StringBuilder();
        ScaleImageEnum scaleImage = image.getScaleImageValue();
        if (scaleImage != ScaleImageEnum.CLIP) {
            // clipped images are absolutely positioned within a div
            if (!horizontalAlignment.equals(CSS_TEXT_ALIGN_LEFT)) {
                styleBuffer.append("text-align: ");
                styleBuffer.append(horizontalAlignment);
                styleBuffer.append(";");
            }

            if (!verticalAlignment.equals(HTML_VERTICAL_ALIGN_TOP)) {
                styleBuffer.append(" vertical-align: ");
                styleBuffer.append(verticalAlignment);
                styleBuffer.append(";");
            }
        }

        Renderable renderer = image.getRenderer();

        boolean isLazy = RendererUtil.isLazy(renderer);

        if (isLazy || (scaleImage == ScaleImageEnum.CLIP && availableImageHeight > 0)) {
            // some browsers need td height so that height: 100% works on the div used for clipped
            // images.
            // we're using the height without paddings because that's closest to the HTML size
            // model.
            styleBuffer.append("height: ");
            styleBuffer.append(toSizeUnit((float) availableImageHeight));
            styleBuffer.append("; ");
        }

        appendElementCellGenericStyle(cell, styleBuffer);
        appendBackcolorStyle(cell, styleBuffer);

        boolean addedToStyle = appendBorderStyle(cell.getBox(), styleBuffer);
        if (!addedToStyle) {
            appendPen(styleBuffer, image.getLinePen(), null);
        }

        appendPaddingStyle(image.getLineBox(), styleBuffer);

        writeStyle(styleBuffer);

        finishStartCell();

        if (image.getAnchorName() != null) {
            writer.write("<a name=\"");
            writer.write(image.getAnchorName());
            writer.write("\"/>");
        }

        if (image.getBookmarkLevel() != JRAnchor.NO_BOOKMARK) {
            writer.write("<a name=\"");
            writer.write(JR_BOOKMARK_ANCHOR_PREFIX + reportIndex + "_" + pageIndex + "_" + cell.getElementAddress());
            writer.write("\"/>");
        }

        if (renderer != null) {
            boolean startedDiv = false;
            if (scaleImage == ScaleImageEnum.CLIP || (isLazy && ((scaleImage == ScaleImageEnum.RETAIN_SHAPE
                    || scaleImage == ScaleImageEnum.REAL_HEIGHT || scaleImage == ScaleImageEnum.REAL_SIZE)
                    || (image.getHorizontalImageAlign() != HorizontalImageAlignEnum.LEFT
                            || image.getVerticalImageAlign() != VerticalImageAlignEnum.TOP)))) {
                writer.write("<div style=\"width: 100%; height: 100%; position: relative; overflow: hidden;\">\n");
                startedDiv = true;
            }

            boolean hasAreaHyperlinks = renderer instanceof AreaHyperlinksRenderable
                    && ((AreaHyperlinksRenderable) renderer).hasImageAreaHyperlinks();

            boolean hasHyperlinks = false;

            boolean hyperlinkStarted;
            if (hasAreaHyperlinks) {
                hyperlinkStarted = false;
                hasHyperlinks = true;
            } else {
                hyperlinkStarted = startHyperlink(image);
                hasHyperlinks = hyperlinkStarted;
            }

            String imageMapName = null;
            List<JRPrintImageAreaHyperlink> imageMapAreas = null;

            if (hasAreaHyperlinks) {
                Rectangle renderingArea = new Rectangle(image.getWidth(), image.getHeight());

                if (renderer instanceof DataRenderable) {
                    imageMapName = imageMaps.get(new Pair<String, Rectangle>(renderer.getId(), renderingArea));
                }

                if (imageMapName == null) {
                    Renderable originalRenderer = image.getRenderer();
                    imageMapName = "map_" + getElementIndex(cell).toString() + "-" + originalRenderer.getId();
                    imageMapAreas = ((AreaHyperlinksRenderable) originalRenderer).getImageAreaHyperlinks(renderingArea);

                    if (renderer instanceof DataRenderable) {
                        imageMaps.put(new Pair<String, Rectangle>(renderer.getId(), renderingArea), imageMapName);
                    }
                }
            }

            boolean useBackgroundLazyImage = isLazy && ((scaleImage == ScaleImageEnum.RETAIN_SHAPE
                    || scaleImage == ScaleImageEnum.REAL_HEIGHT || scaleImage == ScaleImageEnum.REAL_SIZE)
                    || !(image.getHorizontalImageAlign() == HorizontalImageAlignEnum.LEFT
                            && image.getVerticalImageAlign() == VerticalImageAlignEnum.TOP));

            InternalImageProcessor imageProcessor = new InternalImageProcessor(image, isLazy,
                    !useBackgroundLazyImage && scaleImage != ScaleImageEnum.FILL_FRAME && !isLazy, cell,
                    availableImageWidth, availableImageHeight);

            InternalImageProcessorResult imageProcessorResult = null;

            try {
                imageProcessorResult = imageProcessor.process(renderer);
            } catch (Exception e) {
                Renderable onErrorRenderer = getRendererUtil().handleImageError(e, image.getOnErrorTypeValue());
                if (onErrorRenderer != null) {
                    imageProcessorResult = imageProcessor.process(onErrorRenderer);
                }
            }

            if (imageProcessorResult != null) {
                if (useBackgroundLazyImage) {
                    writer.write("<div style=\"width: 100%; height: 100%; background-image: url('");
                    String imagePath = imageProcessorResult.imageSource;
                    if (imagePath != null) {
                        writer.write(imagePath);
                    }
                    writer.write("'); background-repeat: no-repeat; background-position: " + horizontalAlignment + " "
                            + (image.getVerticalImageAlign() == VerticalImageAlignEnum.MIDDLE ? "center"
                                    : verticalAlignment)
                            + ";background-size: ");

                    switch (scaleImage) {
                    case FILL_FRAME:
                        writer.write("100% 100%");
                        break;
                    case CLIP:
                        writer.write("auto");
                        break;
                    case RETAIN_SHAPE:
                    default:
                        writer.write("contain");
                    }
                    writer.write(";\"></div>");
                } else if (imageProcessorResult.isEmbededSvgData) {
                    writer.write("<svg");

                    switch (scaleImage) {
                    case FILL_FRAME: {
                        Dimension2D dimension = imageProcessorResult.dimension;
                        if (dimension != null) {
                            writer.write(" viewBox=\"0 0 ");
                            writer.write(String.valueOf(dimension.getWidth()));
                            writer.write(" ");
                            writer.write(String.valueOf(dimension.getHeight()));
                            writer.write("\"");
                        }

                        writer.write(" width=\"");
                        writer.write(String.valueOf(availableImageWidth));
                        writer.write("\"");
                        writer.write(" height=\"");
                        writer.write(String.valueOf(availableImageHeight));
                        writer.write("\"");
                        writer.write(" preserveAspectRatio=\"none\"");

                        break;
                    }
                    case CLIP: {
                        double normalWidth = availableImageWidth;
                        double normalHeight = availableImageHeight;

                        Dimension2D dimension = imageProcessorResult.dimension;
                        if (dimension != null) {
                            normalWidth = dimension.getWidth();
                            normalHeight = dimension.getHeight();

                            writer.write(" viewBox=\"");
                            writer.write(String.valueOf(
                                    (int) (ImageUtil.getXAlignFactor(image) * (normalWidth - availableImageWidth))));
                            writer.write(" ");
                            writer.write(String.valueOf(
                                    (int) (ImageUtil.getYAlignFactor(image) * (normalHeight - availableImageHeight))));
                            writer.write(" ");
                            writer.write(String.valueOf(availableImageWidth));
                            writer.write(" ");
                            writer.write(String.valueOf(availableImageHeight));
                            writer.write("\"");
                        }

                        writer.write(" width=\"");
                        writer.write(String.valueOf(availableImageWidth));
                        writer.write("\"");
                        writer.write(" height=\"");
                        writer.write(String.valueOf(availableImageHeight));
                        writer.write("\"");

                        break;
                    }
                    case RETAIN_SHAPE:
                    default: {
                        // considering the IF above, if we get here, then for sure isLazy() is
                        // false, so we can ask the renderer for its dimension
                        if (availableImageHeight > 0) {
                            double normalWidth = availableImageWidth;
                            double normalHeight = availableImageHeight;

                            Dimension2D dimension = imageProcessorResult.dimension;
                            if (dimension != null) {
                                normalWidth = dimension.getWidth();
                                normalHeight = dimension.getHeight();

                                writer.write(" viewBox=\"0 0 ");
                                writer.write(String.valueOf(normalWidth));
                                writer.write(" ");
                                writer.write(String.valueOf(normalHeight));
                                writer.write("\"");
                            }

                            double ratio = normalWidth / normalHeight;

                            if (ratio > (double) availableImageWidth / (double) availableImageHeight) {
                                writer.write(" width=\"");
                                writer.write(String.valueOf(availableImageWidth));
                                writer.write("\"");
                            } else {
                                writer.write(" height=\"");
                                writer.write(String.valueOf(availableImageHeight));
                                writer.write("\"");
                            }
                        }
                    }
                    }

                    writer.write("><g>\n");
                    writer.write(imageProcessorResult.imageSource);
                    writer.write("</g></svg>");
                } else {
                    writer.write("<img");
                    writer.write(" src=\"");
                    String imagePath = imageProcessorResult.imageSource;
                    if (imagePath != null) {
                        writer.write(imagePath);
                    }
                    writer.write("\"");

                    switch (scaleImage) {
                    case FILL_FRAME: {
                        writer.write(" style=\"width: ");
                        writer.write(toSizeUnit((float) availableImageWidth));
                        writer.write("; height: ");
                        writer.write(toSizeUnit((float) availableImageHeight));
                        writer.write("\"");

                        break;
                    }
                    case CLIP: {
                        int positionLeft;
                        int positionTop;

                        HorizontalImageAlignEnum horizontalAlign = image.getHorizontalImageAlign();
                        VerticalImageAlignEnum verticalAlign = image.getVerticalImageAlign();
                        if (isLazy || (horizontalAlign == HorizontalImageAlignEnum.LEFT
                                && verticalAlign == VerticalImageAlignEnum.TOP)) {
                            // no need to compute anything
                            positionLeft = 0;
                            positionTop = 0;
                        } else {
                            double normalWidth = availableImageWidth;
                            double normalHeight = availableImageHeight;

                            Dimension2D dimension = imageProcessorResult.dimension;
                            if (dimension != null) {
                                normalWidth = dimension.getWidth();
                                normalHeight = dimension.getHeight();
                            }

                            // these calculations assume that the image td does not stretch due to
                            // other cells.
                            // when that happens, the image will not be properly aligned.
                            positionLeft = (int) (ImageUtil.getXAlignFactor(horizontalAlign)
                                    * (availableImageWidth - normalWidth));
                            positionTop = (int) (ImageUtil.getYAlignFactor(verticalAlign)
                                    * (availableImageHeight - normalHeight));
                        }

                        writer.write(" style=\"position: absolute; left:");
                        writer.write(toSizeUnit((float) positionLeft));
                        writer.write("; top: ");
                        writer.write(toSizeUnit((float) positionTop));
                        // not setting width, height and clip as it doesn't seem needed plus it
                        // fixes clip for lazy images
                        writer.write(";\"");

                        break;
                    }
                    case RETAIN_SHAPE:
                    default: {
                        // considering the IF above, if we get here, then for sure isLazy() is
                        // false, so we can ask the renderer for its dimension
                        if (availableImageHeight > 0) {
                            double normalWidth = availableImageWidth;
                            double normalHeight = availableImageHeight;

                            Dimension2D dimension = imageProcessorResult.dimension;
                            if (dimension != null) {
                                normalWidth = dimension.getWidth();
                                normalHeight = dimension.getHeight();
                            }

                            double ratio = normalWidth / normalHeight;

                            if (ratio > (double) availableImageWidth / (double) availableImageHeight) {
                                writer.write(" style=\"width: ");
                                writer.write(toSizeUnit((float) availableImageWidth));
                                writer.write("\"");
                            } else {
                                writer.write(" style=\"height: ");
                                writer.write(toSizeUnit((float) availableImageHeight));
                                writer.write("\"");
                            }
                        }
                    }
                    }

                    if (imageMapName != null) {
                        writer.write(" usemap=\"#" + imageMapName + "\"");
                    }

                    writer.write(" alt=\"\"");

                    if (hasHyperlinks) {
                        writer.write(" border=\"0\"");
                    }

                    if (image.getHyperlinkTooltip() != null) {
                        writer.write(" title=\"");
                        writer.write(JRStringUtil.xmlEncode(image.getHyperlinkTooltip()));
                        writer.write("\"");
                    }

                    writer.write("/>");
                }
            }

            if (hyperlinkStarted) {
                endHyperlink();
            }

            if (startedDiv) {
                writer.write("</div>");
            }

            if (imageMapAreas != null) {
                writer.write("\n");
                writeImageMap(imageMapName, image, imageMapAreas);
            }
        }

        endCell();
    }

    private static class InternalImageProcessorResult {

        private final String imageSource;

        private final Dimension2D dimension;

        private final boolean isEmbededSvgData;

        protected InternalImageProcessorResult(String imagePath, Dimension2D dimension, boolean isEmbededSvgData) {
            this.imageSource = imagePath;
            this.dimension = dimension;
            this.isEmbededSvgData = isEmbededSvgData;
        }

        public String getImageSource() {
            return imageSource;
        }

        public Dimension2D getDimension() {
            return dimension;
        }

        public boolean isEmbededSvgData() {
            return isEmbededSvgData;
        }

    }

    private class InternalImageProcessor {
        private final JRPrintElement imageElement;
        private final boolean isLazy;
        private final boolean embedImage;
        private final boolean needDimension;
        private final TableCell cell;
        private final int availableImageWidth;
        private final int availableImageHeight;

        protected InternalImageProcessor(JRPrintElement imageElement, boolean isLazy, boolean needDimension,
                TableCell cell, int availableImageWidth, int availableImageHeight) {
            this.imageElement = imageElement;
            this.isLazy = isLazy;
            this.embedImage = isEmbedImage(imageElement);
            this.needDimension = needDimension;
            this.cell = cell;
            this.availableImageWidth = availableImageWidth;
            this.availableImageHeight = availableImageHeight;
        }

        protected InternalImageProcessorResult process(Renderable renderer) throws JRException, IOException {
            String imageSource = null;
            Dimension2D dimension = null;
            boolean isEmbededSvgData = false;

            if (isLazy) {
                // we do not cache imagePath for lazy images because the short location string is
                // already cached inside the render itself
                imageSource = RendererUtil.getResourceLocation(renderer);
            } else {
                if (renderer instanceof ResourceRenderer) {
                    renderer = renderersCache.getLoadedRenderer((ResourceRenderer) renderer);
                }

                // check dimension first, to avoid caching renderers that might not be used
                // eventually, due to their dimension errors
                if (needDimension) {
                    DimensionRenderable dimensionRenderer = renderersCache.getDimensionRenderable(renderer);
                    dimension = dimensionRenderer == null ? null : dimensionRenderer.getDimension(jasperReportsContext);
                }

                if (!embedImage // we do not cache imagePath for embedded images because it is too
                                // big
                        && renderer instanceof DataRenderable
                        // we do not cache imagePath for non-data renderers because they render
                        // width different width/height each time
                        && rendererToImagePathMap.containsKey(renderer.getId())) {
                    imageSource = rendererToImagePathMap.get(renderer.getId());
                } else {
                    if (embedImage) {
                        DataRenderable dataRenderer = null;

                        if (isConvertSvgToImage(imageElement)) {
                            dataRenderer = getRendererUtil().getImageDataRenderable(renderersCache, renderer,
                                    new Dimension(availableImageWidth, availableImageHeight),
                                    ModeEnum.OPAQUE == imageElement.getModeValue() ? imageElement.getBackcolor()
                                            : null);
                        } else {
                            dataRenderer = getRendererUtil().getDataRenderable(renderer,
                                    new Dimension(availableImageWidth, availableImageHeight),
                                    ModeEnum.OPAQUE == imageElement.getModeValue() ? imageElement.getBackcolor()
                                            : null);
                        }

                        byte[] imageData = dataRenderer.getData(jasperReportsContext);

                        SvgDataSniffer.SvgInfo svgInfo = getRendererUtil().getSvgInfo(imageData);

                        if (svgInfo != null) {
                            imageData = processSvgWithFonts(imageData);

                            isEmbededSvgData = true;

                            String encoding = svgInfo.getEncoding();
                            imageSource = new String(imageData, encoding == null ? "UTF-8" : encoding);

                            // we might have received needDimension false above, as a hint, but if
                            // we arrive here,
                            // we definitely need to attempt getting the dimension of the SVG,
                            // regardless of scale image type
                            DimensionRenderable dimensionRenderer = renderersCache.getDimensionRenderable(renderer);
                            dimension = dimensionRenderer == null ? null
                                    : dimensionRenderer.getDimension(jasperReportsContext);
                        } else {
                            String imageMimeType = JRTypeSniffer.getImageTypeValue(imageData).getMimeType();

                            ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();

                            Base64Encoder encoder = new Base64Encoder(bais, baos);
                            encoder.process();

                            imageSource = "data:" + imageMimeType + ";base64,"
                                    + new String(baos.toByteArray(), "UTF-8"); // UTF-8 is fine as
                                                                               // we just need an
                                                                               // ASCII compatible
                                                                               // encoding for the
                                                                               // Base64 array
                        }

                        // don't cache embedded imageSource as they are not image paths
                    } else {
                        @SuppressWarnings("deprecation")
                        HtmlResourceHandler imageHandler = getImageHandler() == null
                                ? getExporterOutput().getImageHandler() : getImageHandler();
                        if (imageHandler != null) {
                            DataRenderable dataRenderer = null;

                            if (isConvertSvgToImage(imageElement)) {
                                dataRenderer = getRendererUtil().getImageDataRenderable(renderersCache, renderer,
                                        new Dimension(availableImageWidth, availableImageHeight),
                                        ModeEnum.OPAQUE == imageElement.getModeValue() ? imageElement.getBackcolor()
                                                : null);
                            } else {
                                dataRenderer = getRendererUtil().getDataRenderable(renderer,
                                        new Dimension(availableImageWidth, availableImageHeight),
                                        ModeEnum.OPAQUE == imageElement.getModeValue() ? imageElement.getBackcolor()
                                                : null);
                            }

                            byte[] imageData = dataRenderer.getData(jasperReportsContext);

                            // CUSTOMIZED

                            SvgDataSniffer.SvgInfo svgInfo = getRendererUtil().getSvgInfo(imageData);

                            if (svgInfo != null) {
                                imageData = processSvgWithFonts(imageData);
                                if (dataRenderer instanceof SvgFontRenderer) {
                                    ((SvgFontRenderer) dataRenderer).setFontFixedData(imageData);
                                }
                            }
                            // END CUSTOMIZED

                            String fileExtension = getRendererUtil().isSvgData(imageData)
                                    ? RendererUtil.SVG_FILE_EXTENSION
                                    : JRTypeSniffer.getImageTypeValue(imageData).getFileExtension();

                            String imageName = getImageName(getElementIndex(cell), fileExtension);

                            imageHandler.handleResource(imageName, imageData);

                            imageSource = imageHandler.getResourcePath(imageName);

                            if (dataRenderer == renderer) {
                                // cache imagePath only for true ImageRenderable instances because
                                // the wrapping ones render with different width/height each time
                                rendererToImagePathMap.put(renderer.getId(), imageSource);
                            }
                        }
                        // does not make sense to cache null imagePath, in the absence of an image
                        // handler
                    }
                }
            }

            return new InternalImageProcessorResult(imageSource, dimension, isEmbededSvgData);
        }

        /**
         * Process the SVG data and use the fonts defined to generate labels etc in the correct font
         *
         * @param imageData
         * @return
         */
        private byte[] processSvgWithFonts(byte[] imageData) {
            if (isEmbeddedSvgUseFonts(imageElement)) {
                Locale locale = getLocale();

                SvgFontProcessor svgFontProcessor = new SvgFontProcessor(jasperReportsContext, locale) {
                    @Override
                    public String getFontFamily(String fontFamily, Locale locale) {
                        // Here we rely on the ability of FontUtil.getFontInfoIgnoreCase(fontFamily,
                        // locale) method to
                        // find fonts from font extensions based on the java.awt.Font.getFamily() of
                        // their font faces.
                        // This is because the SVG produced by Batik stores the family name of the
                        // AWT fonts used to
                        // render text on the Batik Graphics2D implementation, as it knows nothing
                        // about family names from JR extensions.
                        return HtmlSvgImageFontExporter.this.getFontFamily(true, fontFamily, locale);
                    }
                };

                imageData = svgFontProcessor.process(imageData);
            }

            return imageData;
        }
    }

}
