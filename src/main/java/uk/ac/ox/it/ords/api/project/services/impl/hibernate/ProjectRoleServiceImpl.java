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

import org.apache.shiro.SecurityUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.it.ords.api.project.model.Permission;
import uk.ac.ox.it.ords.api.project.model.UserRole;
import uk.ac.ox.it.ords.api.project.permissions.ProjectPermissionSets;
import uk.ac.ox.it.ords.api.project.server.ValidationException;
import uk.ac.ox.it.ords.api.project.services.AuditService;
import uk.ac.ox.it.ords.api.project.services.ProjectRoleService;
import uk.ac.ox.it.ords.api.project.services.impl.AbstractProjectRoleService;

public class ProjectRoleServiceImpl extends AbstractProjectRoleService implements ProjectRoleService {

	private static Logger log = LoggerFactory.getLogger(ProjectRoleServiceImpl.class);

	private SessionFactory sessionFactory;

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public ProjectRoleServiceImpl() {
		setSessionFactory (HibernateUtils.getSessionFactory());
	}

	public void createInitialPermissions(int projectId) throws Exception {
		Session session = this.sessionFactory.getCurrentSession();
		
		try {
			session.beginTransaction();
			//
			// Assign the principal to the owner role
			//
			UserRole owner = new UserRole();
			owner.setPrincipalName(SecurityUtils.getSubject().getPrincipal().toString());
			owner.setRole("owner_"+projectId);
			session.save(owner);
			session.getTransaction().commit();
			//
			// Create the permissions for roles associated with the project
			//
			createPermissionsForProject(projectId);

		} catch (HibernateException e) {
			log.error("Error creating Project", e);
			session.getTransaction().rollback();
			throw new Exception("Cannot create project",e);
		} finally {
			  HibernateUtils.closeSession();
		}
		

	}

	@SuppressWarnings("unchecked")
	public void deletePermissions(int projectId) throws Exception {
		Session session = this.sessionFactory.getCurrentSession();
		try {
			session.beginTransaction();
			//
			// Get the roles associated with the project
			//
			List<UserRole> owners = session.createCriteria(UserRole.class)
					.add(Restrictions.eq("role", "owner_"+projectId)).list();
			List<UserRole> contributors = session.createCriteria(UserRole.class)
					.add(Restrictions.eq("role", "contributor_"+projectId)).list();	
			List<UserRole> viewers = session.createCriteria(UserRole.class)
					.add(Restrictions.eq("role", "viewer_"+projectId)).list();

			//
			// Delete all the permissions for each role, and then each role
			//
			for (UserRole owner : owners){
				deletePermissionsForRole(session, owner);
				session.delete(owner);
			}
			for (UserRole contributor : contributors){
				deletePermissionsForRole(session, contributor);
				session.delete(contributor);
			}
			for (UserRole viewer : viewers){
				deletePermissionsForRole(session, viewer);
				session.delete(viewer);
			}
			session.getTransaction().commit();

		} catch (HibernateException e) {
			log.error("Error removing roles and permissions", e);
			session.getTransaction().rollback();
			throw new Exception("Cannot revoke project permissions",e);
		} finally {
			  HibernateUtils.closeSession();
		}

	}

	@SuppressWarnings("unchecked")
	private void deletePermissionsForRole(Session session, UserRole role){
		List<Permission> permissions = session.createCriteria(Permission.class)
				.add(Restrictions.eq("role", role.getRole())).list();
		for (Permission permission : permissions){
			session.delete(permission);
		}
	}

	/**
	 * Each project has a set of roles and permissions
	 * associated with it.
	 * 
	 * By default these are:
	 * 
	 *   owner_{projectId}
	 *   contributor_{projectId}
	 *   viewer_{projectId}
	 *   
	 * @param projectId
	 * @throws Exception 
	 */
	private void createPermissionsForProject(int projectId) throws Exception{
		//
		// Owner
		//
		String ownerRole = "owner_"+projectId;
		for (String permission : ProjectPermissionSets.getPermissionsForOwner(projectId)){
			createPermission(ownerRole, permission);			
		}

		//
		// Contributor
		//
		String contributorRole = "contributor_"+projectId;
		for (String permission : ProjectPermissionSets.getPermissionsForContributor(projectId)){
			createPermission(contributorRole, permission);			
		}

		//
		// Viewer
		//
		String viewerRole = "viewer_"+projectId;
		for (String permission : ProjectPermissionSets.getPermissionsForViewer(projectId)){
			createPermission(viewerRole, permission);			
		}
	}

	private void createPermission(String role, String permissionString) throws Exception{
		Session session = this.sessionFactory.getCurrentSession();
		session.beginTransaction();
		try {
			Permission permission = new Permission();
			permission.setRole(role);
			permission.setPermission(permissionString);
			session.save(permission);
			session.getTransaction().commit();
		} catch (Exception e) {
			log.error("Error creating permission", e);
			session.getTransaction().rollback();
			throw new Exception("Cannot create project",e);
		} finally {
			  HibernateUtils.closeSession();
		}
	}

	public UserRole addUserRoleToProject(int projectId, UserRole userRole) throws ValidationException, Exception {
		Session session = this.sessionFactory.getCurrentSession();
		try {
			session.beginTransaction();
			validate(userRole);
			String projectRole = userRole.getRole()+"_"+projectId;
			userRole.setRole(projectRole);
			session.save(userRole);
			session.getTransaction().commit();
			AuditService.Factory.getInstance().createProjectUser(userRole, projectId);
			return userRole;
		} catch (HibernateException e) {
			log.error("Error creating user role", e);
			session.getTransaction().rollback();
			throw new Exception("Cannot create user role",e);
		} finally {
			  HibernateUtils.closeSession();
		}

	}

	public void removeUserFromRoleInProject(int projectId, int roleId)
			throws ValidationException, Exception {
		
		//
		// First, obtain the UserRole
		//
		UserRole userRole = getUserRole(roleId);
		if (userRole == null) throw new Exception("Cannot find user role");
		//
		// Lets check that the role contains the project id
		//
		if(!userRole.getRole().endsWith(String.valueOf(projectId))){
			throw new ValidationException("Attempt to remove role via another project");
		}

		Session session = this.sessionFactory.getCurrentSession();
		try {
			session.beginTransaction();
			session.delete(userRole);
			session.getTransaction().commit();
			AuditService.Factory.getInstance().deleteProjectRole(userRole, projectId);
		} catch (Exception e) {
			session.getTransaction().rollback();
			log.error("Cannot find user role", e);
			throw new Exception("Cannot find user role",e);
		} finally {
			  HibernateUtils.closeSession();
		}

	}

	public UserRole getUserRole(int roleId) throws Exception {
		Session session = this.sessionFactory.getCurrentSession();
		try {
			session.beginTransaction();
			UserRole userRole = (UserRole) session.get(UserRole.class, roleId);
			session.getTransaction().commit();
			return userRole;
		} catch (Exception e) {
			session.getTransaction().rollback();
			throw e;
		} finally {
			  HibernateUtils.closeSession();
		}
	}

	public List<UserRole> getUserRolesForProject(int projectId) throws Exception {
		Session session = this.sessionFactory.getCurrentSession();
		
		try {
			session.beginTransaction();
			@SuppressWarnings("unchecked")
			List<UserRole> userRoles = session.createCriteria(UserRole.class)
					.add(Restrictions.like("role", "%_"+projectId))
					.list();
			session.getTransaction().commit();
			return userRoles;
		} catch (Exception e) {
			session.getTransaction().rollback();
			throw e;
		} finally {
			  HibernateUtils.closeSession();
		}
	}

}
