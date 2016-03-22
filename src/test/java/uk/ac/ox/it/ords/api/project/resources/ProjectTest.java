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

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.shiro.SecurityUtils;
import org.junit.Test;

import uk.ac.ox.it.ords.api.project.permissions.ProjectPermissions;
import uk.ac.ox.it.ords.security.model.UserRole;

public class ProjectTest extends AbstractResourceTest {

	
	@Test
	public void deleteNonexistingProject(){
		WebClient client = getClient();
		client.path("/99999");
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
		client.path("/");

		uk.ac.ox.it.ords.api.project.model.Project project = new uk.ac.ox.it.ords.api.project.model.Project();
		project.setName("Test Project X");
		project.setDescription("deleteProjectUnauthenticated");
		Response response = client.post(project);
		
		assertEquals(201, response.getStatus());
		response = getClient().path(response.getLocation().getPath()).get();
		project = response.readEntity(uk.ac.ox.it.ords.api.project.model.Project.class);
		
		assertEquals("Test Project X", project.getName());
		int id = project.getProjectId();
		
		//
		// DELETE logged out
		//
		logout();
		client = getClient();
		client.path("/"+id);
		response = client.delete();
		assertEquals(403, response.getStatus());
		
		//
		// DELETE as Pinga
		//
		loginUsingSSO("pinga","test");
		client = getClient();
		client.path("/"+id);
		response = client.delete();
		assertEquals(403, response.getStatus());
		logout();
	}
	
	
	@Test
	public void createNullProject() throws IOException {
		loginUsingSSO("pingu","test");
		WebClient client = getClient();
		client.path("/");
		Response response = client.post(null);
		assertEquals(400, response.getStatus());
		logout();
	}
	
	
	
	@Test
	public void createIncompleteProject() throws IOException {
		loginUsingSSO("pingu","test");
		WebClient client = getClient();
		client.path("/");
		uk.ac.ox.it.ords.api.project.model.Project project = new uk.ac.ox.it.ords.api.project.model.Project();
		project.setDescription("createIncompleteProject");
		Response response = client.post(project);
		assertEquals(400, response.getStatus());
		logout();
	}
	
	
	@Test
	public void createInvalidProject() throws IOException {
		loginUsingSSO("pingu","test");
		
		uk.ac.ox.it.ords.api.project.model.Project project = new uk.ac.ox.it.ords.api.project.model.Project();
		project.setName("createInvalidProject");
		assertEquals(400, getClient().path("/").post(project).getStatus());
		
		project.setName("createInvalidProject___");
		project.setDescription("createInvalidProject");
		assertEquals(400, getClient().path("/").post(project).getStatus());
		
		project.setName("createInvalidProject");
		project.setDescription("createInvalidProject___");
		assertEquals(400, getClient().path("/").post(project).getStatus());
		
		logout();
	}
	
	
	@Test
	public void createProjectUnauthenticated() throws IOException {
		WebClient client = getClient();
		client.path("/");
		
		uk.ac.ox.it.ords.api.project.model.Project project = new uk.ac.ox.it.ords.api.project.model.Project();
		project.setName("Test Project A");
		project.setDescription("Test Project A");
		Response response = client.post(project);
		assertEquals(403, response.getStatus());
	}

	
	@Test
	public void createFullProject() throws IOException {
		
		uk.ac.ox.it.ords.api.project.model.Project project = new uk.ac.ox.it.ords.api.project.model.Project();
		project.setName("Test Project J");
		project.setDescription("createFullProject");
		project.setTrialProject(false);
		
		loginUsingSSO("pingu", "pingu");
		assertEquals(403, getClient().path("/").post(project).getStatus());
		logout();
		
		loginUsingSSO("admin","admin");
		assertEquals(201, getClient().path("/").post(project).getStatus());
		logout();
	}
	
	@Test
	public void updateFullProject(){
		//
		// Full project
		//
		uk.ac.ox.it.ords.api.project.model.Project project = new uk.ac.ox.it.ords.api.project.model.Project();
		project.setName("Test Project J");
		project.setDescription("createFullProject");
		project.setTrialProject(false);
		
		//
		// Admin will create it, and add Pingu to owner role
		//
		loginUsingSSO("admin","admin");
		
		String projectPath = getClient().path("/").post(project).getLocation().getPath();
	
		UserRole role = new UserRole();
		role.setPrincipalName("pingu");
		role.setRole("owner");
		getClient().path(projectPath+"/role").post(role);
		
		logout();
		
		//
		// Lets check our Owner can still update the project
		//
		loginUsingSSO("pingu", "pingu");
		project = getClient().path(projectPath).get().readEntity(uk.ac.ox.it.ords.api.project.model.Project.class);
		project.setDescription("createFullProject - edited");
		assertEquals(200, getClient().path(projectPath).put(project).getStatus());
		logout();
	}

	
	@Test
	public void createProjectWithODBC() throws IOException {
		loginUsingSSO("pingu", "pingu");
		WebClient client = getClient();
		client.path("/");
		
		//
		// POST project
		//
		uk.ac.ox.it.ords.api.project.model.Project project = new uk.ac.ox.it.ords.api.project.model.Project();
		project.setName("Test Project F2");
		project.setDescription("createProjectWithODBC");
		project.setOdbcSet(true);
		Response response = client.post(project);
		assertEquals(201, response.getStatus());
		response = getClient().path(response.getLocation().getPath()).get();
		project = response.readEntity(uk.ac.ox.it.ords.api.project.model.Project.class);
		assertEquals("Test Project F2", project.getName());
		int id = project.getProjectId();
		
		//
		// GET the project
		//
		client = getClient();
		client.path("/"+id);
		response = client.get();
		assertEquals(200, response.getStatus());
		project = response.readEntity(uk.ac.ox.it.ords.api.project.model.Project.class);
		assertEquals("Test Project F2", project.getName());
		assertEquals(true, project.canEdit());
		assertEquals(true, project.canDelete());
				
		//
		// DELETE the project
		//
		client = getClient();
		client.path("/"+id);
		response = client.delete();
		assertEquals(200, response.getStatus());
		
		//
		// GET it again - is it GONE?
		//
		client = getClient();
		client.path("/"+id);
		response = client.get();
		assertEquals(410, response.getStatus());
		
		logout();
	}
	
	@Test
	public void createProjectAuthenticated() throws IOException {
		loginUsingSSO("pingu", "pingu");
		WebClient client = getClient();
		client.path("/");
		
		//
		// POST project
		//
		uk.ac.ox.it.ords.api.project.model.Project project = new uk.ac.ox.it.ords.api.project.model.Project();
		project.setName("Test Project F");
		project.setDescription("createProjectAuthenticated");
		Response response = client.post(project);
		assertEquals(201, response.getStatus());
		response = getClient().path(response.getLocation().getPath()).get();
		project = response.readEntity(uk.ac.ox.it.ords.api.project.model.Project.class);
		assertEquals("Test Project F", project.getName());
		int id = project.getProjectId();
		
		//
		// GET the project
		//
		client = getClient();
		client.path("/"+id);
		response = client.get();
		assertEquals(200, response.getStatus());
		project = response.readEntity(uk.ac.ox.it.ords.api.project.model.Project.class);
		assertEquals("Test Project F", project.getName());
		assertEquals(true, project.canEdit());
		assertEquals(true, project.canDelete());
				
		//
		// DELETE the project
		//
		client = getClient();
		client.path("/"+id);
		response = client.delete();
		assertEquals(200, response.getStatus());
		
		//
		// GET it again - is it GONE?
		//
		client = getClient();
		client.path("/"+id);
		response = client.get();
		assertEquals(410, response.getStatus());
		
		logout();
	}
	
	@Test 
	public void testProjectFiltering() throws Exception{
		//
		// Create three projects as Admin; one is private
		//
		loginUsingSSO("admin", "test");
		WebClient client = getClient();
		client.path("/");
		
		uk.ac.ox.it.ords.api.project.model.Project project = new uk.ac.ox.it.ords.api.project.model.Project();
		project.setName("Apple");
		project.setDescription("createProjectAuthenticated");
		project.setPrivateProject(false);
		project.setTrialProject(false);
		Response response = client.post(project);

		assertEquals(201, response.getStatus());
		response = getClient().path(response.getLocation().getPath()).get();
		project = response.readEntity(uk.ac.ox.it.ords.api.project.model.Project.class);
		
		assertEquals("Apple", project.getName());
		
		project.setName("Orange");
		project.setDescription("createProjectAuthenticated");
		project.setPrivateProject(true);
		project.setTrialProject(false);
		response = client.post(project);
		
		assertEquals(201, response.getStatus());
		response = getClient().path(response.getLocation().getPath()).get();
		project = response.readEntity(uk.ac.ox.it.ords.api.project.model.Project.class);
		
		assertEquals("Orange", project.getName());
		
		project.setName("Banana");
		project.setDescription("createProjectAuthenticated");
		project.setPrivateProject(false);
		project.setTrialProject(false);
		response = client.post(project);
		
		assertEquals(201, response.getStatus());
		response = getClient().path(response.getLocation().getPath()).get();
		project = response.readEntity(uk.ac.ox.it.ords.api.project.model.Project.class);
		
		assertEquals("Banana", project.getName());
		
		//
		// Now logout and GET the open project list.
		//
		logout();
		client = getClient();
		client.path("/");
		client.query("open", true);
		response = client.get();
		List<uk.ac.ox.it.ords.api.project.model.Project> projects = 
				response.readEntity(
						new GenericType<List<uk.ac.ox.it.ords.api.project.model.Project>>() {}
						);
		assertEquals(2, projects.size());
		
		//
		// Now lets see what Admin can see if they ask for the list
		// of all projects - they should see all 6, including the
		// private one and 3 deleted ones
		//
		loginUsingSSO("admin", "test");
		client = getClient();
		client.path("/");
		client.query("all", true);
		response = client.get();
		projects = response.readEntity(
						new GenericType<List<uk.ac.ox.it.ords.api.project.model.Project>>() {}
						);
		assertEquals(6, projects.size());
		
		//
		// Anonymous user asking for the FULL project list sees the same
		// as the open list plus deleted public projects
		//
		logout();
		client = getClient();
		client.path("/");
		client.query("all", true);
		response = client.get();
		projects = response.readEntity(
						new GenericType<List<uk.ac.ox.it.ords.api.project.model.Project>>() {}
						);
		assertEquals(5, projects.size());
		
		//
		// Admin asks for their projects - they'll get all 3
		//
		loginUsingSSO("admin", "test");
		client = getClient();
		client.path("/");
		response = client.get();
		projects = response.readEntity(
						new GenericType<List<uk.ac.ox.it.ords.api.project.model.Project>>() {}
						);
		assertEquals(3, projects.size());
	}
	
	@Test
	public void getMalformedRequest(){
		loginUsingSSO("admin", "test");
		WebClient client = getClient();
		client.path("/XXXX");
		Response response = client.get();
		assertEquals(404, response.getStatus());
		
	}
	
	@Test
	public void getNonexistantProject(){
		loginUsingSSO("admin", "test");
		WebClient client = getClient();
		client.path("/99999");
		Response response = client.get();
		assertEquals(404, response.getStatus());
		
	}

	@Test
	public void upgradeProject(){	
		//
		// POST project
		//
		loginUsingSSO("pingu", "pingu");
		WebClient client = getClient();
		client.path("/");
		uk.ac.ox.it.ords.api.project.model.Project project = new uk.ac.ox.it.ords.api.project.model.Project();
		project.setName("Test Project U");
		project.setDescription("upgradeProject");
		Response response = client.post(project);
		assertEquals(201, response.getStatus());
		response = getClient().path(response.getLocation().getPath()).get();
		project = response.readEntity(uk.ac.ox.it.ords.api.project.model.Project.class);
		assertEquals("Test Project U", project.getName());
		assertEquals(true, project.isTrialProject());
		int id = project.getProjectId();
		logout();

		//
		// Pingu PUT
		//
		loginUsingSSO("pingu", "pingu");
		client = getClient();
		client.path("/"+id);
		project.setId(id);
		project.setTrialProject(false);
		response = client.put(project);
		assertEquals(403, response.getStatus());
		logout();
		
		//
		// Admin PUT
		//
		loginUsingSSO("admin", "admin");
		client = getClient();
		client.path("/"+id);
		project.setId(id);
		project.setTrialProject(false);
		response = client.put(project);
		project = response.readEntity(uk.ac.ox.it.ords.api.project.model.Project.class);
		assertEquals(false, project.isTrialProject());
		logout();
		
		//
		// GET
		//
		loginUsingSSO("pingu", "pingu");
		client = getClient();
		client.path("/"+id);
		response = client.get();
		assertEquals(200, response.getStatus());
		project = response.readEntity(uk.ac.ox.it.ords.api.project.model.Project.class);
		assertEquals("Test Project U", project.getName());
		assertEquals(false, project.isTrialProject());
		logout();
	}
	
	@Test
	public void updateProject(){	
		//
		// POST project
		//
		loginUsingSSO("pingu", "pingu");
		WebClient client = getClient();
		client.path("/");
		uk.ac.ox.it.ords.api.project.model.Project projectToCreate = new uk.ac.ox.it.ords.api.project.model.Project();
		projectToCreate.setName("Test Project V");
		projectToCreate.setDescription("updateProject");
		Response response = client.post(projectToCreate);
		
		assertEquals(201, response.getStatus());
		response = getClient().path(response.getLocation().getPath()).get();
		assertEquals(200, response.getStatus());
		ProjectTestModel project = response.readEntity(ProjectTestModel.class);

		assertEquals("localhost", project.getDbServerPublicAddress());
		assertEquals("Test Project V", project.getName());
		assertEquals(true, project.isTrialProject());
		int id = project.getProjectId();
		logout();

		
		// PUT while logged out
		//
		client = getClient();
		client.path("/"+id);
		project.setId(id);
		project.setDescription("updateProject - updated BADLY");
		response = client.put(project);
		assertEquals(403, response.getStatus());
		
		//
		// PUT - no old project
		//
		loginUsingSSO("pingu", "pingu");
		client = getClient();
		client.path("/9999");
		response = client.put(project);
		assertEquals(404, response.getStatus());
		logout();
		
		//
		// PUT - no new project
		//
		loginUsingSSO("pingu", "pingu");
		client = getClient();
		client.path("/"+id);
		response = client.put(null);
		assertEquals(400, response.getStatus());
		logout();

		//
		// PUT - using the wrong entity
		//
		loginUsingSSO("pingu", "pingu");
		client = getClient();
		client.path("/"+id);
		project.setId(26);
		project.setDescription("updateProject - updated BADLY");
		response = client.put(project);
		assertEquals(400, response.getStatus());
		logout();

		
		//
		// Check none of the previous efforts actually updated it
		//
		loginUsingSSO("pingu", "pingu");
		client = getClient();
		client.path("/"+id);
		response = client.get();
		assertEquals(200, response.getStatus());
		project = response.readEntity(ProjectTestModel.class);
		assertEquals("Test Project V", project.getName());
		assertEquals("updateProject", project.getDescription());
		logout();
		
		//
		// PUT
		//
		loginUsingSSO("pingu", "pingu");
		client = getClient();
		client.path("/"+id);
		project.setId(id);
		project.setDescription("updateProject - updated");
		project.setStartDate("2000 BC");
		project.setEndDate("THE END OF THE WORLD");
		
		//
		// Enable ODBC
		//
		//project.setOdbcSet(true);	
		
		//
		// Set some things we aren't allowed to - these should be ignored
		//
		project.setProjectUuid("99");
		project.setDbServerAddress("banana");
		project.setDbServerPublicAddress("banana");
		project.setDateCreated(new Date());
		
		response = client.put(project);
		project = response.readEntity(ProjectTestModel.class);
		logout();
		
		//
		// GET
		//
		loginUsingSSO("pingu", "pingu");
		client = getClient();
		client.path("/"+id);
		response = client.get();
		assertEquals(200, response.getStatus());
		project = response.readEntity(ProjectTestModel.class);
		assertEquals("Test Project V", project.getName());
		assertEquals("updateProject - updated", project.getDescription());
		assertEquals("2000 BC", project.getStartDate());
		assertEquals("THE END OF THE WORLD", project.getEndDate());
		assertEquals("localhost", project.getDbServerPublicAddress());
		logout();
		
		//
		// DELETE the project and try to modify it
		//
		loginUsingSSO("pingu", "pingu");
		assertEquals(200, getClient().path("/"+id).delete().getStatus());
		project.setDeleted(true);
		assertEquals(410, getClient().path("/"+id).put(project).getStatus());
		
		//
		// Try to Restore the project
		//
		project.setDeleted(false);
		assertEquals(403, getClient().path("/"+id).put(project).getStatus());
		logout();
		
		//
		// Admin can restore the project
		//
		loginUsingSSO("admin", "admin");
		assertEquals(200, getClient().path("/"+id).put(project).getStatus());
		
		//
		// And then delete it again!
		//
		assertEquals(200, getClient().path("/"+id).delete().getStatus());
		logout();

	}
	

	@Test
	public void searchProjects(){
		
		loginUsingSSO("admin", "admin");
		
		uk.ac.ox.it.ords.api.project.model.Project project = new uk.ac.ox.it.ords.api.project.model.Project();
		project.setTrialProject(false);
		
		//
		// POST projects
		//
		project.setName("Test Project searchProjects1");
		project.setDescription("searchProjects Octopus");
		assertEquals(201, getClient().path("/").post(project).getStatus());
		
		project.setName("Test Project searchProjects2");
		project.setDescription("searchProjects Octopus");
		assertEquals(201, getClient().path("/").post(project).getStatus());
		
		project.setName("Test Project searchProjects3");
		project.setDescription("searchProjects Squid");
		Response response = getClient().path("/").post(project);
		assertEquals(201, response.getStatus());
		String path = response.getLocation().getPath();
		response = getClient().path(path).get();
		project = response.readEntity(uk.ac.ox.it.ords.api.project.model.Project.class);
		
		//
		// Baseline - all projects
		//
		int openProjects = getClient().path("/").query("open", "true").get().readEntity(new GenericType<List<uk.ac.ox.it.ords.api.project.model.Project>>() {}).size();

		
		//
		// Search projects
		//
		assertEquals(1, getClient().path("/").query("q", "searchProjects1").get().readEntity(new GenericType<List<uk.ac.ox.it.ords.api.project.model.Project>>() {}).size());
		assertEquals(2, getClient().path("/").query("q", "octopus").get().readEntity(new GenericType<List<uk.ac.ox.it.ords.api.project.model.Project>>() {}).size());
		assertEquals(1, getClient().path("/").query("q", "squid").get().readEntity(new GenericType<List<uk.ac.ox.it.ords.api.project.model.Project>>() {}).size());
		assertEquals(0, getClient().path("/").query("q", "mussell").get().readEntity(new GenericType<List<uk.ac.ox.it.ords.api.project.model.Project>>() {}).size());
		assertEquals(openProjects, getClient().path("/").query("q", " ").get().readEntity(new GenericType<List<uk.ac.ox.it.ords.api.project.model.Project>>() {}).size());
		assertEquals(openProjects, getClient().path("/").query("q", "").get().readEntity(new GenericType<List<uk.ac.ox.it.ords.api.project.model.Project>>() {}).size());

		//
		// Exclude project from search
		//
		project.setPrivateProject(true);
		assertEquals(200, getClient().path(path).put(project).getStatus());
		assertEquals(0, getClient().path("/").query("q", "squid").get().readEntity(new GenericType<List<uk.ac.ox.it.ords.api.project.model.Project>>() {}).size());

		//
		// Re-include
		//
		project.setPrivateProject(false);
		assertEquals(200, getClient().path(path).put(project).getStatus());
		assertEquals(1, getClient().path("/").query("q", "squid").get().readEntity(new GenericType<List<uk.ac.ox.it.ords.api.project.model.Project>>() {}).size());

	}
}
