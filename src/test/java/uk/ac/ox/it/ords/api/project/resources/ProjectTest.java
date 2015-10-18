package uk.ac.ox.it.ords.api.project.resources;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.Test;

public class ProjectTest extends AbstractResourceTest {

	
	@Test
	public void deleteNonexistingProject(){
		WebClient client = getClient();
		client.path("project/99999");
		Response response = client.delete();
		assertEquals(404, response.getStatus());
	}
	
	@Test
	public void deleteProjectUnauthenticated(){
		
		//
		// POST project as Pingu
		//
		loginUsingSSO("pingu", "pingu");
		WebClient client = getClient();
		client.path("project/");

		uk.ac.ox.it.ords.api.project.model.Project project = new uk.ac.ox.it.ords.api.project.model.Project();
		project.setName("Test Project X");
		project.setDescription("deleteProjectUnauthenticated");
		Response response = client.post(project);
		assertEquals(200, response.getStatus());
		project = response.readEntity(uk.ac.ox.it.ords.api.project.model.Project.class);
		assertEquals("Test Project X", project.getName());
		int id = project.getProjectId();
		
		//
		// DELETE logged out
		//
		logout();
		client = getClient();
		client.path("project/"+id);
		response = client.delete();
		assertEquals(403, response.getStatus());
		
		//
		// DELETE as Pinga
		//
		loginUsingSSO("pinga","test");
		client = getClient();
		client.path("project/"+id);
		response = client.delete();
		assertEquals(403, response.getStatus());
		logout();
	}
	
	
	@Test
	public void createNullProject() throws IOException {
		loginUsingSSO("pingu","test");
		WebClient client = getClient();
		client.path("project/");
		Response response = client.post(null);
		assertEquals(400, response.getStatus());
		logout();
	}
	
	
	
	@Test
	public void createIncompleteProject() throws IOException {
		loginUsingSSO("pingu","test");
		WebClient client = getClient();
		client.path("project/");
		uk.ac.ox.it.ords.api.project.model.Project project = new uk.ac.ox.it.ords.api.project.model.Project();
		project.setDescription("createIncompleteProject");
		Response response = client.post(project);
		assertEquals(400, response.getStatus());
		logout();
	}
	
	
	@Test
	public void createProjectUnauthenticated() throws IOException {
		WebClient client = getClient();
		client.path("project/");
		
		uk.ac.ox.it.ords.api.project.model.Project project = new uk.ac.ox.it.ords.api.project.model.Project();
		project.setName("Test Project A");
		project.setDescription("Test Project A");
		Response response = client.post(project);
		assertEquals(403, response.getStatus());
	}
	
	@Test
	public void createProjectAuthenticated() throws IOException {
		loginUsingSSO("pingu", "pingu");
		WebClient client = getClient();
		client.path("project/");
		
		//
		// POST project
		//
		uk.ac.ox.it.ords.api.project.model.Project project = new uk.ac.ox.it.ords.api.project.model.Project();
		project.setName("Test Project F");
		project.setDescription("createProjectAuthenticated");
		Response response = client.post(project);
		assertEquals(200, response.getStatus());
		project = response.readEntity(uk.ac.ox.it.ords.api.project.model.Project.class);
		assertEquals("Test Project F", project.getName());
		int id = project.getProjectId();
		
		//
		// GET the project
		//
		client = getClient();
		client.path("project/"+id);
		response = client.get();
		assertEquals(200, response.getStatus());
		project = response.readEntity(uk.ac.ox.it.ords.api.project.model.Project.class);
		assertEquals("Test Project F", project.getName());
		
		//
		// DELETE the project
		//
		client = getClient();
		client.path("project/"+id);
		response = client.delete();
		assertEquals(200, response.getStatus());
		
		//
		// GET it again - is it GONE?
		//
		client = getClient();
		client.path("project/"+id);
		response = client.get();
		assertEquals(410, response.getStatus());
		
		logout();
	}
	



}
