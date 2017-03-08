package com.ocs.jasperreports.renderer;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRPrintImageAreaHyperlink;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.renderers.AreaHyperlinksRenderable;
import net.sf.jasperreports.renderers.DataRenderable;
import net.sf.jasperreports.renderers.RenderToImageAwareRenderable;
import net.sf.jasperreports.renderers.Renderable;
import net.sf.jasperreports.renderers.SimpleRenderToImageAwareDataRenderer;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

public class SvgFontRenderer
		implements DataRenderable, AreaHyperlinksRenderable, RenderToImageAwareRenderable, Renderable {
	private static final long serialVersionUID = 7386368521149332494L;

	private final SimpleRenderToImageAwareDataRenderer renderer;

	private byte[] fontFixedData;

	public SvgFontRenderer(SimpleRenderToImageAwareDataRenderer renderer) {
		this.renderer = renderer;
	}

	@Override
	public byte[] getData(JasperReportsContext jasperReportsContext) throws JRException {
		if (fontFixedData != null) {
			return fontFixedData;
		}
		return renderer.getData(jasperReportsContext);
	}

	public void setFontFixedData(byte[] fontFixedData) {
		this.fontFixedData = fontFixedData;
	}

	@Override
	public List<JRPrintImageAreaHyperlink> getImageAreaHyperlinks(Rectangle2D renderingArea) throws JRException {
		return renderer.getImageAreaHyperlinks(renderingArea);
	}

	@Override
	public boolean hasImageAreaHyperlinks() {
		return renderer.hasImageAreaHyperlinks();
	}

	@Override
	public int getImageDataDPI(JasperReportsContext jasperReportsContext) {
		return renderer.getImageDataDPI(jasperReportsContext);
	}

	@Override
	public Graphics2D createGraphics(BufferedImage bi) {
		return renderer.createGraphics(bi);
	}

	@Override
	public String getId() {
		return renderer.getId();
	}
}