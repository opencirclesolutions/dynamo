/*
 * JasperReports - Free Java Reporting Library.
 * Copyright (C) 2001 - 2016 TIBCO Software Inc. All rights reserved.
 * http://www.jaspersoft.com
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of JasperReports.
 *
 * JasperReports is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JasperReports is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JasperReports. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.jasperreports.components.map;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRGenericPrintElement;
import net.sf.jasperreports.engine.JRPrintImage;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.base.JRBasePrintImage;
import net.sf.jasperreports.engine.type.HorizontalImageAlignEnum;
import net.sf.jasperreports.engine.type.OnErrorTypeEnum;
import net.sf.jasperreports.engine.type.ScaleImageEnum;
import net.sf.jasperreports.engine.type.VerticalImageAlignEnum;
import net.sf.jasperreports.engine.util.JRColorUtil;
import net.sf.jasperreports.renderers.Renderable;
import net.sf.jasperreports.renderers.util.RendererUtil;

/**
 * Copy from original class by Jasperreports, which doesn't support automatic centering and zooming
 * to ensure the visibility of all defined makers. Now when one defines a zoom level < 0 then the
 * center coordinate and zoom level will not be used and google will automatically size and center
 * to show all markers.
 * 
 * Other enhancements include: - Grouping of the marker style to fit more markers in the static map.
 * - Corrected the Google static map URL.
 * 
 * @author sanda zaharia (shertage@users.sourceforge.net)
 */
public class MapElementImageProvider {
	/**
	 * The character count limit for a static map URL request
	 */
	private static final Integer MAX_URL_LENGTH = 2048;

	private MapElementImageProvider() {
	}

	@SuppressWarnings("unchecked")
	public static JRPrintImage getImage(JasperReportsContext jasperReportsContext, JRGenericPrintElement element)
	        throws JRException {

		Float latitude = (Float) element.getParameterValue(MapComponent.ITEM_PROPERTY_latitude);
		latitude = latitude == null ? MapComponent.DEFAULT_LATITUDE : latitude;

		Float longitude = (Float) element.getParameterValue(MapComponent.ITEM_PROPERTY_longitude);
		longitude = longitude == null ? MapComponent.DEFAULT_LONGITUDE : longitude;

		Integer zoom = (Integer) element.getParameterValue(MapComponent.PARAMETER_ZOOM);
		zoom = zoom == null ? MapComponent.DEFAULT_ZOOM : zoom;

		String mapType = (String) element.getParameterValue(MapComponent.ATTRIBUTE_MAP_TYPE);
		String mapScale = (String) element.getParameterValue(MapComponent.ATTRIBUTE_MAP_SCALE);
		String mapFormat = (String) element.getParameterValue(MapComponent.ATTRIBUTE_IMAGE_TYPE);
		String reqParams = (String) element.getParameterValue(MapComponent.PARAMETER_REQ_PARAMS);
		String markers = "";

		List<Map<String, Object>> markerList = (List<Map<String, Object>>) element
		        .getParameterValue(MapComponent.PARAMETER_MARKERS);
		if (markerList != null && !markerList.isEmpty()) {

			// First group the markers by style
			Map<String, List<String>> groupedMarkers = new HashMap<>();
			for (Map<String, Object> map : markerList) {
				if (map != null && !map.isEmpty()) {
					StringBuilder style = new StringBuilder();
					String size = (String) map.get(MapComponent.ITEM_PROPERTY_MARKER_size);
					style.append(size != null && size.length() > 0 ? "size:" + size + "%7C" : "");
					String color = (String) map.get(MapComponent.ITEM_PROPERTY_MARKER_color);
					style.append(color != null && color.length() > 0 ? "color:0x" + color + "%7C" : "");
					String label = (String) map.get(MapComponent.ITEM_PROPERTY_MARKER_label);
					style.append(label != null && label.length() > 0 ? "label:"
					        + Character.toUpperCase(label.charAt(0)) + "%7C" : "");
					String icon = map.get(MapComponent.ITEM_PROPERTY_MARKER_ICON_url) != null ? (String) map
					        .get(MapComponent.ITEM_PROPERTY_MARKER_ICON_url) : (String) map
					        .get(MapComponent.ITEM_PROPERTY_MARKER_icon);
					if (icon != null && icon.length() > 0) {
						style.append("icon:" + icon + "%7C");
					}
					StringBuilder ll = new StringBuilder().append(map.get(MapComponent.ITEM_PROPERTY_latitude));
					ll.append(",");
					ll.append(map.get(MapComponent.ITEM_PROPERTY_longitude));
					List<String> gml = null;
					if (groupedMarkers.containsKey(style)) {
						gml = groupedMarkers.get(style);
					} else {
						gml = new ArrayList<>();
						groupedMarkers.put(style.toString(), gml);
					}
					gml.add(ll.toString());
				}
			}

			// Then add the markers to string list
			String currentMarkers = "";
			for (Object style : groupedMarkers.keySet().toArray()) {
				currentMarkers = "&markers=" + style;
				int i = 0;
				for (String cm : groupedMarkers.get(style)) {
					if (i > 0) {
						currentMarkers += "%7C";
					}
					currentMarkers += cm;

					i++;
				}
				if (markers.length() + 248 < MAX_URL_LENGTH) {
					markers += currentMarkers;
				} else {
					break;
				}
			}
		}

		List<Map<String, Object>> pathList = (List<Map<String, Object>>) element
		        .getParameterValue(MapComponent.PARAMETER_PATHS);
		StringBuilder currentPaths = new StringBuilder();
		if (pathList != null && !pathList.isEmpty()) {
			for (Map<String, Object> pathMap : pathList) {
				if (pathMap != null && !pathMap.isEmpty()) {
					currentPaths.append("&path=");
					String color = (String) pathMap.get(MapComponent.ITEM_PROPERTY_STYLE_strokeColor);
					if (color != null && color.length() > 0) {
						// adding opacity to color
						color = JRColorUtil.getColorHexa(JRColorUtil.getColor(color, Color.BLACK));
						color += pathMap.get(MapComponent.ITEM_PROPERTY_STYLE_strokeOpacity) == null
						        || pathMap.get(MapComponent.ITEM_PROPERTY_STYLE_strokeOpacity).toString().length() == 0 ? "ff"
						        : Integer.toHexString((int) (255 * Double.valueOf(pathMap.get(
						                MapComponent.ITEM_PROPERTY_STYLE_strokeOpacity).toString())));
					}
					currentPaths.append(color != null && color.length() > 0 ? "color:0x" + color.toLowerCase() + "%7C"
					        : "");
					Boolean isPolygon = pathMap.get(MapComponent.ITEM_PROPERTY_STYLE_isPolygon) == null ? false
					        : Boolean.valueOf(pathMap.get(MapComponent.ITEM_PROPERTY_STYLE_isPolygon).toString());
					if (isPolygon) {
						String fillColor = (String) pathMap.get(MapComponent.ITEM_PROPERTY_STYLE_fillColor);
						if (fillColor != null && fillColor.length() > 0) {
							// adding opacity to fill color
							fillColor = JRColorUtil.getColorHexa(JRColorUtil.getColor(fillColor, Color.WHITE));
							fillColor += pathMap.get(MapComponent.ITEM_PROPERTY_STYLE_fillOpacity) == null
							        || pathMap.get(MapComponent.ITEM_PROPERTY_STYLE_fillOpacity).toString().length() == 0 ? "00"
							        : Integer.toHexString((int) (256 * Double.valueOf(pathMap.get(
							                MapComponent.ITEM_PROPERTY_STYLE_fillOpacity).toString())));
						}
						currentPaths.append(fillColor != null && fillColor.length() > 0 ? "fillcolor:0x"
						        + fillColor.toLowerCase() + "%7C" : "");
					}
					String weight = pathMap.get(MapComponent.ITEM_PROPERTY_STYLE_strokeWeight) == null ? null : pathMap
					        .get(MapComponent.ITEM_PROPERTY_STYLE_strokeWeight).toString();
					currentPaths.append(weight != null && weight.length() > 0 ? "weight:" + Integer.valueOf(weight)
					        + "%7C" : "");
					List<Map<String, Object>> locations = (List<Map<String, Object>>) pathMap
					        .get(MapComponent.PARAMETER_PATH_LOCATIONS);
					Map<String, Object> location = null;
					if (locations != null && !locations.isEmpty()) {
						for (int i = 0; i < locations.size(); i++) {
							location = locations.get(i);
							currentPaths.append(location.get(MapComponent.ITEM_PROPERTY_latitude));
							currentPaths.append(",");
							currentPaths.append(location.get(MapComponent.ITEM_PROPERTY_longitude));
							currentPaths.append(i < locations.size() - 1 ? "%7C" : "");
						}
						if (isPolygon) {
							currentPaths.append("%7C");
							currentPaths.append(locations.get(0).get(MapComponent.ITEM_PROPERTY_latitude));
							currentPaths.append(",");
							currentPaths.append(locations.get(0).get(MapComponent.ITEM_PROPERTY_longitude));
						}
					}
				}
			}
		}

		StringBuilder im = new StringBuilder("http://maps.googleapis.com/maps/api/staticmap?");
		if (markers.length() == 0 && zoom >= 0) {
			im.append("center=").append(latitude).append(",").append(longitude).append("&zoom=").append(zoom)
			        .append("&");
		}
		im.append("size=" + element.getWidth() + "x" + element.getHeight()
		        + (mapType == null ? "" : "&maptype=" + mapType) + (mapFormat == null ? "" : "&format=" + mapFormat)
		        + (mapScale == null ? "" : "&scale=" + mapScale));
		String imageLocation = im.toString();
		String params = "&sensor=false" + (reqParams == null ? "" : reqParams);

		// a static map url is limited to 2048 characters
		imageLocation += imageLocation.length() + markers.length() + currentPaths.length() + params.length() < MAX_URL_LENGTH ? markers
		        + currentPaths.toString() + params
		        : imageLocation.length() + markers.length() + params.length() < MAX_URL_LENGTH ? markers + params
		                : params;
		JRBasePrintImage printImage = new JRBasePrintImage(element.getDefaultStyleProvider());

		printImage.setUUID(element.getUUID());
		printImage.setX(element.getX());
		printImage.setY(element.getY());
		printImage.setWidth(element.getWidth());
		printImage.setHeight(element.getHeight());
		printImage.setStyle(element.getStyle());
		printImage.setMode(element.getModeValue());
		printImage.setBackcolor(element.getBackcolor());
		printImage.setForecolor(element.getForecolor());

		// FIXMEMAP there are no scale image and alignment attributes defined
		// for the map element
		printImage.setScaleImage(ScaleImageEnum.RETAIN_SHAPE);
		printImage.setHorizontalImageAlign(HorizontalImageAlignEnum.LEFT);
		printImage.setVerticalImageAlign(VerticalImageAlignEnum.TOP);

		OnErrorTypeEnum onErrorType = element.getParameterValue(MapComponent.PARAMETER_ON_ERROR_TYPE) == null ? MapComponent.DEFAULT_ON_ERROR_TYPE
		        : OnErrorTypeEnum.getByName((String) element.getParameterValue(MapComponent.PARAMETER_ON_ERROR_TYPE));
		printImage.setOnErrorType(onErrorType);

		Renderable cacheRenderer = (Renderable) element.getParameterValue(MapComponent.PARAMETER_CACHE_RENDERER);

		if (cacheRenderer == null) {
			cacheRenderer = RendererUtil.getInstance(jasperReportsContext).getNonLazyRenderable(imageLocation,
			        onErrorType);
			if (cacheRenderer != null) {
				element.setParameterValue(MapComponent.PARAMETER_CACHE_RENDERER, cacheRenderer);
			}
		}

		printImage.setRenderer(cacheRenderer);

		return printImage;
	}

}
