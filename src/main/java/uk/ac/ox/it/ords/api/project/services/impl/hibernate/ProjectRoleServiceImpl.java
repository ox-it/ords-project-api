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

import uk.ac.ox.it.ords.api.project.model.User;
import uk.ac.ox.it.ords.api.project.permissions.ProjectPermissionSets;
import uk.ac.ox.it.ords.api.project.server.ValidationException;
import uk.ac.ox.it.ords.api.project.services.ProjectAuditService;
import uk.ac.ox.it.ords.api.project.services.ProjectRoleService;
import uk.ac.ox.it.ords.api.project.services.UserService;
import uk.ac.ox.it.ords.api.project.services.impl.AbstractProjectRoleService;
import uk.ac.ox.it.ords.security.model.Permission;
import uk.ac.ox.it.ords.security.model.UserRole;

public class ProjectRoleServiceImpl extends AbstractProjectRoleService implements ProjectRoleService {

	private static Logger log = LoggerFactory.getLogger(ProjectRoleServiceImpl.class);

	protected SessionFactory sessionFactory;

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public ProjectRoleServiceImpl() {
		setSessionFactory (HibernateUtils.getSessionFactory());
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.it.ords.api.project.services.ProjectRoleService#updateProjectRole(uk.ac.ox.it.ords.security.model.UserRole, int)
	 */
	@Override
	public void updateProjectRole(UserRole userRole, int projectId) throws Exception {
		validate(userRole);
		//
		// We need to change "contributor" to "contributor_26" etc
		//
		userRole.setRole(getPrivateUserRole(userRole.getRole(), projectId));
		Session session = this.sessionFactory.getCurrentSession();
		try {
			session.beginTransaction();
			session.update(userRole);
			session.getTransaction().commit();
		} catch (HibernateException e) {
			log.error("Error update UserRole", e);
			session.getTransaction().rollback();
			throw new Exception("Cannot update UserRole",e);
		} finally {
			  HibernateUtils.closeSession();
		}

	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.it.ords.api.project.services.ProjectRoleService#createInitialPermissions(int)
	 */
	@Override
	public void createInitialPermissions(int projectId) throws Exception {
		Session session = this.sessionFactory.getCurrentSession();
		
		try {
			session.beginTransaction();
			//
			// Assign the principal to the owner role
			//
			UserRole owner = new UserRole();
			owner.setPrincipalName(SecurityUtils.getSubject().getPrincipal().toString());
			owner.setRole(getPrivateUserRole("owner", projectId));
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

	/* (non-Javadoc)
	 * @see uk.ac.ox.it.ords.api.project.services.ProjectRoleService#deletePermissions(int)
	 */
	@Override
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

	/**
	 * Delete permissions for a role
	 * @param session the current Hibernate session
	 * @param role the role
	 */
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
	 * @param projectId the project
	 * @throws Exception if there is a problem creating permissions
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

	/**
	 * Creates a permission
	 * @param role the role
	 * @param permissionString the permission
	 * @throws Exception if there is a problem saving the permission
	 */
	protected void createPermission(String role, String permissionString) throws Exception{
		Session session = this.sessionFactory.getCurrentSession();
		try {
			session.beginTransaction();
			Permission permission = new Permission();
			permission.setRole(role);
			permission.setPermission(permissionString);
			session.save(permission);
			session.getTransaction().commit();
		} catch (Exception e) {
			log.error("Error creating permission", e);
			session.getTransaction().rollback();
			throw new Exception("Cannot create permission",e);
		} finally {
			  HibernateUtils.closeSession();
		}
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.it.ords.api.project.services.ProjectRoleService#addUserRoleToProject(int, uk.ac.ox.it.ords.security.model.UserRole)
	 */
	@Override
	public UserRole addUserRoleToProject(int projectId, UserRole userRole) throws ValidationException, Exception {
		
		
		//
		// We may sometimes have user roles created with an email address instead of principal name; in this
		// case we have to look up the principal name directly.
		//
		User user = UserService.Factory.getInstance().getUserByPrincipalName(userRole.getPrincipalName());
		if (user == null){
			user = UserService.Factory.getInstance().getUserByEmailAddress(userRole.getPrincipalName());
			if (user != null){
				userRole.setPrincipalName(user.getPrincipalName());
			}
		}
		
		Session session = this.sessionFactory.getCurrentSession();
		try {
			session.beginTransaction();
			validate(userRole);
			String projectRole = getPrivateUserRole(userRole.getRole(), projectId);
			userRole.setRole(projectRole);
			session.save(userRole);
			session.getTransaction().commit();
			ProjectAuditService.Factory.getInstance().createProjectUser(userRole, projectId);
			return userRole;
		} catch (HibernateException e) {
			log.error("Error creating user role", e);
			session.getTransaction().rollback();
			throw new Exception("Cannot create user role",e);
		} finally {
			  HibernateUtils.closeSession();
		}

	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.it.ords.api.project.services.ProjectRoleService#removeUserFromRoleInProject(int, int)
	 */
	@Override
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
		removeUserRole(userRole, projectId);
	}
	
	/**
	 * Removes a user role
	 * @param userRole the user role to remove
	 * @param projectId the project
	 * @throws Exception if there's a problem removing the user role
	 */
	protected void removeUserRole(UserRole userRole, int projectId) throws Exception{
		Session session = this.sessionFactory.getCurrentSession();
		try {
			session.beginTransaction();
			session.delete(userRole);
			session.getTransaction().commit();
			ProjectAuditService.Factory.getInstance().deleteProjectRole(userRole, projectId);
		} catch (Exception e) {
			session.getTransaction().rollback();
			log.error("Cannot find user role", e);
			throw new Exception("Cannot find user role",e);
		} finally {
			  HibernateUtils.closeSession();
		}
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.it.ords.api.project.services.ProjectRoleService#getUserRole(int)
	 */
	@Override
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

	/* (non-Javadoc)
	 * @see uk.ac.ox.it.ords.api.project.services.ProjectRoleService#getUserRolesForProject(int)
	 */
	@Override
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
