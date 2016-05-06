package ${package}.application.service;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ocs.dynamo.dao.BaseDao;
import com.ocs.dynamo.service.impl.BaseServiceImpl;

import ${package}.application.domain.SomeEntity;
import ${package}.application.dao.SomeEntityDao;

@Transactional
@Service("entityService")
public class SomeEntityServiceImpl extends BaseServiceImpl<Integer, SomeEntity> implements SomeEntityService {

    @Inject
    private SomeEntityDao entityDao;

    @Override
    protected BaseDao<Integer, SomeEntity> getDao() {
        return entityDao;
    }
}
