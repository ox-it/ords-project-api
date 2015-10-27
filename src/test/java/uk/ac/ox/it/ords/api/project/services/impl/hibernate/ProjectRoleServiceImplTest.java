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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.ox.it.ords.api.project.model.Project;
import uk.ac.ox.it.ords.api.project.permissions.ProjectPermissions;
import uk.ac.ox.it.ords.api.project.server.ValidationException;
import uk.ac.ox.it.ords.api.project.services.ProjectService;
import uk.ac.ox.it.ords.security.AbstractShiroTest;
import uk.ac.ox.it.ords.security.model.UserRole;

/**
 * Implementation tests for protected methods
 */
public class ProjectRoleServiceImplTest extends ProjectRoleServiceImpl{
	
	private static Project project;
	private static UserRole userRole;
	private static Auth auth;
	
	/**
	 * Embededded Shiro class - we have to do this because we can't mixin
	 * AbstractShiroTest and ProjectRoleServiceImpl
	 */
	static class Auth extends AbstractShiroTest{
		public Auth(){
			org.apache.shiro.util.Factory<SecurityManager> factory = new IniSecurityManagerFactory("classpath:test.shiro.ini");
			SecurityManager securityManager = factory.getInstance();
			SecurityUtils.setSecurityManager(securityManager);
		}
		public void login(String user){
			this.loginUsingSSO(user, "");
		}
	}
	
	@BeforeClass
	public static void setup() throws ValidationException, Exception{
		auth = new Auth();
		auth.login("pingu");
		
		project = new Project();
		project.setName("ProjectRoleServiceImplTest");
		project.setDescription("ProjectRoleServiceImplTest");
		ProjectService.Factory.getInstance().createProject(project);
		
		
		userRole = new UserRole();
		userRole.setPrincipalName("pongo");
		userRole.setRole(ProjectPermissions.PROJECT_ANY_ACTION(project.getProjectId()));
	}
	
	@AfterClass
	public static void tearDown() throws Exception{
		HibernateUtils.closeSession();
		ProjectService.Factory.getInstance().deleteProject(project.getProjectId());

	}
	
	
	/**
	 * Create permission forcing hibernate error
	 * @throws Exception 
	 */
	@Test (expected = Exception.class)
	public void testCreatePermission() throws Exception{
		try {
			this.sessionFactory.getCurrentSession().beginTransaction();
			this.createInitialPermissions(project.getProjectId());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * Delete permission forcing hibernate error
	 * @throws Exception 
	 */
	@Test (expected = Exception.class)
	public void testDeletePermissions() throws Exception{
		try {
			this.sessionFactory.getCurrentSession().beginTransaction();
			this.deletePermissions(project.getProjectId());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
	}
	
	
	/**
	 * Create permission forcing hibernate error
	 * @throws Exception 
	 */
	@Test (expected = Exception.class)
	public void testCreatePermission2() throws Exception{
		try {
			this.sessionFactory.getCurrentSession().beginTransaction();
			this.createPermission("pongo", ProjectPermissions.PROJECT_ANY_ACTION(project.getProjectId()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * Delete non-existing userRole
	 * @throws Exception
	 */
	@Test(expected = Exception.class)
	public void testDeleteRole() throws Exception{
		
		UserRole userRole = new UserRole();
		int projectId = 1;
		this.sessionFactory.getCurrentSession().beginTransaction();
		this.removeUserRole(userRole, projectId);
	}
	
	/**
	 * Delete user role within existing transaction - force hibernate error
	 * @throws Exception
	 */
	@Test(expected = Exception.class)
	public void testDeleteRole2() throws Exception{
		
		this.sessionFactory.getCurrentSession().beginTransaction();
		this.removeUserRole(userRole, project.getProjectId());
	}

}
