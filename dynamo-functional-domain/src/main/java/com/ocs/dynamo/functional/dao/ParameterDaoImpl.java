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
package com.ocs.dynamo.functional.dao;

import com.ocs.dynamo.dao.impl.BaseDaoImpl;
import com.ocs.dynamo.functional.domain.Parameter;
import com.ocs.dynamo.functional.domain.QParameter;
import com.querydsl.core.types.dsl.EntityPathBase;
import org.springframework.stereotype.Repository;

/**
 * Created by R.E.M. Claassen on 6-4-2017.
 */
@Repository("parameterDao")
public class ParameterDaoImpl extends BaseDaoImpl<Integer, Parameter> implements ParameterDao {

    @Override
    public Class<Parameter> getEntityClass() {
        return Parameter.class;
    }

    @Override
    protected EntityPathBase<Parameter> getDslRoot() {
        return QParameter.parameter;
    }
}
