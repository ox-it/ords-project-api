package uk.ac.ox.it.ords.api.project.resources;

import static org.junit.Assert.assertEquals;

import java.net.URI;

import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.it.ords.api.project.model.UserRole;

public class ProjectRoleTest extends AbstractResourceTest {
	
	private int projectId;

	/**
	 * Set up a private project
	 */
	@Before
	public void setup(){
		loginUsingSSO("pingu", "pingu");
		WebClient client = getClient();
		client.path("project/");
		uk.ac.ox.it.ords.api.project.model.Project project = new uk.ac.ox.it.ords.api.project.model.Project();
		project.setName("Test Project B");
		project.setDescription("Test Project B");
		project.setPrivateProject(true);
		Response response = client.post(project);
		assertEquals(200, response.getStatus());
		project = response.readEntity(uk.ac.ox.it.ords.api.project.model.Project.class);
		assertEquals("Test Project B", project.getName());
		this.projectId = project.getProjectId();
		logout();
		
	}
	
	@Test
	public void AddRoleUnauthenticated(){
		logout();
		WebClient client = getClient();
		client.path("project/"+projectId+"/role");
		UserRole role = new UserRole();
		role.setPrincipalName("pinga");
		role.setRole("owner");;
		Response response = client.post(role);
		assertEquals(403, response.getStatus());	
	}
	
	@Test
	public void AddRoleUnauthorized(){
		logout();
		loginUsingSSO("pinga", "test");
		WebClient client = getClient();
		client.path("project/"+projectId+"/role");
		UserRole role = new UserRole();
		role.setPrincipalName("pinga");
		role.setRole("owner");;
		Response response = client.post(role);
		assertEquals(403, response.getStatus());	
	}
	
	@Test
	public void removeRole(){
		
		//
		// This is a private project so Pinga can't see it
		//
		loginUsingSSO("pinga", "test");
		WebClient client = getClient();
		client.path("project/"+projectId);
		Response response = client.get();
		assertEquals(403, response.getStatus());
		
		//
		// Add Pinga as a viewer
		//
		logout();
		loginUsingSSO("pingu", "test");
		client = getClient();
		client.path("project/"+projectId+"/role");
		UserRole role = new UserRole();
		role.setPrincipalName("pinga");
		role.setRole("viewer");;
		response = client.post(role);
		
		//
		// This is the role URI
		//
		URI roleURI = response.getLocation();
		assertEquals(201, response.getStatus());
		
		//
		// Can she view OK?
		//
		logout();
		loginUsingSSO("pinga", "test");
		client = getClient();
		client.path("project/"+projectId);
		response = client.get();
		assertEquals(200, response.getStatus());
		logout();
		
		//
		// Remove Pinga as a viewer
		//
		logout();
		loginUsingSSO("pingu", "test");
		client = getClient();
		client.path(roleURI.getPath());
		response = client.delete();
		assertEquals(200, response.getStatus());
		logout();
	}

	@Test
	public void removeRoleNonexisting(){
		loginUsingSSO("pingu", "test");
		WebClient client = getClient();
		client.path("/project/"+projectId+"/role/999999");
		Response response = client.delete();
		assertEquals(404, response.getStatus());
		logout();
	}
	
	@Test
	public void removeRoleNonexistingProject(){
		loginUsingSSO("pingu", "test");
		WebClient client = getClient();
		client.path("/project/999999/role/999999");
		Response response = client.delete();
		assertEquals(404, response.getStatus());
		logout();
	}
	
	@Test
	public void removeRoleUnauthorized(){
		
		//
		// This is a private project so Pinga can't see it
		//
		loginUsingSSO("pinga", "test");
		WebClient client = getClient();
		client.path("project/"+projectId);
		Response response = client.get();
		assertEquals(403, response.getStatus());
		
		//
		// Add Pinga as a viewer
		//
		logout();
		loginUsingSSO("pingu", "test");
		client = getClient();
		client.path("project/"+projectId+"/role");
		UserRole role = new UserRole();
		role.setPrincipalName("pinga");
		role.setRole("viewer");;
		response = client.post(role);
		
		//
		// This is the role URI
		//
		URI roleURI = response.getLocation();
		assertEquals(201, response.getStatus());
		
		//
		// Can she view OK?
		//
		logout();
		loginUsingSSO("pinga", "test");
		client = getClient();
		client.path("project/"+projectId);
		response = client.get();
		assertEquals(200, response.getStatus());
		logout();
		
		//
		// Remove Pinga as a viewer  - this won't work as she isn't
		// the project owner
		//
		logout();
		loginUsingSSO("pinga", "test");
		client = getClient();
		client.path(roleURI.getPath());
		response = client.delete();
		assertEquals(403, response.getStatus());
		
		//
		// Remove Pinga as a viewer  - this won't work
		// as there is no-one logged in
		//
		logout();
		client = getClient();
		client.path(roleURI.getPath());
		response = client.delete();
		assertEquals(403, response.getStatus());
	}
	
	@Test
	public void addRole(){
		
		//
		// This is a private project so Pinga can't see it
		//
		loginUsingSSO("pinga", "test");
		WebClient client = getClient();
		client.path("project/"+projectId);
		Response response = client.get();
		assertEquals(403, response.getStatus());
		
		//
		// Add Pinga as a viewer
		//
		logout();
		loginUsingSSO("pingu", "test");
		client = getClient();
		client.path("project/"+projectId+"/role");
		UserRole role = new UserRole();
		role.setPrincipalName("pinga");
		role.setRole("viewer");;
		response = client.post(role);
		assertEquals(201, response.getStatus());
		
		//
		// Can she view OK?
		//
		logout();
		loginUsingSSO("pinga", "test");
		client = getClient();
		client.path("project/"+projectId);
		response = client.get();
		assertEquals(200, response.getStatus());
		logout();
		
	}
	
	@Test
	public void removeUnrelatedRole(){
		
		//
		// Create Project 2 - Pingu is owner
		//
		loginUsingSSO("pingu", "pingu");
		WebClient client = getClient();
		client.path("project/");
		uk.ac.ox.it.ords.api.project.model.Project project = new uk.ac.ox.it.ords.api.project.model.Project();
		project.setName("Test Project C");
		project.setDescription("Test Project C");
		project.setPrivateProject(true);
		Response response = client.post(project);
		assertEquals(200, response.getStatus());
		project = response.readEntity(uk.ac.ox.it.ords.api.project.model.Project.class);
		assertEquals("Test Project C", project.getName());
		int project2 = project.getProjectId();
		
		//
		// Pingu adds Pinga to Project 1
		//
		client = getClient();
		client.path("project/"+projectId+"/role");
		UserRole role = new UserRole();
		role.setPrincipalName("pinga");
		role.setRole("viewer");;
		response = client.post(role);
		assertEquals(201, response.getStatus());
		
		URI role1URI = response.getLocation();
		assertEquals(201, response.getStatus());
		
		//
		// Pingu adds Pinga to Project 2
		//
		client = getClient();
		client.path("project/"+project2+"/role");
		role = new UserRole();
		role.setPrincipalName("pinga");
		role.setRole("viewer");;
		response = client.post(role);
		assertEquals(201, response.getStatus());
		
		URI role2URI = response.getLocation();
		assertEquals(201, response.getStatus());
		
		//
		// Pingu tries to remove pinga from "viewer_1" role in project 2 - this should fail
		// as that role belongs to project 1.
		//
		String path1 = role1URI.getPath();
		String path2 = role2URI.getPath();
		String pathBad = path1.replace(path1.split("/")[4], path2.split("/")[4]);
		
		client = getClient();
		client.path(pathBad);
		response = client.delete();
		assertEquals(400, response.getStatus());
		
		//
		// However these deletes should work fine
		//
		client = getClient();
		client.path(path1);
		response = client.delete();
		assertEquals(200, response.getStatus());
		
		client = getClient();
		client.path(path2);
		response = client.delete();
		assertEquals(200, response.getStatus());
		
		//
		// Finally we'll delete the project
		//
		client = getClient();
		client.path("/project/"+project2);
		response = client.delete();
		assertEquals(200, response.getStatus());
		
		
		logout();
	}
	
	
}
