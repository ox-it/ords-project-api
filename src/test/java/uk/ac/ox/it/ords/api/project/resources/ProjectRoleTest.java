package uk.ac.ox.it.ords.api.project.resources;

import static org.junit.Assert.*;

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.GenericType;
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
			assertEquals(201, response.getStatus());
			response = getClient().path(response.getLocation().getPath()).get();
			project = response.readEntity(uk.ac.ox.it.ords.api.project.model.Project.class);
			assertEquals("Test Project B", project.getName());
			this.projectId = project.getProjectId();
			logout();
	}
	
	//
	// Get non-existing role
	//
	@Test
	public void nonExistingRole(){
		loginUsingSSO("pingu", "pingu");
		assertEquals(404, getClient().path("project/9999/role").get().getStatus());
		assertEquals(404, getClient().path("project/9999/role/9999").get().getStatus());
		assertEquals(404, getClient().path("project/"+projectId+"/role/9999").get().getStatus());
		logout();
	}
	
	//
	// Get private role
	//
	@Test
	public void privateRole(){
		assertEquals(403, getClient().path("project/"+projectId+"/role/").get().getStatus());
		
		loginUsingSSO("pingu", "pingu");
		UserRole role = new UserRole();
		role.setPrincipalName("pinga");
		role.setRole("viewer");
		String rolePath = getClient().path("project/"+projectId+"/role/").post(role).getLocation().getPath();
		logout();
		
		assertEquals(403, getClient().path(rolePath).get().getStatus());
	}
	
	//
	// Get public role
	//
	@Test
	public void publicRole(){
		
		loginUsingSSO("pingu", "pingu");
		uk.ac.ox.it.ords.api.project.model.Project project = new uk.ac.ox.it.ords.api.project.model.Project();
		project.setName("Test Project C");
		project.setDescription("Test Project C");
		project.setPrivateProject(false);
		String projectPath = getClient().path("/project").post(project).getLocation().getPath();
		
		UserRole role = new UserRole();
		role.setPrincipalName("pinga");
		role.setRole("viewer");
		String rolePath = getClient().path(projectPath+"/role/").post(role).getLocation().getPath();
		
		logout();
		
		assertEquals(200, getClient().path(projectPath+"/role/").get().getStatus());
		assertEquals(200, getClient().path(rolePath).get().getStatus());
	}
	
	
	//
	// Add Role to non-existant project
	//
	@Test
	public void nonExistingProject(){
		loginUsingSSO("pingu", "pingu");
		UserRole role = new UserRole();
		role.setPrincipalName("pinga");
		role.setRole("viewer");
		assertEquals(404, getClient().path("project/9999/role/").post(role).getStatus());
		logout();	
	}
	
	//
	// Add incomplete Role 
	//
	@Test
	public void incompleteRole(){
		loginUsingSSO("pingu", "pingu");
		UserRole role = new UserRole();
		role.setPrincipalName("pinga");
		assertEquals(400, getClient().path("project/"+projectId+"/role/").post(role).getStatus());
		logout();	
	}
	
	//
	// Add invalid Role 
	//
	@Test
	public void invalidRole(){
		loginUsingSSO("pingu", "pingu");
		UserRole role = new UserRole();
		role.setPrincipalName("pinga");
		role.setRole("chiefbanana");
		assertEquals(400, getClient().path("project/"+projectId+"/role/").post(role).getStatus());
		logout();	
	}
	
	//
	// Project that has been deleted
	//
	@Test
	public void deletedProject(){	
		
		loginUsingSSO("pingu", "pingu");
		
		//
		// Add a role
		//
		UserRole role = new UserRole();
		role.setPrincipalName("pinga");
		role.setRole("viewer");
		String rolePath = getClient().path("project/"+projectId+"/role/").post(role).getLocation().getPath();
		
		//
		// GET the role
		//
		assertEquals(200, getClient().path(rolePath).get().getStatus());
		
		//
		// Delete the project
		//
		assertEquals(200, getClient().path("project/"+projectId).delete().getStatus());
		
		//
		// Act on deleted resources
		//
		assertEquals(410, getClient().path(rolePath).get().getStatus());
		assertEquals(410, getClient().path(rolePath).delete().getStatus());		
		assertEquals(410, getClient().path("project/"+projectId+"/role").get().getStatus());
		assertEquals(410, getClient().path("project/"+projectId+"/role").post(role).getStatus());
		
		logout();
		
	}
	
	//
	// Get role in private project when not authorized 
	//
	
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
		// Get a list of UserRoles - we should have 1
		//
		loginUsingSSO("pingu", "test");
		client = getClient();
		client.path("project/"+projectId+"/role");
		response = client.get();
		assertEquals(200, response.getStatus());
		List<UserRole> roles = response.readEntity(
				new GenericType<List<UserRole>>() {});
		logout();
		assertEquals(1, roles.size());
		
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
		// Get a list of UserRoles - we should have 2
		//
		loginUsingSSO("pingu", "test");
		client = getClient();
		client.path("project/"+projectId+"/role");
		response = client.get();
		assertEquals(200, response.getStatus());
		roles = response.readEntity(
				new GenericType<List<UserRole>>() {});
		logout();
		assertEquals(2, roles.size());
		
		//
		// Can she view OK?
		//
		loginUsingSSO("pinga", "test");
		client = getClient();
		client.path("project/"+projectId);
		response = client.get();
		assertEquals(200, response.getStatus());
		logout();
		
		//
		// Remove Pinga as a viewer
		//
		loginUsingSSO("pingu", "test");
		client = getClient();
		client.path(roleURI.getPath());
		response = client.delete();
		assertEquals(200, response.getStatus());
		logout();
		
		//
		// Get a list of UserRoles - we should have 1
		//
		loginUsingSSO("pingu", "test");
		client = getClient();
		client.path("project/"+projectId+"/role");
		response = client.get();
		assertEquals(200, response.getStatus());
		roles = response.readEntity(
				new GenericType<List<UserRole>>() {});
		logout();
		assertEquals(1, roles.size());
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
		logout();
		
		//
		// Add Pinga as a viewer
		//
		loginUsingSSO("pingu", "test");
		client = getClient();
		client.path("project/"+projectId+"/role");
		UserRole role = new UserRole();
		role.setPrincipalName("pinga");
		role.setRole("viewer");;
		response = client.post(role);
		assertEquals(201, response.getStatus());
		logout();
		
		//
		// Can she view OK?
		//

		loginUsingSSO("pinga", "test");
		client = getClient();
		client.path("project/"+projectId);
		response = client.get();
		assertEquals(200, response.getStatus());
		logout();
		
		//
		// Get a list of UserRoles - we should have 2
		//
		loginUsingSSO("pingu", "test");
		client = getClient();
		client.path("project/"+projectId+"/role");
		response = client.get();
		assertEquals(200, response.getStatus());
		List<UserRole> roles = response.readEntity(
				new GenericType<List<UserRole>>() {});
		logout();
		assertEquals(2, roles.size());
	}
	
	@Test
	public void unrelatedRoles(){
		
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
		assertEquals(201, response.getStatus());
		response = getClient().path(response.getLocation().getPath()).get();
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
		
		assertEquals(400, getClient().path(pathBad).get().getStatus());
		
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
