package com.ocs.dynamo.service.impl;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.ocs.dynamo.dao.BaseDao;
import com.ocs.dynamo.dao.CascadeEntityDao;
import com.ocs.dynamo.domain.CascadeEntity;
import com.ocs.dynamo.service.CascadeEntityService;

@Service("cascadeEntityService")
public class CascadeEntityServiceImpl extends BaseServiceImpl<Integer, CascadeEntity> implements CascadeEntityService {

    @Inject
    private CascadeEntityDao dao;

    @Override
    protected BaseDao<Integer, CascadeEntity> getDao() {
        return dao;
    }

}
