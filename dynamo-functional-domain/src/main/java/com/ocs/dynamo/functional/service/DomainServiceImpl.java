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
package com.ocs.dynamo.functional.service;

import com.ocs.dynamo.dao.BaseDao;
import com.ocs.dynamo.functional.dao.DomainDao;
import com.ocs.dynamo.functional.domain.Domain;
import com.ocs.dynamo.functional.domain.DomainChild;
import com.ocs.dynamo.functional.domain.DomainParent;
import com.ocs.dynamo.service.impl.DefaultServiceImpl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

import java.util.List;

/**
 * Service for working with reference information.
 * 
 * @author Patrick Deenen (patrick@opencircle.solutions)
 *
 */
@Transactional
@Service("domainService")
public class DomainServiceImpl extends DefaultServiceImpl<Integer, Domain> implements DomainService {

	/** data access object for domain */
	@Inject
	private DomainDao domainDao;

	public DomainServiceImpl() {
		super(null);
	}

	public DomainServiceImpl(DomainDao dao) {
		super(dao);
		domainDao = dao;
	}

	@Override
	protected BaseDao<Integer, Domain> getDao() {
		if (super.getDao() != null) {
			return super.getDao();
		}
		return domainDao;
	}

	@Override
	public <C extends DomainChild<C, P>, P extends DomainParent<C, P>> List<C> findChildren(P parent) {
		return domainDao.findChildren(parent);
	}

	@Override
	public <D extends Domain> List<D> findAllByType(Class<D> type) {
		return domainDao.findAllByType(type);
	}

}
