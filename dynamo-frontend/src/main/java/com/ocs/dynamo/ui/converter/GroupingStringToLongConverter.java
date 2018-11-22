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
package com.ocs.dynamo.ui.converter;

import java.text.NumberFormat;
import java.util.Locale;

import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.data.converter.StringToLongConverter;

/**
 * A converter from String to Long that allows the user to specify whether a
 * grouping separator must be used
 * 
 * @author bas.rutten
 */
public class GroupingStringToLongConverter extends StringToLongConverter {

	private static final long serialVersionUID = -281060172465120956L;

	private boolean useGrouping;

	public GroupingStringToLongConverter(String message, boolean useGrouping) {
		super(message);
		this.useGrouping = useGrouping;
	}

	@Override
	protected NumberFormat getFormat(Locale locale) {
		NumberFormat format = super.getFormat(locale == null ? VaadinUtils.getLocale() : locale);
		format.setGroupingUsed(useGrouping);
		return format;
	}
}
