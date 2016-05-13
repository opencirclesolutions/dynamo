package ${package}.application.dao;

import org.springframework.stereotype.Repository;

import com.mysema.query.types.path.EntityPathBase;
import com.ocs.dynamo.dao.impl.BaseDaoImpl;

import ${package}.application.domain.MyEntity;

@Repository("entityDao")
public class MyEntityDaoImpl extends BaseDaoImpl<Integer, MyEntity> implements MyEntityDao {

    /*
     * (non-Javadoc)
     * 
     * @see com.ocs.dynamo.dao.BaseDao#getMyEntityClass()
     */
    public Class<MyEntity> getEntityClass() {
        return MyEntity.class;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ocs.dynamo.dao.impl.BaseDaoImpl#getDslRoot()
     */
    @Override
    protected EntityPathBase<MyEntity> getDslRoot() {
        return null;
    }

}
