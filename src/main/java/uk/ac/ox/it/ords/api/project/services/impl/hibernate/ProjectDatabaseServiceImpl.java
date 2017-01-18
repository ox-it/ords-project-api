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
import uk.ac.ox.it.ords.api.project.model.DatabaseVersion;
import uk.ac.ox.it.ords.api.project.permissions.ProjectPermissions;
import uk.ac.ox.it.ords.api.project.services.DatabaseVersionService;
import uk.ac.ox.it.ords.api.project.services.ProjectDatabaseService;
import uk.ac.ox.it.ords.api.project.services.impl.AbstractProjectDatabaseService;
import uk.ac.ox.it.ords.security.model.Permission;
import uk.ac.ox.it.ords.security.permissions.Permissions;
import uk.ac.ox.it.ords.security.services.ODBCService;
import uk.ac.ox.it.ords.security.services.PermissionsService;

public class ProjectDatabaseServiceImpl extends AbstractProjectDatabaseService implements ProjectDatabaseService {

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
	public Database getDatabase(int id, int projectId) throws Exception {
		
		Database projectDatabase = null;

		Session session = this.sessionFactory.getCurrentSession();
		try {
			session.beginTransaction();
			projectDatabase = (Database) session.createCriteria(Database.class)
					.add(Restrictions.eq("databaseProjectId", projectId))
					.add(Restrictions.eq("logicalDatabaseId", id))
					.uniqueResult();
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
		} catch (Exception e) {
			log.debug(e.getMessage());
			session.getTransaction().rollback();
			throw e;
		} finally {
			HibernateUtils.closeSession();
		}
		initialisePermissions(projectDatabase);
		return projectDatabase;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.it.ords.api.project.services.ProjectDatabaseService#removeDatabaseFromProject(int, int)
	 */
	@Override
	public void removeDatabase(int databaseId)
			throws Exception {
		Database projectDatabase = this.getDatabase(databaseId);
		revokePermissions(projectDatabase);
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

	@Override
	public Database updateDatabase(Database database) throws Exception {
		Session session = this.sessionFactory.getCurrentSession();
		try {
			session.beginTransaction();
			session.update(database);
			session.getTransaction().commit();
		} catch (Exception e) {
			log.debug(e.getMessage());
			session.getTransaction().rollback();
			throw e;
		} finally {
			  HibernateUtils.closeSession();
		}
		return database;
	}
	
	@Override
	public void enableODBC(int projectId) throws Exception {
		List<Database> databases = ProjectDatabaseService.Factory.getInstance().getDatabasesForProject(projectId);
		for (Database database : databases){
			enableODBC(database);
		}
	}

	@Override
	public void disableODBC(int projectId) throws Exception {
		List<Database> databases = ProjectDatabaseService.Factory.getInstance().getDatabasesForProject(projectId);
		for (Database database : databases){
			disableODBC(database);
		}
	}

	@Override
	public void enableODBC(Database database) throws Exception {
		
		String ownerRole = "owner_"+database.getDatabaseProjectId();
		String contributorRole = "contributor_"+database.getDatabaseProjectId();
		String viewerRole = "viewer_"+database.getDatabaseProjectId();
		
		Permission ownerPermission = new Permission();
		ownerPermission.setRole(ownerRole);
		ownerPermission.setPermission(ProjectPermissions.DATABASE_REQUEST_ODBC_ACCESS(database));
		PermissionsService.Factory.getInstance().createPermission(ownerPermission);

		Permission contributorPermission = new Permission();
		contributorPermission.setRole(contributorRole);
		contributorPermission.setPermission(ProjectPermissions.DATABASE_REQUEST_ODBC_ACCESS(database));
		PermissionsService.Factory.getInstance().createPermission(contributorPermission);

		Permission viewerPermission = new Permission();
		viewerPermission.setRole(viewerRole);
		viewerPermission.setPermission(ProjectPermissions.DATABASE_REQUEST_ODBC_ACCESS(database));
		PermissionsService.Factory.getInstance().createPermission(viewerPermission);
	}

	@Override
	public void disableODBC(Database database) throws Exception {

		String ownerRole = "owner_"+database.getDatabaseProjectId();
		String contributorRole = "contributor_"+database.getDatabaseProjectId();
		String viewerRole = "viewer_"+database.getDatabaseProjectId();

		// Collect all permissions
		List<Permission> permissions = PermissionsService.Factory.getInstance().getPermissionsForRole(ownerRole);
		permissions.addAll(PermissionsService.Factory.getInstance().getPermissionsForRole(contributorRole));
		permissions.addAll(PermissionsService.Factory.getInstance().getPermissionsForRole(viewerRole));

		// Revoke all ODBC permissions on this database
		for (Permission permission : permissions){
			if (permission.getPermission().equals(ProjectPermissions.DATABASE_REQUEST_ODBC_ACCESS(database))){
				PermissionsService.Factory.getInstance().deletePermission(permission);
			}
		}
		
		// Revoke all previously created ODBC roles for this database
		removeODBCroles(DatabaseVersionService.Factory.getInstance().getDatabaseVersions(database.getLogicalDatabaseId()));
	}
	
	/**
	 * Revokes and drops all ODBC roles for the specified databases
	 * @param databaseVersions
	 * @throws Exception
	 */
	private void removeODBCroles(List<DatabaseVersion> databaseVersions) throws Exception {
		for (DatabaseVersion databaseVersion : databaseVersions ){
			List<String> roles = ODBCService.Factory.getInstance().getAllODBCRolesForDatabase(databaseVersion.getDatabaseServer(), databaseVersion.getDbConsumedName());
			for (String role : roles){
				try {
					ODBCService.Factory.getInstance().removeRole(role, databaseVersion.getDatabaseServer(), databaseVersion.getDbConsumedName()+"_staging");
				} catch (Exception e) {
					log.warn("problem dropping ODBC role");
				}
				try {
					ODBCService.Factory.getInstance().removeRole(role, databaseVersion.getDatabaseServer(), databaseVersion.getDbConsumedName());
				} catch (Exception e) {
					log.warn("problem dropping ODBC role");
				}
			}			
		}
	}
		
	/**
	 * Create initial permissions for a new database
	 * @param database
	 * @throws Exception
	 */
	private void initialisePermissions(Database database) throws Exception {
		
		String ownerRole = "owner_"+database.getDatabaseProjectId();
		String contributorRole = "contributor_"+database.getDatabaseProjectId();
		String viewerRole = "viewer_"+database.getDatabaseProjectId();
		
		Permission ownerPermission = new Permission();
		ownerPermission.setRole(ownerRole);
		ownerPermission.setPermission(Permissions.DATABASE_MODIFY(database.getLogicalDatabaseId()));
		PermissionsService.Factory.getInstance().createPermission(ownerPermission);
		Permission ownerPermission2 = new Permission();
		ownerPermission2.setRole(ownerRole);
		ownerPermission2.setPermission(Permissions.DATABASE_DELETE(database.getLogicalDatabaseId()));
		PermissionsService.Factory.getInstance().createPermission(ownerPermission2);
		Permission ownerPermission3 = new Permission();
		ownerPermission3.setRole(ownerRole);
		ownerPermission3.setPermission(Permissions.DATABASE_VIEW(database.getLogicalDatabaseId()));
		PermissionsService.Factory.getInstance().createPermission(ownerPermission3);

		Permission contributorPermission = new Permission();
		contributorPermission.setRole(contributorRole);
		contributorPermission.setPermission(Permissions.DATABASE_MODIFY(database.getLogicalDatabaseId()));
		PermissionsService.Factory.getInstance().createPermission(contributorPermission);

		Permission viewerPermission = new Permission();
		viewerPermission.setRole(viewerRole);
		viewerPermission.setPermission(Permissions.DATABASE_VIEW(database.getLogicalDatabaseId()));
		PermissionsService.Factory.getInstance().createPermission(viewerPermission);
	}
	
	private void revokePermissions(Database database) throws Exception {

		String ownerRole = "owner_"+database.getDatabaseProjectId();
		String contributorRole = "contributor_"+database.getDatabaseProjectId();
		String viewerRole = "viewer_"+database.getDatabaseProjectId();

		// Collect all permissions
		List<Permission> permissions = PermissionsService.Factory.getInstance().getPermissionsForRole(ownerRole);
		permissions.addAll(PermissionsService.Factory.getInstance().getPermissionsForRole(contributorRole));
		permissions.addAll(PermissionsService.Factory.getInstance().getPermissionsForRole(viewerRole));

		// Revoke all permissions on this database
		for (Permission permission : permissions){
			if (permission.getPermission().equals(Permissions.DATABASE_VIEW(database.getLogicalDatabaseId()))){
				PermissionsService.Factory.getInstance().deletePermission(permission);
			}
			if (permission.getPermission().equals(Permissions.DATABASE_MODIFY(database.getLogicalDatabaseId()))){
				PermissionsService.Factory.getInstance().deletePermission(permission);
			}
			if (permission.getPermission().equals(Permissions.DATABASE_DELETE(database.getLogicalDatabaseId()))){
				PermissionsService.Factory.getInstance().deletePermission(permission);
			}
		}
	}
}
