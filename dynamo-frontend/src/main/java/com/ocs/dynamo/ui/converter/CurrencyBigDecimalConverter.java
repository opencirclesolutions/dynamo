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

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;

/**
 * A converter for a BigDecimal field that includes a currency symbol.
 * 
 * @author bas.rutten
 */
public class CurrencyBigDecimalConverter extends BigDecimalConverter {

	private static final long serialVersionUID = -8785156070280947096L;

	/**
	 * The currency symbol
	 */
	private String currencySymbol;

	/**
	 * 
	 * Constructor for CurrencyBigDecimalConverter.
	 * 
	 * @param message
	 * @param precision
	 * @param useGrouping
	 * @param currencySymbol
	 */
	public CurrencyBigDecimalConverter(String message, int precision, boolean useGrouping, String currencySymbol) {
		super(message, precision, useGrouping);
		this.currencySymbol = currencySymbol;
	}

	@Override
	public Result<BigDecimal> convertToModel(String value, ValueContext context) {
		if (value == null) {
			return Result.ok(null);
		}

		value = value.replace(" ", "").trim();
		if (!StringUtils.isEmpty(value) && !value.startsWith(currencySymbol)) {
			value = currencySymbol + value;
		}
		return super.convertToModel(value, context);
	}

	@Override
	protected DecimalFormat constructFormat(Locale locale) {
		DecimalFormat nf = (DecimalFormat) DecimalFormat.getCurrencyInstance(locale);
		nf.applyPattern("¤####,###,###,###.##");

		DecimalFormatSymbols s = nf.getDecimalFormatSymbols();
		s.setCurrencySymbol(currencySymbol);
		nf.setDecimalFormatSymbols(s);
		return nf;
	}

	public String getCurrencySymbol() {
		return currencySymbol;
	}

	public void setCurrencySymbol(String currencySymbol) {
		this.currencySymbol = currencySymbol;
	}
}
