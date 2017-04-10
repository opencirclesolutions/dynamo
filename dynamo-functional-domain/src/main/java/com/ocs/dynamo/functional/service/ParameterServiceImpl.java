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

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.ocs.dynamo.dao.BaseDao;
import com.ocs.dynamo.functional.dao.ParameterDao;
import com.ocs.dynamo.functional.domain.Parameter;
import com.ocs.dynamo.functional.domain.ParameterType;
import com.ocs.dynamo.service.impl.BaseServiceImpl;

/**
 * Created by R.E.M. Claassen on 7-4-2017.
 */
@Service("parameterService")
public class ParameterServiceImpl extends BaseServiceImpl<Integer, Parameter> implements ParameterService {

    @Inject
    private ParameterDao parameterDao;

    @Override
    protected BaseDao<Integer, Parameter> getDao() {
        return parameterDao;
    }

    @Override
    public Integer getValueAsInteger(String parameterName) {
        Parameter parameter = this.findByUniqueProperty(Parameter.ATTRIBUTE_NAME, parameterName, false);
        if (parameter != null && ParameterType.INTEGER.equals(parameter.getParameterType())) {
            return Integer.valueOf(parameter.getValue());
        }
        return null;
    }

    @Override
    public Boolean getValueAsBoolean(String parameterName) {
        Parameter parameter = this.findByUniqueProperty(Parameter.ATTRIBUTE_NAME, parameterName, false);
        if (parameter != null && ParameterType.BOOLEAN.equals(parameter.getParameterType())) {
            return Boolean.valueOf(parameter.getValue());
        }
        return Boolean.FALSE;
    }

    @Override
    public String getValueAsString(String parameterName) {
        Parameter parameter = this.findByUniqueProperty(Parameter.ATTRIBUTE_NAME, parameterName, false);
        if (parameter != null && ParameterType.STRING.equals(parameter.getParameterType())) {
            return parameter.getValue();
        }
        return null;
    }

}
