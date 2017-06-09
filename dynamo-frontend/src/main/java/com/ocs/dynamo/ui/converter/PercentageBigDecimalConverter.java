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
import java.util.Locale;

/**
 * A BigDecimal converter that adds or removes a percentage sign
 * 
 * @author bas.rutten
 */
public class PercentageBigDecimalConverter extends BigDecimalConverter {

    private static final long serialVersionUID = 5566274434473612396L;

    /**
     * Constructor
     * 
     * @param precision
     * @param useGrouping
     */
    public PercentageBigDecimalConverter(int precision, boolean useGrouping) {
        super(precision, useGrouping);
    }

    @Override
    public String convertToPresentation(BigDecimal value, Class<? extends String> targetType,
            Locale locale) {
        String result = super.convertToPresentation(value, targetType, locale);
        return result == null ? null : result + "%";
    }

    @Override
    public BigDecimal convertToModel(String value, Class<? extends BigDecimal> targetType,
            Locale locale) {
        value = value == null ? null : value.replaceAll("%", "");
        return super.convertToModel(value, targetType, locale);
    }
}
