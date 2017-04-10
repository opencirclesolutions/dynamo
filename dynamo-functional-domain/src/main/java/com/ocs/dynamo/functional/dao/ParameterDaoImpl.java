package com.ocs.dynamo.functional.dao;

import com.mysema.query.types.path.EntityPathBase;
import com.ocs.dynamo.dao.impl.BaseDaoImpl;
import com.ocs.dynamo.functional.domain.Parameter;
import com.ocs.dynamo.functional.domain.QParameter;
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
