package uk.ac.ox.it.ords.api.project.resources;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.Test;

import uk.ac.ox.it.ords.api.project.model.UserRole;

public class ProjectDatabaseTest extends AbstractResourceTest {
	
	@Test
	public void addDatabasesToProjectWithErrors(){
		
		loginUsingSSO("pingu","pingu");
		
		// create a project
		WebClient client = getClient();
		client.path("project/");
		uk.ac.ox.it.ords.api.project.model.Project project = new uk.ac.ox.it.ords.api.project.model.Project();
		project.setName("Test Project N");
		project.setDescription("addDatabasesToProject");
		Response response = client.post(project);
		assertEquals(200, response.getStatus());
		project = response.readEntity(uk.ac.ox.it.ords.api.project.model.Project.class);
		assertEquals("Test Project N", project.getName());
		int id = project.getProjectId();
		
		// add database
		client = getClient();
		client.path("project/"+id+"/database");		
		uk.ac.ox.it.ords.api.project.model.ProjectDatabase projectDatabase = new uk.ac.ox.it.ords.api.project.model.ProjectDatabase();
		projectDatabase.setDbName("Test DB 1");
		projectDatabase.setProjectId(id);
		response = client.post(projectDatabase);
		assertEquals(201, response.getStatus());
		URI projectDatabaseURI = response.getLocation();
		
		// add database unauthenticated
		logout();
		client = getClient();
		client.path("project/"+id+"/database");		
		projectDatabase = new uk.ac.ox.it.ords.api.project.model.ProjectDatabase();
		projectDatabase.setDbName("Test DB 2");
		projectDatabase.setProjectId(id);
		response = client.post(projectDatabase);
		assertEquals(403, response.getStatus());
		
		// delete one unauthenticated
		client = getClient();
		client.path(projectDatabaseURI.getPath());
		response = client.delete();
		assertEquals(403, response.getStatus());
		
		loginUsingSSO("pingu","pingu");
		
		// Get non-existant db
		client = getClient();
		client.path("project/"+id+"/database/9999");		
		response = client.get();
		assertEquals(404, response.getStatus());
		
		// Delete non-existant db
		client = getClient();
		client.path("project/"+id+"/database/9999");
		response = client.delete();
		assertEquals(404, response.getStatus());
		
		// Add a DB where Ids don't match up
		client = getClient();
		client.path("project/"+id+"/database");		
		projectDatabase = new uk.ac.ox.it.ords.api.project.model.ProjectDatabase();
		projectDatabase.setDbName("Test DB 2");
		projectDatabase.setProjectId(9999);
		response = client.post(projectDatabase);
		assertEquals(400, response.getStatus());

	}
	
	

	@Test
	public void addDatabasesToProject(){
		
		loginUsingSSO("pingu","pingu");
		
		// create a project
		WebClient client = getClient();
		client.path("project/");
		uk.ac.ox.it.ords.api.project.model.Project project = new uk.ac.ox.it.ords.api.project.model.Project();
		project.setName("Test Project N");
		project.setDescription("addDatabasesToProject");
		Response response = client.post(project);
		assertEquals(200, response.getStatus());
		project = response.readEntity(uk.ac.ox.it.ords.api.project.model.Project.class);
		assertEquals("Test Project N", project.getName());
		int id = project.getProjectId();
		
		// add two databases
		client = getClient();
		client.path("project/"+id+"/database");		
		uk.ac.ox.it.ords.api.project.model.ProjectDatabase projectDatabase = new uk.ac.ox.it.ords.api.project.model.ProjectDatabase();
		projectDatabase.setDbName("Test DB 1");
		projectDatabase.setProjectId(id);
		response = client.post(projectDatabase);
		assertEquals(201, response.getStatus());
		
		client = getClient();
		client.path("project/"+id+"/database");
		projectDatabase.setDbName("Test DB 2");
		projectDatabase.setProjectId(id);
		response = client.post(projectDatabase);
		assertEquals(201, response.getStatus());
		URI projectDatabaseURI = response.getLocation();
		
		// get them
		client = getClient();
		client.path("project/"+id+"/database");
		response = client.get();
		assertEquals(200, response.getStatus());
		List<uk.ac.ox.it.ords.api.project.model.ProjectDatabase> projectDatabases = response.readEntity(new GenericType<List<uk.ac.ox.it.ords.api.project.model.ProjectDatabase>>() {});
		assertEquals(2, projectDatabases.size());
		
		// delete one
		client = getClient();
		client.path(projectDatabaseURI.getPath());
		response = client.delete();
		assertEquals(200, response.getStatus());
		
		// get again
		client = getClient();
		client.path("project/"+id+"/database");
		response = client.get();
		assertEquals(200, response.getStatus());
		projectDatabases = response.readEntity(new GenericType<List<uk.ac.ox.it.ords.api.project.model.ProjectDatabase>>() {});
		assertEquals(1, projectDatabases.size());
		
		// delete the project
		client = getClient();
		client.path("project/"+id);
		response = client.delete();
		assertEquals(200, response.getStatus());
	}

}
