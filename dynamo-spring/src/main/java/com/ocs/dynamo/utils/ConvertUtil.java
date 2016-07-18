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
package com.ocs.dynamo.utils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Locale;

import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.ui.converter.WeekCodeConverter;
import com.ocs.dynamo.ui.utils.VaadinUtils;

/**
 * Utility for converting between data types
 * 
 * @author bas.rutten
 *
 */
public final class ConvertUtil {

    private ConvertUtil() {
    }

    /**
     * Converts the search value from the presentation to the model
     * 
     * @param attributeModel
     *            the attribute model that governs the conversion
     * @param input
     *            the search value to convert
     * @return
     */
    public static Object convertSearchValue(AttributeModel attributeModel, Object input,
            Locale locale) {
        if (input == null) {
            return null;
        }

        boolean grouping = SystemPropertyUtils.useThousandsGroupingInEditMode();

        if (attributeModel.isWeek()) {
            WeekCodeConverter converter = new WeekCodeConverter();
            return converter.convertToModel((String) input, Date.class, null);
        } else if (Integer.class.equals(attributeModel.getType())) {
            return VaadinUtils.stringToInteger(grouping, (String) input, locale);
        } else if (Long.class.equals(attributeModel.getType())) {
            return VaadinUtils.stringToLong(grouping, (String) input, locale);
        } else if (BigDecimal.class.equals(attributeModel.getType())) {
            return VaadinUtils.stringToBigDecimal(attributeModel.isPercentage(), grouping,
                    attributeModel.isCurrency(), (String) input, locale);
        }
        return input;
    }

}
