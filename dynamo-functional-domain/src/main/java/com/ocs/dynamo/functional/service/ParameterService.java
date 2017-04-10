package com.ocs.dynamo.functional.service;

import com.ocs.dynamo.functional.domain.Parameter;
import com.ocs.dynamo.service.BaseService;

/**
 * Created by R.E.M. Claassen on 7-4-2017.
 */
public interface ParameterService extends BaseService<Integer, Parameter> {

	public Integer getValueAsInteger(String parameterName);
	public Boolean getValueAsBoolean(String parameterName);
	public String getValueAsString(String parameterName);
}
