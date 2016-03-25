package com.ocs.dynamo.showcase.movies;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ocs.dynamo.dao.BaseDao;
import com.ocs.dynamo.service.impl.BaseServiceImpl;

@Transactional
@Service("movieService")
public class MovieServiceImpl extends BaseServiceImpl<Integer, Movie> implements MovieService {

    @Inject
    private MovieDao movieDao;

    @Override
    protected BaseDao<Integer, Movie> getDao() {
        return movieDao;
    }
}
