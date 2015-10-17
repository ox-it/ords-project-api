package uk.ac.ox.it.ords.api.project.resources;

import static org.junit.Assert.assertEquals;

import java.net.URI;

import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.Test;

import uk.ac.ox.it.ords.api.project.model.UserRole;

public class Integration  extends AbstractResourceTest {

	@Test
	public void integrationTest(){
		
		// Set up a new project
		
		// Add a contributor
		
		// Add a viewer
		
		// Delete the project
		
		//
		// Create Project - Pingu is owner
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
		// Pingu adds Pinga as Contributor to Project 
		//
		client = getClient();
		client.path("project/"+project2+"/role");
		UserRole role = new UserRole();
		role.setPrincipalName("pinga");
		role.setRole("contributor");;
		response = client.post(role);
		assertEquals(201, response.getStatus());
		
		//
		// Pingu adds Pinga as Viewer to Project 
		//
		client = getClient();
		client.path("project/"+project2+"/role");
		role = new UserRole();
		role.setPrincipalName("pinga");
		role.setRole("viewer");;
		response = client.post(role);
		assertEquals(201, response.getStatus());
		
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
