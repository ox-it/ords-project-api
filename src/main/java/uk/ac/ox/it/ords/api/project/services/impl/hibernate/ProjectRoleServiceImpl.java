package uk.ac.ox.it.ords.api.project.services.impl.hibernate;

import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.it.ords.api.project.model.Permission;
import uk.ac.ox.it.ords.api.project.model.UserRole;
import uk.ac.ox.it.ords.api.project.services.ProjectRoleService;

public class ProjectRoleServiceImpl implements ProjectRoleService {
	
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
		Transaction transaction = session.beginTransaction();
		try {
			//
			// Assign the principal to the owner role
			//
			UserRole owner = new UserRole();
			owner.setPrincipalName(SecurityUtils.getSubject().getPrincipal().toString());
			owner.setRole("owner_"+projectId);
			session.save(owner);
			transaction.commit();

			//
			// Create the permissions for roles associated with the project
			//
			createPermissionsForProject(projectId);
			
		} catch (HibernateException e) {
			System.out.println("Error!!!!!"+e.getCause());
			log.error("Error creating Project", e);
			transaction.rollback();
			throw new Exception("Cannot create project",e);
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
		createPermission(ownerRole, "project:*:"+projectId);
		
		//
		// Contributor
		//
		String contributorRole = "contributor_"+projectId;
		createPermission(contributorRole, "project:view:"+projectId);
		
		//
		// Viewer
		//
		String viewerRole = "viewer_"+projectId;
		createPermission(viewerRole, "project:view:"+projectId);
	}
	
	private void createPermission(String role, String permissionString) throws Exception{
		Session session = this.sessionFactory.getCurrentSession();
		Transaction transaction = session.beginTransaction();
		try {
			Permission permission = new Permission();
			permission.setRole(role);
			permission.setPermission(permissionString);
			session.save(permission);
			transaction.commit();
		} catch (Exception e) {
			log.error("Error creating permission", e);
			transaction.rollback();
			throw new Exception("Cannot create project",e);
		}
	}

	public void addUserRoleToProject(int projectId, String principalName,
			String role) throws Exception {
		Session session = this.sessionFactory.getCurrentSession();
		try {
			session.beginTransaction();
			UserRole userRole = new UserRole();
			userRole.setPrincipalName(principalName);
			String projectRole = role+"_"+projectId;
			userRole.setRole(projectRole);
			session.save(userRole);
			session.getTransaction().commit();
		} catch (HibernateException e) {
			log.error("Error creating user role", e);
			session.getTransaction().rollback();
			throw new Exception("Cannot create project",e);
		}
		
	}

}
