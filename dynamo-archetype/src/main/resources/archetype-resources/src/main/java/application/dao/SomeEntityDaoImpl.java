package ${package}.application.dao;

import org.springframework.stereotype.Repository;

import com.mysema.query.types.path.EntityPathBase;
import com.ocs.dynamo.dao.impl.BaseDaoImpl;

import ${package}.application.domain.SomeEntity;

@Repository("entityDao")
public class SomeEntityDaoImpl extends BaseDaoImpl<Integer, SomeEntity> implements SomeEntityDao {

    /*
     * (non-Javadoc)
     * 
     * @see com.ocs.dynamo.dao.BaseDao#getSomeEntityClass()
     */
    public Class<SomeEntity> getEntityClass() {
        return SomeEntity.class;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ocs.dynamo.dao.impl.BaseDaoImpl#getDslRoot()
     */
    @Override
    protected EntityPathBase<SomeEntity> getDslRoot() {
        return null;
    }

}
