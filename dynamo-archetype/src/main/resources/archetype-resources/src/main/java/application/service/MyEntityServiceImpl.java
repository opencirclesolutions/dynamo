package ${package}.application.service;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ocs.dynamo.dao.BaseDao;
import com.ocs.dynamo.service.impl.BaseServiceImpl;

import ${package}.application.domain.MyEntity;
import ${package}.application.dao.MyEntityDao;

@Transactional
@Service("entityService")
public class MyEntityServiceImpl extends BaseServiceImpl<Integer, MyEntity> implements MyEntityService {

    @Inject
    private MyEntityDao entityDao;

    @Override
    protected BaseDao<Integer, MyEntity> getDao() {
        return entityDao;
    }
}
