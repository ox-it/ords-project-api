package uk.ac.ox.it.ords.api.project.resources;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.Test;

public class ProjectDatabaseTest extends AbstractResourceTest {
	
	// Get databases for private project
	@Test
	public void privateProjectDatabases(){
		//
		// Create project
		//
		loginUsingSSO("pingu","pingu");
		WebClient client = getClient();
		client.path("project/");
		uk.ac.ox.it.ords.api.project.model.Project project = new uk.ac.ox.it.ords.api.project.model.Project();
		project.setName("Test Project H");
		project.setDescription("privateProjectDatabases");
		project.setPrivateProject(true);
		Response response = client.post(project);
		assertEquals(201, response.getStatus());
		String path = response.getLocation().getPath();
		URI projectURI = response.getLocation();
		response = getClient().path(path).get();
		project = response.readEntity(uk.ac.ox.it.ords.api.project.model.Project.class);
		assertEquals("Test Project H", project.getName());
		int id = project.getProjectId();
		
		//
		// Add DB 
		//
		client = getClient();
		client.path(projectURI.getPath()+"/database");		
		uk.ac.ox.it.ords.api.project.model.ProjectDatabase projectDatabase = new uk.ac.ox.it.ords.api.project.model.ProjectDatabase();
		projectDatabase.setDbName("Test DB 1");
		projectDatabase.setProjectId(id);
		response = client.post(projectDatabase);
		assertEquals(201, response.getStatus());
		URI projectDatabaseURI = response.getLocation();
		
		//
		// View
		// 
		assertEquals(200, getClient().path(projectURI.getPath()+"/database").get().getStatus());
		assertEquals(200, getClient().path(projectDatabaseURI.getPath()).get().getStatus());
		
		//
		// Logout and view
		//
		logout();
		assertEquals(403, getClient().path(projectURI.getPath()+"/database").get().getStatus());
		assertEquals(403, getClient().path(projectDatabaseURI.getPath()).get().getStatus());
		
	}
	
	// Delete from deleted project
	@Test
	public void deleteDatabaseFromDeletedProject(){
		//
		// Create project
		//
		loginUsingSSO("pingu","pingu");
		WebClient client = getClient();
		client.path("project/");
		uk.ac.ox.it.ords.api.project.model.Project project = new uk.ac.ox.it.ords.api.project.model.Project();
		project.setName("Test Project I");
		project.setDescription("deleteDatabaseFromDeletedProject");
		Response response = client.post(project);
		assertEquals(201, response.getStatus());
		String path = response.getLocation().getPath();
		URI projectURI = response.getLocation();
		response = getClient().path(path).get();
		project = response.readEntity(uk.ac.ox.it.ords.api.project.model.Project.class);
		assertEquals("Test Project I", project.getName());
		int id = project.getProjectId();
		
		//
		// Add a database
		//
		client = getClient();
		client.path("project/"+id+"/database");		
		uk.ac.ox.it.ords.api.project.model.ProjectDatabase projectDatabase = new uk.ac.ox.it.ords.api.project.model.ProjectDatabase();
		projectDatabase.setDbName("Test DB 1");
		projectDatabase.setProjectId(id);
		response = client.post(projectDatabase);
		assertEquals(201, response.getStatus());
		URI projectDatabaseURI = response.getLocation();
		
		//
		// Delete the project
		//
		assertEquals(200, getClient().path(projectURI.getPath()).delete().getStatus());
		
		//
		// Delete the database
		//
		getClient().path(projectURI.getPath()).delete();
		assertEquals(410, getClient().path(projectDatabaseURI.getPath()).delete().getStatus());
		
	}
	
	@Test
	public void sideAttacks(){
		loginUsingSSO("pingu","pingu");
		WebClient client = getClient();
		
		client = getClient();
		client.path("project/");
		uk.ac.ox.it.ords.api.project.model.Project project = new uk.ac.ox.it.ords.api.project.model.Project();
		project.setName("Test Project S1");
		project.setDescription("sideAttacks");
		Response response = client.post(project);
		assertEquals(201, response.getStatus());
		String path = response.getLocation().getPath();
		response = getClient().path(path).get();
		project = response.readEntity(uk.ac.ox.it.ords.api.project.model.Project.class);
		assertEquals("Test Project S1", project.getName());
		int id1 = project.getProjectId();
		
		client = getClient();
		client.path("project/"+id1+"/database");		
		uk.ac.ox.it.ords.api.project.model.ProjectDatabase projectDatabase1 = new uk.ac.ox.it.ords.api.project.model.ProjectDatabase();
		projectDatabase1.setDbName("Test DB 1");
		projectDatabase1.setProjectId(id1);
		response = client.post(projectDatabase1);
		assertEquals(201, response.getStatus());
		URI projectDatabaseURI1 = response.getLocation();
		int databaseId1 = getClient().path(projectDatabaseURI1.getPath()).get().readEntity(uk.ac.ox.it.ords.api.project.model.ProjectDatabase.class).getProjectDatabaseId();
		
		client = getClient();
		client.path("project/");
		uk.ac.ox.it.ords.api.project.model.Project project2 = new uk.ac.ox.it.ords.api.project.model.Project();
		project2.setName("Test Project S2");
		project2.setDescription("sideAttacks");
		response = client.post(project);
		assertEquals(201, response.getStatus());
		path = response.getLocation().getPath();
		response = getClient().path(path).get();
		project = response.readEntity(uk.ac.ox.it.ords.api.project.model.Project.class);
		assertEquals("Test Project S1", project.getName());
		int id2 = project.getProjectId();
		
		//
		// Add using wrong project id
		//
		client = getClient();
		client.path("project/"+id2+"/database");		
		uk.ac.ox.it.ords.api.project.model.ProjectDatabase projectDatabase2 = new uk.ac.ox.it.ords.api.project.model.ProjectDatabase();
		projectDatabase2.setDbName("Test DB 2");
		projectDatabase2.setProjectId(id1);
		response = client.post(projectDatabase2);
		assertEquals(400, response.getStatus());
		
		client = getClient();
		client.path("project/"+id2+"/database");		
		projectDatabase2.setProjectId(id2);
		response = client.post(projectDatabase2);
		assertEquals(201, response.getStatus());
		URI projectDatabaseURI2 = response.getLocation();
		int databaseId2 = getClient().path(projectDatabaseURI2.getPath()).get().readEntity(uk.ac.ox.it.ords.api.project.model.ProjectDatabase.class).getProjectDatabaseId();
		
		//
		// OK, now lets do some deleting using the wrong projects
		//
		assertEquals(400, getClient().path("project/" + id1 + "/database/" + databaseId2).delete().getStatus());
		assertEquals(400, getClient().path("project/" + id2 + "/database/" + databaseId1).delete().getStatus());		
	}
	
	@Test
	public void deleteDatabasesNonExisting(){
		loginUsingSSO("pingu","pingu");
		WebClient client = getClient();
		
		client = getClient();
		client.path("project/");
		uk.ac.ox.it.ords.api.project.model.Project project = new uk.ac.ox.it.ords.api.project.model.Project();
		project.setName("Test Project O");
		project.setDescription("getDatabaseNonExisting");
		Response response = client.post(project);
		assertEquals(201, response.getStatus());
		String path = response.getLocation().getPath();
		response = getClient().path(path).get();
		project = response.readEntity(uk.ac.ox.it.ords.api.project.model.Project.class);
		assertEquals("Test Project O", project.getName());
		int id = project.getProjectId();
		
		assertEquals(404, getClient().path("project/" + id + "/database/9999").delete().getStatus());
		
		assertEquals(404, getClient().path("project/9999/database/9999").delete().getStatus());		
		
	}
	
	// Side-delete a database from another project
	
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
		assertEquals(201, response.getStatus());
		response = getClient().path(response.getLocation().getPath()).get();
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
	public void getDatabaseNonExisting(){
		loginUsingSSO("pingu","pingu");
		
		WebClient client = getClient();
		client.path("project/");
		uk.ac.ox.it.ords.api.project.model.Project project = new uk.ac.ox.it.ords.api.project.model.Project();
		project.setName("Test Project O");
		project.setDescription("getDatabaseNonExisting");
		Response response = client.post(project);
		assertEquals(201, response.getStatus());
		String path = response.getLocation().getPath();
		response = getClient().path(path).get();
		project = response.readEntity(uk.ac.ox.it.ords.api.project.model.Project.class);
		assertEquals("Test Project O", project.getName());
		int id = project.getProjectId();
		
		path = "project/" + id + "/database/9999";
		assertEquals(404, getClient().path(path).get().getStatus());
		
		path =  "project/9999/database/9999";
		assertEquals(404, getClient().path(path).get().getStatus());
		
		path =  "project/9999/database/";
		assertEquals(404, getClient().path(path).get().getStatus());
		
	}

	@Test
	public void addDatabasesToNoProject(){
		loginUsingSSO("pingu","pingu");
		
		//
		// Create database for non-existing project
		//
		WebClient client = getClient();
		client.path("project/9999/database");		
		uk.ac.ox.it.ords.api.project.model.ProjectDatabase projectDatabase = new uk.ac.ox.it.ords.api.project.model.ProjectDatabase();
		projectDatabase.setDbName("Test DB 1");
		projectDatabase.setProjectId(9999);
		Response response = client.post(projectDatabase);
		assertEquals(404, response.getStatus());
		
		//
		// create a project and delete it, then attempt to add a database to it
		//
		client = getClient();
		client.path("project/");
		uk.ac.ox.it.ords.api.project.model.Project project = new uk.ac.ox.it.ords.api.project.model.Project();
		project.setName("Test Project G");
		project.setDescription("addDatabasesToNoProject");
		response = client.post(project);
		assertEquals(201, response.getStatus());
		String path = response.getLocation().getPath();
		response = getClient().path(path).get();
		project = response.readEntity(uk.ac.ox.it.ords.api.project.model.Project.class);
		assertEquals("Test Project G", project.getName());
		int id = project.getProjectId();
		getClient().path(path).delete();
		assertEquals(410, getClient().path(path).get().getStatus());
		
		path = path + "/database";
		projectDatabase.setProjectId(id);
		assertEquals(410, getClient().path(path).post(projectDatabase).getStatus());
		assertEquals(410, getClient().path(path).get().getStatus());
		assertEquals(410, getClient().path(path+"/9999").get().getStatus());
		
		
		
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
		assertEquals(201, response.getStatus());
		response = getClient().path(response.getLocation().getPath()).get();
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
		
		// get one of them
		client = getClient();
		client.path("project/"+id+"/database/"+projectDatabases.get(0).getProjectDatabaseId());
		response = client.get();
		assertEquals(200, response.getStatus());
		
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
