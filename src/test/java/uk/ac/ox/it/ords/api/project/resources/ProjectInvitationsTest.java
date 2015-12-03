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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.hibernate.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.it.ords.api.project.model.Invitation;
import uk.ac.ox.it.ords.api.project.services.ProjectService;
import uk.ac.ox.it.ords.api.project.services.impl.hibernate.HibernateUtils;
import uk.ac.ox.it.ords.security.model.UserRole;

public class ProjectInvitationsTest extends AbstractResourceTest {
	
	private int projectId;
	
	/**
	 * Set up a private project
	 */
	@Before
	public void setup(){
			loginUsingSSO("pingu", "pingu");
			WebClient client = getClient();
			client.path("/");
			uk.ac.ox.it.ords.api.project.model.Project project = new uk.ac.ox.it.ords.api.project.model.Project();
			project.setName("Test Project B");
			project.setDescription("ProjectInvitationsTest");
			project.setPrivateProject(true);
			Response response = client.post(project);
			assertEquals(201, response.getStatus());
			response = getClient().path(response.getLocation().getPath()).get();			
			assertEquals(200, response.getStatus());
			project = response.readEntity(uk.ac.ox.it.ords.api.project.model.Project.class);
			assertEquals("Test Project B", project.getName());
			this.projectId = project.getProjectId();
			logout();
	}

	@Test
	public void createInvitation(){
		loginUsingSSO("pingu", "pingu");
		Invitation invitation = new Invitation();
		invitation.setProjectId(projectId);
		invitation.setSender("Pingu");
		invitation.setRoleRequired("viewer");
		invitation.setEmail("pinga@mailinator.com");
		Response response = getClient().path("/"+projectId+"/invitation").post(invitation);
		assertEquals(201, response.getStatus());
		assertEquals(1, getClient().path("/"+projectId+"/invitation").get().readEntity(new GenericType<List<Invitation>>() {}).size());
		
		invitation = getClient().path(response.getLocation().getPath()).get().readEntity(Invitation.class);
		
		assertEquals(projectId, invitation.getProjectId());
		assertEquals("viewer", invitation.getRoleRequired());
		assertNotNull(invitation.getUuid());
		logout();
		
		//
		// Now confirm invitation
		//
		loginUsingSSO("pinga", "pinga");
		assertEquals(200, getClient().path("/invitation/"+invitation.getUuid()).post(null).getStatus());
		
		//
		// Now check she has a role
		//
		assertEquals(200, getClient().path("/"+projectId).get().getStatus());	
		
	}
	
	@Test
	public void confirmUsingWrongCode(){
		loginUsingSSO("pinga", "pinga");
		assertEquals(400, getClient().path("/invitation/zzzzz-111111-99999").post(null).getStatus());
	}
	
	@Test
	public void permissions(){
		loginUsingSSO("pingu", "pingu");
		Invitation invitation = new Invitation();
		invitation.setProjectId(projectId);
		invitation.setRoleRequired("viewer");
		invitation.setSender("Pingu");
		invitation.setEmail("pinga@mailinator.com");
		String path = getClient().path("/"+projectId+"/invitation").post(invitation).getLocation().getPath();
		logout();
		
		assertEquals(403, getClient().path(path).get().getStatus());
		assertEquals(403, getClient().path(path).delete().getStatus());
		assertEquals(403, getClient().path("/"+projectId+"/invitation").get().getStatus());
		
	}
	
	@Test
	public void createInvitationNoProject(){
		loginUsingSSO("pingu", "pingu");
		Invitation invitation = new Invitation();
		invitation.setProjectId(-1);
		invitation.setRoleRequired("viewer");
		assertEquals(400, getClient().path("/"+projectId+"/invitation").post(invitation).getStatus());
		assertEquals(404, getClient().path("/9999/invitation").post(invitation).getStatus());

		logout();
	}

	@Test
	public void createInvitationDeletedProject(){

		loginUsingSSO("pingu", "pingu");
		
		// Create project
		WebClient client = getClient();
		client.path("/");
		uk.ac.ox.it.ords.api.project.model.Project project = new uk.ac.ox.it.ords.api.project.model.Project();
		project.setName("Test Project Z");
		project.setDescription("createInvitationDeletedProject");
		project.setPrivateProject(true);
		Response response = client.post(project);
		assertEquals(201, response.getStatus());
		project = getClient().path(response.getLocation().getPath()).get().readEntity(uk.ac.ox.it.ords.api.project.model.Project.class);
		assertEquals("Test Project Z", project.getName());
		projectId = project.getProjectId();

		// Delete project
		assertEquals(200, getClient().path(response.getLocation().getPath()).delete().getStatus());
		
		// Create invitation
		Invitation invitation = new Invitation();
		invitation.setProjectId(projectId);
		invitation.setRoleRequired("viewer");
		assertEquals(410, getClient().path("/"+projectId+"/invitation").post(invitation).getStatus());
		logout();
	}
	
	@Test
	public void confirmInvitationDeletedProject(){

		loginUsingSSO("pingu", "pingu");
		
		// Create project
		WebClient client = getClient();
		client.path("/");
		uk.ac.ox.it.ords.api.project.model.Project project = new uk.ac.ox.it.ords.api.project.model.Project();
		project.setName("Test Project Z");
		project.setDescription("createInvitationDeletedProject");
		project.setPrivateProject(true);
		Response response = client.post(project);
		assertEquals(201, response.getStatus());
		project = getClient().path(response.getLocation().getPath()).get().readEntity(uk.ac.ox.it.ords.api.project.model.Project.class);
		assertEquals("Test Project Z", project.getName());
		projectId = project.getProjectId();

		
		// Create invitation
		Invitation invitation = new Invitation();
		invitation.setProjectId(projectId);
		invitation.setRoleRequired("viewer");
		invitation.setEmail("pinga@ox.ac.uk");
		invitation.setSender("pingu@ox.ac.uk");
		response = getClient().path("/"+projectId+"/invitation").post(invitation);
		invitation = getClient().path(response.getLocation().getPath()).get().readEntity(Invitation.class);
		
		// Delete project
		assertEquals(200, getClient().path("/"+projectId).delete().getStatus());
		logout();
		
		// Confirm invitation
		loginUsingSSO("pinga", "pinga");
		assertEquals(410, getClient().path("/invitation/"+invitation.getUuid()).post(null).getStatus());
		logout();
	}
	
	@Test
	public void confirmInvitationWithPurgedProject(){
		loginUsingSSO("pingu", "pingu");
		Invitation invitation = new Invitation();
		invitation.setProjectId(projectId);
		invitation.setSender("Pingu");
		invitation.setRoleRequired("viewer");
		invitation.setEmail("pinga@mailinator.com");
		Response response = getClient().path("/"+projectId+"/invitation").post(invitation);
		assertEquals(201, response.getStatus());
		assertEquals(1, getClient().path("/"+projectId+"/invitation").get().readEntity(new GenericType<List<Invitation>>() {}).size());
		
		invitation = getClient().path(response.getLocation().getPath()).get().readEntity(Invitation.class);
		
		assertEquals(projectId, invitation.getProjectId());
		assertEquals("viewer", invitation.getRoleRequired());
		assertNotNull(invitation.getUuid());
		logout();
		
		//
		// Purge the project
		//
		uk.ac.ox.it.ords.api.project.model.Project project = ProjectService.Factory.getInstance().getProject(projectId);
		Session session = HibernateUtils.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		session.delete(project);
		session.getTransaction().commit();
		
		//
		// Now confirm invitation
		//
		loginUsingSSO("pinga", "pinga");
		assertEquals(404, getClient().path("/invitation/"+invitation.getUuid()).post(null).getStatus());
	}
	
	@Test
	public void createInvitationWrongObject(){
		loginUsingSSO("pingu", "pingu");
		UserRole userRole = new UserRole();
		userRole.setPrincipalName("fred");
		userRole.setRole("chief banana");
		assertEquals(400, getClient().path("/"+projectId+"/invitation").post(userRole).getStatus());
		logout();
	}
	
	@Test
	public void createInvitationNoRole(){
		loginUsingSSO("pingu", "pingu");
		Invitation invitation = new Invitation();
		invitation.setProjectId(-1);
		invitation.setRoleRequired(null);
		assertEquals(400, getClient().path("/"+projectId+"/invitation").post(invitation).getStatus());
		logout();
	}
	
	@Test
	public void createInvalidInvitation(){
		loginUsingSSO("pingu", "pingu");
		Invitation invitation = new Invitation();
		invitation.setProjectId(projectId);
		invitation.setRoleRequired(null);
		assertEquals(400, getClient().path("/"+projectId+"/invitation").post(invitation).getStatus());
		logout();
	}
	
	@Test
	public void deleteInvitation(){
		loginUsingSSO("pingu", "pingu");
		Invitation invitation = new Invitation();
		invitation.setProjectId(projectId);
		invitation.setEmail("pinga@mailinator.com");
		invitation.setSender("Pingu");
		invitation.setRoleRequired("viewer");
		String path = getClient().path("/"+projectId+"/invitation").post(invitation).getLocation().getPath();
		
		assertEquals(200, getClient().path(path).delete().getStatus());
		assertEquals(404, getClient().path(path).get().getStatus());
		logout();
	}
	
	@Test
	public void noProject(){
		assertEquals(404, getClient().path("/9999/invitation/").get().getStatus());
		assertEquals(404, getClient().path("/9999/invitation/26").delete().getStatus());
		assertEquals(404, getClient().path("/9999/invitation/26").get().getStatus());

	}
	
	@Test
	public void noInvitation(){
		assertEquals(404, getClient().path("/"+projectId+"/invitation/9999").delete().getStatus());
		assertEquals(404, getClient().path("/"+projectId+"/invitation/9999").get().getStatus());
	}
	
	@Test
	public void createInvitationUnauth(){
		Invitation invitation = new Invitation();
		invitation.setProjectId(projectId);
		invitation.setRoleRequired("viewer");
		
		assertEquals(403, getClient().path("/"+projectId+"/invitation").post(invitation).getStatus());
	}
	
	@Test
	public void createInvitationViewer(){
		loginUsingSSO("pingu", "pingu");
		UserRole role = new UserRole();
		role.setPrincipalName("pinga");
		role.setRole("viewer");
		assertEquals(201, getClient().path("/"+projectId+"/role").post(role).getStatus());
		logout();
		
		loginUsingSSO("pinga", "pinga");
		Invitation invitation = new Invitation();
		invitation.setProjectId(projectId);
		invitation.setRoleRequired("viewer");
		assertEquals(403, getClient().path("/"+projectId+"/invitation").post(invitation).getStatus());
		logout();
	}
	
	@Test
	public void deletedProjects(){
		loginUsingSSO("pingu", "pingu");		
		assertEquals(200, getClient().path("/"+projectId).delete().getStatus());
		

		Invitation invitation = new Invitation();
		invitation.setProjectId(projectId);
		invitation.setRoleRequired("viewer");
		assertEquals(410, getClient().path("/"+projectId+"/invitation").post(invitation).getStatus());
		assertEquals(410, getClient().path("/"+projectId+"/invitation").get().getStatus());
		assertEquals(410, getClient().path("/"+projectId+"/invitation/26").get().getStatus());
		assertEquals(410, getClient().path("/"+projectId+"/invitation/26").delete().getStatus());
		
		logout();
	}
	
	@Test
	public void sideAttacks(){
		loginUsingSSO("pingu","pingu");
		WebClient client = getClient();
		
		// Project 1
		client = getClient();
		client.path("/");
		uk.ac.ox.it.ords.api.project.model.Project project = new uk.ac.ox.it.ords.api.project.model.Project();
		project.setName("Test Project S1");
		project.setDescription("sideAttacks");
		Response response = client.post(project);
		assertEquals(201, response.getStatus());
		String path = response.getLocation().getPath();
		response = getClient().path(path).get();
		project = response.readEntity(uk.ac.ox.it.ords.api.project.model.Project.class);
		assertEquals("Test Project S1", project.getName());
		int project1Id = project.getProjectId();
			
		// Project 1 Invitation 1
		Invitation invitation1 = new Invitation();
		invitation1.setProjectId(project1Id);
		invitation1.setRoleRequired("owner");
		invitation1.setSender("Pingu");
		invitation1.setEmail("pinga@mailinator.com");
		response = getClient().path("/"+project1Id+"/invitation").post(invitation1);
		assertEquals(201, response.getStatus());
		URI invitation1URI = response.getLocation();
		int invitationId1 = getClient().path(invitation1URI.getPath()).get().readEntity(Invitation.class).getId();
		
		// Project 2
		client = getClient();
		client.path("/");
		uk.ac.ox.it.ords.api.project.model.Project project2 = new uk.ac.ox.it.ords.api.project.model.Project();
		project2.setName("Test Project S2");
		project2.setDescription("sideAttacks");
		response = client.post(project);
		assertEquals(201, response.getStatus());
		path = response.getLocation().getPath();
		response = getClient().path(path).get();
		project = response.readEntity(uk.ac.ox.it.ords.api.project.model.Project.class);
		assertEquals("Test Project S1", project.getName());
		int project2Id = project.getProjectId();
		
		//
		// Add using wrong project id
		//	
		Invitation invitation2 = new Invitation();
		invitation2.setProjectId(project1Id);
		invitation2.setRoleRequired("owner");
		invitation2.setSender("Pingu");
		invitation2.setEmail("pinga@mailinator.com");
		assertEquals(400, getClient().path("/"+project2Id+"/invitation").post(invitation2).getStatus());
		
		//
		// Now add to the correct project2 invitation2
		//
		invitation2.setProjectId(project2Id);
		response = getClient().path("/"+project2Id+"/invitation").post(invitation2);
		assertEquals(201, response.getStatus());
		URI projectDatabaseURI2 = response.getLocation();
		int invitationId2 = getClient().path(projectDatabaseURI2.getPath()).get().readEntity(Invitation.class).getId();
		
		//
		// OK, now lets do some gets and deletes using the wrong projects
		//
		assertEquals(400, getClient().path("/" + project1Id + "/invitation/" + invitationId2).get().getStatus());
		assertEquals(400, getClient().path("/" + project2Id + "/invitation/" + invitationId1).get().getStatus());
		assertEquals(400, getClient().path("/" + project1Id + "/invitation/" + invitationId2).delete().getStatus());
		assertEquals(400, getClient().path("/" + project2Id + "/invitation/" + invitationId1).delete().getStatus());
		
		//
		// Now delete properly
		//
		assertEquals(200, getClient().path("/" + project1Id + "/invitation/" + invitationId1).delete().getStatus());
		assertEquals(200, getClient().path("/" + project2Id + "/invitation/" + invitationId2).delete().getStatus());
	}
	
	@After
	public void tearDown(){
		loginUsingSSO("pingu", "pingu");
		getClient().path("/"+projectId).delete().getStatus();
	}
}
