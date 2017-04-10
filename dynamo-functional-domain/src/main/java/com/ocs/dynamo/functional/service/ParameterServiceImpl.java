package com.ocs.dynamo.functional.service;

import com.ocs.dynamo.dao.BaseDao;
import com.ocs.dynamo.functional.dao.ParameterDao;
import com.ocs.dynamo.functional.domain.Parameter;
import com.ocs.dynamo.functional.domain.ParameterType;
import com.ocs.dynamo.service.impl.BaseServiceImpl;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * Created by R.E.M. Claassen on 7-4-2017.
 */
@Service("parameterService")
public class ParameterServiceImpl extends BaseServiceImpl<Integer,Parameter> implements ParameterService {

	@Inject
	ParameterDao parameterDao;

	@Override
	protected BaseDao getDao() {
		return parameterDao;
	}

	@Override
	public Integer getValueAsInteger(String parameterName){
		Parameter parameter = this.findByUniqueProperty("name", parameterName, false);

		if(parameter != null && ParameterType.INTEGER.equals(parameter.getParameterType())){
			return Integer.valueOf(parameter.getValue());
		}

		return null;
	}

	@Override
	public Boolean getValueAsBoolean(String parameterName) {
		Parameter parameter = this.findByUniqueProperty("name", parameterName, false);

		if(parameter != null && ParameterType.BOOLEAN.equals(parameter.getParameterType())){
			return Boolean.valueOf(parameter.getValue());
		}

		return null;
	}

	@Override
	public String getValueAsString(String parameterName) {
		Parameter parameter = this.findByUniqueProperty("name", parameterName, false);

		if(parameter != null && ParameterType.STRING.equals(parameter.getParameterType())){
			return parameter.getValue();
		}

		return null;
	}

}
