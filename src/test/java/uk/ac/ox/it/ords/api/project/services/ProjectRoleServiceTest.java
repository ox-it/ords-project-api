package uk.ac.ox.it.ords.api.project.services;

import org.junit.Test;

import uk.ac.ox.it.ords.api.project.model.UserRole;
import uk.ac.ox.it.ords.api.project.services.impl.hibernate.HibernateUtils;

public class ProjectRoleServiceTest {

	
	@Test (expected = Exception.class)
	public void testExceptionHandlingForCreate() throws Exception{
		ProjectRoleService service = ProjectRoleService.Factory.getInstance();
		service.addUserRoleToProject(-1, null);
	}
	
	@Test (expected = Exception.class)
	public void testExceptionHandlingForCreate1() throws Exception{
		ProjectRoleService service = ProjectRoleService.Factory.getInstance();
		service.addUserRoleToProject(1, null);
	}
	
	@Test (expected = Exception.class)
	public void testExceptionHandlingForCreate2() throws Exception{
		ProjectRoleService service = ProjectRoleService.Factory.getInstance();
		UserRole userRole = new UserRole();
		userRole.setPrincipalName(null);
		userRole.setRole(null);
		service.addUserRoleToProject(1, userRole);
	}
	
	@Test (expected = Exception.class)
	public void testExceptionHandlingForCreate3() throws Exception{
		ProjectRoleService service = ProjectRoleService.Factory.getInstance();
		UserRole userRole = new UserRole();
		userRole.setPrincipalName(null);
		userRole.setRole("viewer");
		service.addUserRoleToProject(1, userRole);
	}
	
	@Test (expected = Exception.class)
	public void testExceptionHandlingForCreate4() throws Exception{
		ProjectRoleService service = ProjectRoleService.Factory.getInstance();
		UserRole userRole = new UserRole();
		userRole.setPrincipalName("bob");
		userRole.setRole(null);
		service.addUserRoleToProject(1, userRole);
	}
	
	
	@Test (expected = Exception.class)
	public void testExceptionHandlingForCreate5() throws Exception{
		ProjectRoleService service = ProjectRoleService.Factory.getInstance();
		UserRole userRole = new UserRole();
		userRole.setPrincipalName("bob");
		userRole.setRole("nosuchroleexists");
		service.addUserRoleToProject(1, userRole);
	}
	
	//
	// Force a hibernate exception and recover
	//
	@Test
	public void testExceptionHandlingForCreate6() throws Exception{
		try {
			ProjectRoleService service = ProjectRoleService.Factory.getInstance();
			HibernateUtils.getSessionFactory().getCurrentSession().beginTransaction();
			UserRole userRole = new UserRole();
			userRole.setPrincipalName("bob");
			userRole.setRole("viewer");
			service.addUserRoleToProject(1, userRole);
		} catch (Exception e) {
			ProjectRoleService service = ProjectRoleService.Factory.getInstance();
			UserRole userRole = new UserRole();
			userRole.setPrincipalName("bob");
			userRole.setRole("viewer");
			service.addUserRoleToProject(1, userRole);
		}
	}
	
	@Test (expected = Exception.class)
	public void testExceptionHandlingForCreate7() throws Exception{
		ProjectRoleService service = ProjectRoleService.Factory.getInstance();
		UserRole userRole = new UserRole();
		userRole.setPrincipalName("bob");
		userRole.setRole("meh");
		service.addUserRoleToProject(1, userRole);
	}
	
	@Test (expected = Exception.class)
	public void testExceptionHandlingForRemove1() throws Exception{
		ProjectRoleService service = ProjectRoleService.Factory.getInstance();
		service.removeUserFromRoleInProject(999, 9999);
	}
	
	@Test (expected = Exception.class)
	public void testExceptionHandlingForRemove2() throws Exception{
		ProjectRoleService service = ProjectRoleService.Factory.getInstance();
		service.removeUserFromRoleInProject(1,1);
	}
	
	//
	// Force hibernate exception
	//
	@Test (expected = Exception.class)
	public void testExceptionHandlingForRemove3() throws Exception{
		try {
			ProjectRoleService service = ProjectRoleService.Factory.getInstance();
			HibernateUtils.getSessionFactory().getCurrentSession().beginTransaction();
			UserRole userRole = new UserRole();
			userRole.setPrincipalName("bob");
			userRole.setRole("viewer");
			service.addUserRoleToProject(1, userRole);		
			service.removeUserFromRoleInProject(1,userRole.getId());
		} catch (Exception e) {
			ProjectRoleService service = ProjectRoleService.Factory.getInstance();
			UserRole userRole = new UserRole();
			userRole.setPrincipalName("bob");
			userRole.setRole("viewer");
			service.addUserRoleToProject(1, userRole);		
			service.removeUserFromRoleInProject(1,userRole.getId());
		}
	}

	@Test (expected = Exception.class)
	public void testExceptionHandlingForRemove4() throws Exception{
		ProjectRoleService service = ProjectRoleService.Factory.getInstance();
		
		UserRole userRole = new UserRole();
		userRole.setPrincipalName("bob");
		userRole.setRole("viewer");
		service.addUserRoleToProject(1, userRole);	
		
		UserRole userRole2 = new UserRole();
		userRole2.setPrincipalName("bob");
		userRole2.setRole("viewer");
		service.addUserRoleToProject(2, userRole2);	
		
		service.removeUserFromRoleInProject(2,userRole.getId());
	}
}
