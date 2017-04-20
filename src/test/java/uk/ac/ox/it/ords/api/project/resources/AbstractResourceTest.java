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
package uk.ac.ox.it.ords.api.project.resources;

import java.util.ArrayList;
import java.util.List;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.lifecycle.ResourceProvider;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.apache.cxf.transport.local.LocalConduit;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.util.Factory;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import uk.ac.ox.it.ords.api.project.model.User;
import uk.ac.ox.it.ords.api.project.permissions.ProjectPermissionSets;
import uk.ac.ox.it.ords.api.project.server.UnrecognizedPropertyExceptionMapper;
import uk.ac.ox.it.ords.api.project.server.ValidationExceptionMapper;
import uk.ac.ox.it.ords.api.project.services.ProjectRoleService;
import uk.ac.ox.it.ords.api.project.services.impl.hibernate.HibernateUtils;
import uk.ac.ox.it.ords.security.AbstractShiroTest;
import uk.ac.ox.it.ords.security.model.Permission;
import uk.ac.ox.it.ords.security.model.UserRole;

public class AbstractResourceTest extends AbstractShiroTest {

	protected final static String ENDPOINT_ADDRESS = "local://project-api";
	protected static Server server;
	protected static void startServer() throws Exception {

	}
	
	public WebClient getClient(){
		List<Object> providers = new ArrayList<Object>();
		providers.add(new JacksonJsonProvider());
		WebClient client = WebClient.create(ENDPOINT_ADDRESS, providers);
		client.type("application/json");
		client.accept("application/json");
		WebClient.getConfig(client).getRequestContext().put(LocalConduit.DIRECT_DISPATCH, Boolean.TRUE);
		return client;
	}
	
	public static void createTestUsersAndRoles() throws Exception{
		
		///
		/// I have no idea why this is needed to "kickstart" Hibernate
		/// into behaving properly for these tests. I *think* it binds
		/// the hibernate session to the same thread that is used by
		/// the resource classes, but I could be wrong. It doesn't seem
		/// to bother any of the other modules.
		/// 
		ProjectRoleService service = ProjectRoleService.Factory.getInstance();
		UserRole userRole = new UserRole();
		userRole.setPrincipalName("bob");
		userRole.setRole("viewer");
		service.addUserRoleToProject(1, userRole);	
		
		//
		// Set up the database
		//
		//
		// Set up the test users and their permissions
		//
		Session session = HibernateUtils.getSessionFactory().getCurrentSession();
		Transaction transaction = session.beginTransaction();
		
		//
		// Clear out anything there already
		//
		session.createSQLQuery("truncate userrole, permissions, project").executeUpdate();
		transaction.commit();
		
		//
		// Add our test permissions
		//
		session = HibernateUtils.getSessionFactory().getCurrentSession();
		transaction = session.beginTransaction();
		
		//
		// Anyone with the "User" role can contribute to projects
		//
		for (String permission : ProjectPermissionSets.getPermissionsForUser()){
			Permission permissionObject = new Permission();
			permissionObject.setRole("user");
			permissionObject.setPermission(permission);
			session.save(permissionObject);
		}
		
		//
		// Anyone with the "User" role can create new trial projects
		//
		for (String permission : ProjectPermissionSets.getPermissionsForLocalUser()){
			Permission permissionObject = new Permission();
			permissionObject.setRole("localuser");
			permissionObject.setPermission(permission);
			session.save(permissionObject);
		}
		
		//
		// Anyone with the "Administrator" role can create new full
		// projects and upgrade projects to full, and update any
		// user projects
		//
		for (String permission : ProjectPermissionSets.getPermissionsForSysadmin()){
			Permission permissionObject = new Permission();
			permissionObject.setRole("administrator");
			permissionObject.setPermission(permission);
			session.save(permissionObject);
		}

		//
		// "Anonymous" can View public projects
		//
		for (String permission : ProjectPermissionSets.getPermissionsForAnonymous()){
			Permission permissionObject = new Permission();
			permissionObject.setRole("anonymous");
			permissionObject.setPermission(permission);
			session.save(permissionObject);
		}
	
		//
		// Add test users to roles
		//
		UserRole admin = new UserRole();
		admin.setPrincipalName("admin");
		admin.setRole("administrator");
		session.save(admin);
		
		UserRole pingu = new UserRole();
		pingu.setPrincipalName("pingu");
		pingu.setRole("localuser");
		session.save(pingu);
		
		UserRole pingu2 = new UserRole();
		pingu2.setPrincipalName("pingu");
		pingu2.setRole("premiumuser");
		session.save(pingu2);
		
		UserRole pingo = new UserRole();
		pingo.setPrincipalName("pingo");
		pingo.setRole("user");
		session.save(pingo);
		
		UserRole anonymous = new UserRole();
		anonymous.setPrincipalName("anonymous");
		anonymous.setRole("anonymous");
		session.save(anonymous);
		
		User phil = new User();
		phil.setEmail("phil@mailinator.com");
		phil.setPrincipalName("philster");
		phil.setUserId(1);
		List<User> users = session.createCriteria(User.class).add(Restrictions.eq("principalName", "philster")).list();
		if (users.isEmpty()){
			session.save(phil);
		}
		
		UserRole philRole = new UserRole();
		philRole.setPrincipalName("philster");
		philRole.setRole("user");
		session.save(philRole);
		
		//
		// Commit our changes
		//
		transaction.commit();
		HibernateUtils.closeSession();
	}

	/**
	 * Configure Shiro and start the server
	 * @throws Exception
	 */
	@BeforeClass
	public static void initialize() throws Exception {
	
		//
		// Set up roles
		//
		createTestUsersAndRoles();
		
		//
		// This is for unit testing only and uses the test.shiro.ini configuration
		//
		Factory<SecurityManager> factory = new IniSecurityManagerFactory("classpath:test.shiro.ini");
		SecurityManager securityManager = factory.getInstance();
		SecurityUtils.setSecurityManager(securityManager);
		
		//
		// Create an embedded server with JSON processing
		//
		JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
		
		ArrayList<Object> providers = new ArrayList<Object>();
		providers.add(new JacksonJsonProvider());
		providers.add(new UnrecognizedPropertyExceptionMapper());
		providers.add(new ValidationExceptionMapper());
		sf.setProviders(providers);
		
		//
		// Add our REST resources to the server
		//
		ArrayList<ResourceProvider> resources = new ArrayList<ResourceProvider>();
		resources.add(new SingletonResourceProvider(new Project(), true));
		resources.add(new SingletonResourceProvider(new ProjectRole(), true));
		resources.add(new SingletonResourceProvider(new ProjectDatabase(), true));
		resources.add(new SingletonResourceProvider(new ProjectInvitation(), true));
		sf.setResourceProviders(resources);
		
		//
		// Start the server at the endpoint
		//
		sf.setAddress(ENDPOINT_ADDRESS);
		server = sf.create(); 
		startServer();
	}

	@AfterClass
	public static void destroy() throws Exception {
		server.stop();
		server.destroy();
	}

	@After
	public void logout(){
		SecurityUtils.getSubject().logout();
	}



}
