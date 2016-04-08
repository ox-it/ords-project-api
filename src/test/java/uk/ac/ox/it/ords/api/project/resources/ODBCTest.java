package uk.ac.ox.it.ords.api.project.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.shiro.SecurityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.it.ords.api.project.model.Database;
import uk.ac.ox.it.ords.api.project.permissions.ProjectPermissions;

public class ODBCTest extends AbstractResourceTest {
	
	int projectId;
	
	@Before
	public void setup(){
		
		loginUsingSSO("pingu", "pingu");

		//
		// POST project
		//
		uk.ac.ox.it.ords.api.project.model.Project project = new uk.ac.ox.it.ords.api.project.model.Project();
		project.setName("Test Project F");
		project.setDescription("createProjectAuthenticated");
		project.setOdbcSet(true);
		Response response = getClient().path("/").post(project);
		assertEquals(201, response.getStatus());
		response = getClient().path(response.getLocation().getPath()).get();
		project = response.readEntity(uk.ac.ox.it.ords.api.project.model.Project.class);
		assertEquals("Test Project F", project.getName());
		projectId = project.getProjectId();
		
		logout();
	}
	
	@After
	public void tearDown(){
		loginUsingSSO("pingu", "pingu");
		WebClient client = getClient();
		
		// delete the project
		client = getClient();
		client.path("/"+projectId);
		Response response = client.delete();
		assertEquals(200, response.getStatus());
		
		logout();
	}
	
	@Test
	public void createProjectWithODBC(){
		loginUsingSSO("pingu", "pingu");
		WebClient client = getClient();
		
		// Add Database
		client = getClient();
		client.path("/"+projectId+"/database");		
		Database projectDatabase = new Database();
		projectDatabase.setDatabaseProjectId(projectId);
		projectDatabase.setDatabaseType("MAIN");
		projectDatabase.setDbName("test");
		projectDatabase.setDbDescription("test");
		Response response = client.post(projectDatabase);
		assertEquals(201, response.getStatus());
		
		// Get Database
		client = getClient();
		client.path(response.getLocation().getPath());
		response = client.get();
		assertEquals(200, response.getStatus());
		Database db = response.readEntity(Database.class);
		
		//
		// Does the creator have the right to request ODBC access?
		//
		assertTrue(SecurityUtils.getSubject().isPermitted(ProjectPermissions.DATABASE_REQUEST_ODBC_ACCESS(db)));
		
		logout();
	}
	
	@Test
	public void revokePermissionsOnDelete(){
		loginUsingSSO("pingu", "pingu");
		WebClient client = getClient();
		
		// Add Database
		client = getClient();
		client.path("/"+projectId+"/database");		
		Database projectDatabase = new Database();
		projectDatabase.setDatabaseProjectId(projectId);
		projectDatabase.setDatabaseType("MAIN");
		projectDatabase.setDbName("test");
		projectDatabase.setDbDescription("test");
		Response response = client.post(projectDatabase);
		assertEquals(201, response.getStatus());
		
		// Get Database
		client = getClient();
		client.path(response.getLocation().getPath());
		response = client.get();
		assertEquals(200, response.getStatus());
		Database db = response.readEntity(Database.class);
		
		// Delete it
		response = client.delete();
		assertEquals(200, response.getStatus());
		
		//
		// Does the creator have the right to request ODBC access?
		//
		assertFalse(SecurityUtils.getSubject().isPermitted(ProjectPermissions.DATABASE_REQUEST_ODBC_ACCESS(db)));
		
		logout();
	}

}
