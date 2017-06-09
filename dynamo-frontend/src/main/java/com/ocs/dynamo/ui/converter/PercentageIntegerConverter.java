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

import java.util.Locale;

public class PercentageIntegerConverter extends GroupingStringToIntegerConverter {

    private static final long serialVersionUID = -3063510923788897054L;

    public PercentageIntegerConverter(boolean useGrouping) {
        super(useGrouping);
    }

    @Override
    public String convertToPresentation(Integer value, Class<? extends String> targetType, Locale locale) {
        String result = super.convertToPresentation(value, targetType, locale);
        return result == null ? null : result + "%";
    }

    @Override
    public Integer convertToModel(String value, Class<? extends Integer> targetType, Locale locale) {
        value = value == null ? null : value.replaceAll("%", "");
        return super.convertToModel(value, targetType, locale);
    }
}
