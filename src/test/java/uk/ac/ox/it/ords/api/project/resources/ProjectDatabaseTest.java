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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.shiro.SecurityUtils;
import org.hibernate.Session;
import org.junit.Test;

import uk.ac.ox.it.ords.api.project.model.Database;
import uk.ac.ox.it.ords.api.project.model.DatabaseVersion;
import uk.ac.ox.it.ords.api.project.permissions.ProjectPermissions;
import uk.ac.ox.it.ords.api.project.services.impl.hibernate.HibernateUtils;

public class ProjectDatabaseTest extends AbstractResourceTest {
	
	// Get databases for private project
	@Test
	public void privateProjectDatabases(){
		//
		// Create project
		//
		loginUsingSSO("pingu","pingu");
		WebClient client = getClient();
		client.path("/");
		uk.ac.ox.it.ords.api.project.model.Project project = new uk.ac.ox.it.ords.api.project.model.Project();
		project.setName("Test Project H");
		project.setDescription("privateProjectDatabases");
		project.setPrivateProject(true);
		project.setOdbcSet(true);
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
		Database projectDatabase = new Database();
		projectDatabase.setDatabaseProjectId(id);
		projectDatabase.setDatabaseType("MAIN");
		projectDatabase.setDbName("test");
		projectDatabase.setDbDescription("test");
		response = client.post(projectDatabase);
		assertEquals(201, response.getStatus());
		URI projectDatabaseURI = response.getLocation();
		
		
		//
		// View
		// 
		assertEquals(200, getClient().path(projectURI.getPath()+"/database").get().getStatus());
		assertEquals(200, getClient().path(projectDatabaseURI.getPath()).get().getStatus());
		
		//
		// Check ODBC is enabled for this user
		//
		projectDatabase = getClient().path(projectDatabaseURI.getPath()).get().readEntity(Database.class);
		assertTrue(SecurityUtils.getSubject().isPermitted(ProjectPermissions.DATABASE_REQUEST_ODBC_ACCESS(projectDatabase)));
		
		//
		// Logout and view
		//
		logout();
		assertEquals(403, getClient().path(projectURI.getPath()+"/database").get().getStatus());
		assertEquals(403, getClient().path(projectDatabaseURI.getPath()).get().getStatus());
		
	}
	
	@Test
	public void addAndModifyDatabase(){
		//
		// Create project
		//
		loginUsingSSO("pingu","pingu");
		WebClient client = getClient();
		client.path("/");
		uk.ac.ox.it.ords.api.project.model.Project project = new uk.ac.ox.it.ords.api.project.model.Project();
		project.setName("Test Project I");
		project.setDescription("deleteDatabaseFromDeletedProject");
		Response response = client.post(project);
		assertEquals(201, response.getStatus());
		String path = response.getLocation().getPath();
		response = getClient().path(path).get();
		project = response.readEntity(uk.ac.ox.it.ords.api.project.model.Project.class);
		assertEquals("Test Project I", project.getName());
		int id = project.getProjectId();
		
		//
		// Add a database
		//
		client = getClient();
		client.path("/"+id+"/database");		
		Database projectDatabase = new Database();
		projectDatabase.setDatabaseProjectId(id);
		projectDatabase.setDatabaseType("MAIN");
		projectDatabase.setDbName("test");
		projectDatabase.setDbDescription("test");
		response = client.post(projectDatabase);
		assertEquals(201, response.getStatus());
		URI projectDatabaseURI = response.getLocation();
		projectDatabase = getClient().path(projectDatabaseURI.getPath()).get().readEntity(Database.class);
		
		//
		// Update
		//
		projectDatabase.setDataGatheringProcess("Random");
		assertEquals(200, getClient().path(projectDatabaseURI.getPath()).put(projectDatabase).getStatus());
		
		//
		// Check it updated OK
		//
		projectDatabase = getClient().path(projectDatabaseURI.getPath()).get().readEntity(Database.class);
		assertEquals("Random", projectDatabase.getDataGatheringProcess());
	}
	
	@Test
	public void updateWithErrors(){
		// Project does not exist
		assertEquals(404, getClient().path("/999/database/999").put(null).getStatus());
		
		//
		// Create project
		//
		loginUsingSSO("pingu","pingu");
		WebClient client = getClient();
		client.path("/");
		uk.ac.ox.it.ords.api.project.model.Project project = new uk.ac.ox.it.ords.api.project.model.Project();
		project.setName("Test Project I");
		project.setDescription("deleteDatabaseFromDeletedProject");
		Response response = client.post(project);
		assertEquals(201, response.getStatus());
		String path = response.getLocation().getPath();
		response = getClient().path(path).get();
		project = response.readEntity(uk.ac.ox.it.ords.api.project.model.Project.class);
		assertEquals("Test Project I", project.getName());
		int id = project.getProjectId();
		
		// Database does not exist
		assertEquals(404, getClient().path("/"+id+"/database/999").put(null).getStatus());
		
		//
		// Add a database
		//
		client = getClient();
		client.path("/"+id+"/database");		
		Database projectDatabase = new Database();
		projectDatabase.setDatabaseProjectId(id);
		projectDatabase.setDatabaseType("MAIN");
		projectDatabase.setDbName("test");
		projectDatabase.setDbDescription("test");
		response = client.post(projectDatabase);
		assertEquals(201, response.getStatus());
		URI projectDatabaseURI = response.getLocation();
		projectDatabase = getClient().path(projectDatabaseURI.getPath()).get().readEntity(Database.class);
		
		//
		// Update without permission
		//
		logout();
		assertEquals(403, getClient().path(projectDatabaseURI.getPath()).put(projectDatabase).getStatus());

		
		//
		// Update with null
		//
		loginUsingSSO("pingu","pingu");
		assertEquals(400, getClient().path(projectDatabaseURI.getPath()).put(null).getStatus());
		
		//
		// Update with invalid id
		//
		projectDatabase.setLogicalDatabaseId(9999);
		assertEquals(400, getClient().path(projectDatabaseURI.getPath()).put(projectDatabase).getStatus());

	}
	
	// Delete from deleted project
	@Test
	public void deleteDatabaseFromDeletedProject(){
		//
		// Create project
		//
		loginUsingSSO("pingu","pingu");
		WebClient client = getClient();
		client.path("/");
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
		client.path("/"+id+"/database");		
		Database projectDatabase = new Database();
		projectDatabase.setDatabaseProjectId(id);
		projectDatabase.setDatabaseType("MAIN");
		projectDatabase.setDbName("test");
		projectDatabase.setDbDescription("test");
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
		int id1 = project.getProjectId();
		
		client = getClient();
		client.path("/"+id1+"/database");		
		Database projectDatabase1 = new Database();
		projectDatabase1.setDatabaseProjectId(id1);
		projectDatabase1.setDatabaseType("MAIN");
		projectDatabase1.setDbName("test");
		projectDatabase1.setDbDescription("test");
		response = client.post(projectDatabase1);
		assertEquals(201, response.getStatus());
		URI projectDatabaseURI1 = response.getLocation();
		int databaseId1 = getClient().path(projectDatabaseURI1.getPath()).get().readEntity(Database.class).getLogicalDatabaseId();
		
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
		int id2 = project.getProjectId();
		
		//
		// Add using wrong project id
		//
		client = getClient();
		client.path("/"+id2+"/database");		
		Database projectDatabase2 = new Database();
		projectDatabase2.setDatabaseType("MAIN");
		projectDatabase2.setDbName("test");
		projectDatabase2.setDbDescription("test");
		projectDatabase2.setDatabaseProjectId(id1);
		response = client.post(projectDatabase2);
		assertEquals(400, response.getStatus());
		
		client = getClient();
		client.path("/"+id2+"/database");		
		projectDatabase2.setDatabaseProjectId(id2);
		response = client.post(projectDatabase2);
		assertEquals(201, response.getStatus());
		URI projectDatabaseURI2 = response.getLocation();
		int databaseId2 = getClient().path(projectDatabaseURI2.getPath()).get().readEntity(Database.class).getLogicalDatabaseId();
		
		//
		// OK, now lets do some deleting using the wrong projects
		//
		assertEquals(400, getClient().path("/" + id1 + "/database/" + databaseId2).delete().getStatus());
		assertEquals(400, getClient().path("/" + id2 + "/database/" + databaseId1).delete().getStatus());		
	}
	
	@Test
	public void deleteDatabasesNonExisting(){
		loginUsingSSO("pingu","pingu");
		WebClient client = getClient();
		
		client = getClient();
		client.path("/");
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
		
		assertEquals(404, getClient().path("/" + id + "/database/9999").delete().getStatus());
		
		assertEquals(404, getClient().path("/9999/database/9999").delete().getStatus());		
		
	}
	
	// Side-delete a database from another project
	
	@Test
	public void addDatabasesToProjectWithErrors(){
		
		loginUsingSSO("pingu","pingu");
		
		// create a project
		WebClient client = getClient();
		client.path("/");
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
		client.path("/"+id+"/database");		
		Database projectDatabase = new Database();
		projectDatabase.setDatabaseProjectId(id);
		projectDatabase.setDatabaseType("MAIN");
		projectDatabase.setDbName("test");
		projectDatabase.setDbDescription("test");
		response = client.post(projectDatabase);
		assertEquals(201, response.getStatus());
		URI projectDatabaseURI = response.getLocation();
		
		// add database unauthenticated
		logout();
		client = getClient();
		client.path("/"+id+"/database");		
		projectDatabase = new Database();
		projectDatabase.setDatabaseProjectId(id);
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
		client.path("/"+id+"/database/9999");		
		response = client.get();
		assertEquals(404, response.getStatus());
		
		// Delete non-existant db
		client = getClient();
		client.path("/"+id+"/database/9999");
		response = client.delete();
		assertEquals(404, response.getStatus());
		
		// Add a DB where Ids don't match up
		client = getClient();
		client.path("/"+id+"/database");		
		projectDatabase = new Database();
		projectDatabase.setDatabaseProjectId(9999);
		response = client.post(projectDatabase);
		assertEquals(400, response.getStatus());

	}

	@Test
	public void getDatabaseNonExisting(){
		loginUsingSSO("pingu","pingu");
		
		WebClient client = getClient();
		client.path("/");
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
		
		path = "/" + id + "/database/9999";
		assertEquals(404, getClient().path(path).get().getStatus());
		
		path =  "/9999/database/9999";
		assertEquals(404, getClient().path(path).get().getStatus());
		
		path =  "/9999/database/";
		assertEquals(404, getClient().path(path).get().getStatus());
		
	}

	@Test
	public void addDatabasesToNoProject(){
		loginUsingSSO("pingu","pingu");
		
		//
		// Create database for non-existing project
		//
		WebClient client = getClient();
		client.path("/9999/database");		
		Database projectDatabase = new Database();
		projectDatabase.setDatabaseProjectId(9999);
		Response response = client.post(projectDatabase);
		assertEquals(404, response.getStatus());
		
		//
		// create a project and delete it, then attempt to add a database to it
		//
		client = getClient();
		client.path("/");
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
		projectDatabase.setDatabaseProjectId(id);
		assertEquals(410, getClient().path(path).post(projectDatabase).getStatus());
		assertEquals(410, getClient().path(path).get().getStatus());
		assertEquals(410, getClient().path(path+"/9999").get().getStatus());
		
		
		
	}
		
	

	@Test
	public void addDatabasesToProject(){
		
		loginUsingSSO("pingu","pingu");
		
		// create a project
		WebClient client = getClient();
		client.path("/");
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
		client.path("/"+id+"/database");		
		Database projectDatabase = new Database();
		projectDatabase.setDatabaseProjectId(id);
		projectDatabase.setDatabaseType("MAIN");
		projectDatabase.setDbName("test");
		projectDatabase.setDbDescription("test");
		response = client.post(projectDatabase);
		assertEquals(201, response.getStatus());
		
		client = getClient();
		client.path("/"+id+"/database");
		projectDatabase.setDatabaseProjectId(id);
		response = client.post(projectDatabase);
		assertEquals(201, response.getStatus());
		URI projectDatabaseURI = response.getLocation();
		
		// get them
		client = getClient();
		client.path("/"+id+"/database");
		response = client.get();
		assertEquals(200, response.getStatus());
		List<Database> projectDatabases = response.readEntity(new GenericType<List<Database>>() {});
		assertEquals(2, projectDatabases.size());
		
		// get one of them
		client = getClient();
		client.path("/"+id+"/database/"+projectDatabases.get(0).getLogicalDatabaseId());
		response = client.get();
		assertEquals(200, response.getStatus());
		Database deletedDatabase = getClient().path(projectDatabaseURI.getPath()).get().readEntity(Database.class);
		
		// delete one
		client = getClient();
		client.path(projectDatabaseURI.getPath());
		response = client.delete();
		assertEquals(200, response.getStatus());
		
		//
		// Check ODBC is disabled for the deleted database
		//
		assertFalse(SecurityUtils.getSubject().isPermitted(ProjectPermissions.DATABASE_REQUEST_ODBC_ACCESS(deletedDatabase)));
		
		// get again
		client = getClient();
		client.path("/"+id+"/database");
		response = client.get();
		assertEquals(200, response.getStatus());
		projectDatabases = response.readEntity(new GenericType<List<Database>>() {});
		assertEquals(1, projectDatabases.size());
		
		// delete the project
		client = getClient();
		client.path("/"+id);
		response = client.delete();
		assertEquals(200, response.getStatus());
	}
	
	@Test
	public void addDatabaseWithDatabaseVersion(){
		
		loginUsingSSO("pingu","pingu");
		
		// create a project
		WebClient client = getClient();
		client.path("/");
		uk.ac.ox.it.ords.api.project.model.Project project = new uk.ac.ox.it.ords.api.project.model.Project();
		project.setName("Test Project N");
		project.setDescription("addDatabasesToProject");
		Response response = client.post(project);
		assertEquals(201, response.getStatus());
		response = getClient().path(response.getLocation().getPath()).get();
		project = response.readEntity(uk.ac.ox.it.ords.api.project.model.Project.class);
		assertEquals("Test Project N", project.getName());
		int id = project.getProjectId();
		
		// add logical database
		client = getClient();
		client.path("/"+id+"/database");		
		Database projectDatabase = new Database();
		projectDatabase.setDatabaseProjectId(id);
		projectDatabase.setDatabaseType("MAIN");
		projectDatabase.setDbName("test");
		projectDatabase.setDbDescription("test");
		response = client.post(projectDatabase);
		assertEquals(201, response.getStatus());
		projectDatabase = getClient().path(response.getLocation().getPath()).get().readEntity(Database.class);
		
		// add database version
		Session session = HibernateUtils.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		DatabaseVersion version = new DatabaseVersion();
		version.setFileName("test.mdb"); 
		version.setFullPathToDirectory("/tmp");
		version.setEntityType(DatabaseVersion.EntityType.MAIN);
		version.setLogicalDatabaseId(projectDatabase.getLogicalDatabaseId());
		session.save(version);
		session.getTransaction().commit();
		
		//
		// Get the logical database, and check it contains a single database version
		//
		response = getClient().path(response.getLocation().getPath()).get();
		assertEquals(200, response.getStatus());
		projectDatabase = response.readEntity(Database.class);
		assertEquals(1, projectDatabase.getDatabaseVersions().size());
		assertEquals("test.mdb", projectDatabase.getDatabaseVersions().get(0).getFileName());
		assertEquals(DatabaseVersion.EntityType.MAIN, projectDatabase.getDatabaseVersions().get(0).getEntityType());
		
		//
		// Clean up
		//
		session = HibernateUtils.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		session.delete(version);
		session.getTransaction().commit();

		
	}

}
