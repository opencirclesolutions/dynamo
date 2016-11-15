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

import org.springframework.stereotype.Repository;

import com.mysema.query.types.path.EntityPathBase;
import com.ocs.dynamo.dao.impl.BaseDaoImpl;

/**
 * Dynamo advocates the use of Data Access Objects per domain object to implement the data access
 * logic. It has a Base Data Acces Object class that contains the standard functionality. Special
 * features can be added in this subclass.
 */
@Repository("movieDao")
public class MovieDaoImpl extends BaseDaoImpl<Integer, Movie> implements MovieDao {

    @Override
    public Class<Movie> getEntityClass() {
        return Movie.class;
    }

    @Override
    protected EntityPathBase<Movie> getDslRoot() {
        return null;
    }

}
