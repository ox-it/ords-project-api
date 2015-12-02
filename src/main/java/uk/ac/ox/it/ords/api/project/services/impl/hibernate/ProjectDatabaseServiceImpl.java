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

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.it.ords.api.project.model.Database;
import uk.ac.ox.it.ords.api.project.services.ProjectDatabaseService;

public class ProjectDatabaseServiceImpl implements ProjectDatabaseService {

	private static Logger log = LoggerFactory.getLogger(ProjectDatabaseServiceImpl.class);

	private SessionFactory sessionFactory;

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public ProjectDatabaseServiceImpl() {
		setSessionFactory (HibernateUtils.getSessionFactory());
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.it.ords.api.project.services.ProjectDatabaseService#getDatabaseForProject(int)
	 */
	@Override
	public Database getDatabase(int id) throws Exception {
		
		Database projectDatabase = null;

		Session session = this.sessionFactory.getCurrentSession();
		try {
			session.beginTransaction();
			projectDatabase = (Database) session.get(Database.class, id);
			session.getTransaction().commit();

		} catch (Exception e) {
			log.debug(e.getMessage());
			session.getTransaction().rollback();
			throw e;
		} finally {
			  HibernateUtils.closeSession();
		}
		return projectDatabase;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.it.ords.api.project.services.ProjectDatabaseService#getDatabasesForProject(int)
	 */
	@Override
	public List<Database> getDatabasesForProject(int projectId) throws Exception {
		Session session = this.sessionFactory.getCurrentSession();
		
		try {
			session.beginTransaction();
			@SuppressWarnings("unchecked")
			List<Database> projectDatabases = session.createCriteria(Database.class)
					.add(Restrictions.eq("databaseProjectId", projectId))
					.list();
			session.getTransaction().commit();
			return projectDatabases;
		} catch (Exception e) {
			log.debug(e.getMessage());
			session.getTransaction().rollback();
			throw e;
		} finally {
			  HibernateUtils.closeSession();
		}
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.it.ords.api.project.services.ProjectDatabaseService#addDatabaseToProject(int, uk.ac.ox.it.ords.api.project.model.ProjectDatabase)
	 */
	@Override
	public Database addDatabase(Database projectDatabase) throws Exception {
		Session session = this.sessionFactory.getCurrentSession();
		try {
			session.beginTransaction();
			session.save(projectDatabase);
			session.getTransaction().commit();
			return projectDatabase;
		} catch (Exception e) {
			log.debug(e.getMessage());
			session.getTransaction().rollback();
			throw e;
		} finally {
			  HibernateUtils.closeSession();
		}
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.it.ords.api.project.services.ProjectDatabaseService#removeDatabaseFromProject(int, int)
	 */
	@Override
	public void removeDatabase(int databaseId)
			throws Exception {
		Database projectDatabase = this.getDatabase(databaseId);
		Session session = this.sessionFactory.getCurrentSession();
		try {
			session.beginTransaction();
			session.delete(projectDatabase);
			session.getTransaction().commit();
		} catch (Exception e) {
			log.debug(e.getMessage());
			session.getTransaction().rollback();
			throw e;
		} finally {
			  HibernateUtils.closeSession();
		}
	}
}
