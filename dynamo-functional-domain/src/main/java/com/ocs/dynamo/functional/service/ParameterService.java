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
package com.ocs.dynamo.functional.service;

import com.ocs.dynamo.functional.domain.Parameter;
import com.ocs.dynamo.service.BaseService;

/**
 * Created by R.E.M. Claassen on 7-4-2017.
 */
public interface ParameterService extends BaseService<Integer, Parameter> {

    /**
     * Returns the value of a parameter as an integer
     * 
     * @param parameterName
     *            the name of the parameter
     * @return
     */
    Integer getValueAsInteger(String parameterName);

    /**
     * Returns the value of a parameter as a boolean
     * 
     * @param parameterName
     *            the name of the parameter
     * @return
     */
    Boolean getValueAsBoolean(String parameterName);

    /**
     * Returns the value of a parameter as a string
     * 
     * @param parameterName
     *            the name of the parameter
     * @return
     */
    String getValueAsString(String parameterName);
}
