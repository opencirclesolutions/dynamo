/*
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.ocs.dynamo.showcase.movies;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ocs.dynamo.dao.BaseDao;
import com.ocs.dynamo.service.impl.BaseServiceImpl;

/**
 * Dynamo advocates the use of a Facade to implement business services. It has a Abstract Service
 * class that contains the standard functionality. Special features can be added in this subclass.
 */
@Transactional
@Service("movieService")
public class MovieServiceImpl extends BaseServiceImpl<Integer, Movie> implements MovieService {

    /** Data Access Object for Movie. */
    @Inject
    private MovieDao movieDao;

    /*
     * (non-Javadoc)
     * 
     * @see com.ocs.dynamo.service.impl.BaseServiceImpl#getDao()
     */
    @Override
    protected BaseDao<Integer, Movie> getDao() {
        return movieDao;
    }
}
