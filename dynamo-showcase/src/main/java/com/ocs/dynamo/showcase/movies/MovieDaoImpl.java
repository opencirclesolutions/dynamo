package com.ocs.dynamo.showcase.movies;

import org.springframework.stereotype.Repository;

import com.mysema.query.types.path.EntityPathBase;
import com.ocs.dynamo.dao.impl.BaseDaoImpl;

@Repository("movieDao")
public class MovieDaoImpl extends BaseDaoImpl<Integer, Movie> implements MovieDao {

    /*
     * (non-Javadoc)
     * 
     * @see com.ocs.dynamo.dao.BaseDao#getEntityClass()
     */
    public Class<Movie> getEntityClass() {
        // TODO Auto-generated method stub
        return Movie.class;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ocs.dynamo.dao.impl.BaseDaoImpl#getDslRoot()
     */
    @Override
    protected EntityPathBase<Movie> getDslRoot() {
        // TODO Auto-generated method stub
        return null;
    }

}
