package com.ocs.jasperreports;

import net.sf.jasperreports.web.util.WebHtmlResourceHandler;

public class UniqueWebHtmlResourceHandler extends WebHtmlResourceHandler {
	public UniqueWebHtmlResourceHandler(String path) {
		super(path);
	}

	@Override
	public String getResourcePath(String id) {
		return super.getResourcePath(id) + "&time=" + System.nanoTime();
	}
}
