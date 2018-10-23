package com.ocs.dynamo.dao.impl;

import com.ocs.dynamo.domain.CascadeEntity;
import com.querydsl.core.types.dsl.EntityPathBase;
import org.springframework.stereotype.Repository;

@Repository("cascadeEntityDao")
public class CascadeEntityDaoImpl extends BaseDaoImpl<Integer, CascadeEntity> implements CascadeEntityDao {

    @Override
    public Class<CascadeEntity> getEntityClass() {
        return CascadeEntity.class;
    }

    @Override
    protected EntityPathBase<CascadeEntity> getDslRoot() {
        return null;
    }

}
