/*
 * Copyright 2015 University of Oxford
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ox.it.ords.api.project.services.impl.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.it.ords.api.project.model.DatabaseVersion;
import uk.ac.ox.it.ords.api.project.services.DatabaseVersionService;

public class DatabaseVersionServiceImpl implements DatabaseVersionService {
	
	private static Logger log = LoggerFactory.getLogger(DatabaseVersionServiceImpl.class);

	private SessionFactory sessionFactory;

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public DatabaseVersionServiceImpl() {
		setSessionFactory (HibernateUtils.getSessionFactory());
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.it.ords.api.project.services.DatabaseVersionService#getDatabaseVersions(int)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<DatabaseVersion> getDatabaseVersions(int logicalDatabaseId) {
		List<DatabaseVersion> databases = new ArrayList<DatabaseVersion>();
		Session session = this.sessionFactory.getCurrentSession();
		try {
			session.beginTransaction();
			databases = session.createCriteria(DatabaseVersion.class).add(Restrictions.eq("logicalDatabaseId", logicalDatabaseId)).list();
			session.getTransaction().commit();
		} catch (Exception e) {
			log.debug(e.getMessage());
			session.getTransaction().rollback();
			throw e;
		} finally {
			  HibernateUtils.closeSession();
		}
		return databases;
	}

}
