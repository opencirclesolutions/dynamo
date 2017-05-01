package com.ocs.dynamo.dao.impl;

import org.springframework.stereotype.Repository;

import com.mysema.query.types.path.EntityPathBase;
import com.ocs.dynamo.dao.CascadeEntityDao;
import com.ocs.dynamo.domain.CascadeEntity;

@Repository("cascadeEntityDao")
public class CascadeEntityDaoImpl extends BaseDaoImpl<Integer, CascadeEntity> implements CascadeEntityDao {

    @Override
    public Class<CascadeEntity> getEntityClass() {
        return CascadeEntity.class;
    }

    @Override
    protected EntityPathBase<CascadeEntity> getDslRoot() {
        // TODO Auto-generated method stub
        return null;
    }

}
