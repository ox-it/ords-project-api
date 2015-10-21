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
package uk.ac.ox.it.ords.api.project.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;

import uk.ac.ox.it.ords.api.project.model.UserRole;
import uk.ac.ox.it.ords.api.project.services.impl.hibernate.HibernateUtils;

public class ProjectRoleServiceTest {


	@Test
	public void testExceptionHandlingForGet() throws Exception{
		ProjectRoleService service = ProjectRoleService.Factory.getInstance();
		UserRole role = service.getUserRole(-1);
		assertNull(role);
	}
	
	@Test (expected = Exception.class)
	public void testExceptionHandlingForGet2() throws Exception{
			try {
				ProjectRoleService service = ProjectRoleService.Factory.getInstance();
				HibernateUtils.getSessionFactory().getCurrentSession().beginTransaction();
				service.getUserRole(999999);
				service.getUserRole(-1);
				fail();
			} catch (Exception e) {
				HibernateUtils.closeSession();
				throw e;
			}
	}
	
	@Test
	public void testExceptionHandlingForGetList() throws Exception{
			ProjectRoleService service = ProjectRoleService.Factory.getInstance();
			assertEquals(0, service.getUserRolesForProject(9999).size());
	}
	
	@Test (expected = Exception.class)
	public void testExceptionHandlingForGetList2() throws Exception{
			try {
				ProjectRoleService service = ProjectRoleService.Factory.getInstance();
				HibernateUtils.getSessionFactory().getCurrentSession().beginTransaction();
				service.getUserRolesForProject(9999);
				fail();
			} catch (Exception e) {
				HibernateUtils.closeSession();
				throw e;
			}
	}
	
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
			HibernateUtils.closeSession();
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
		service.removeUserFromRoleInProject(1,9999);
	}
	
	//
	// Force hibernate exception
	//
	@Test
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
			HibernateUtils.closeSession();
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
