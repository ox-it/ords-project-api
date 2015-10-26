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

import java.io.IOException;
import java.util.List;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.Test;

import uk.ac.ox.it.ords.security.model.UserRole;

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
	public void createInvalidProject() throws IOException {
		loginUsingSSO("pingu","test");
		
		uk.ac.ox.it.ords.api.project.model.Project project = new uk.ac.ox.it.ords.api.project.model.Project();
		project.setName("createInvalidProject");
		assertEquals(400, getClient().path("project/").post(project).getStatus());
		
		project.setName("createInvalidProject___");
		project.setDescription("createInvalidProject");
		assertEquals(400, getClient().path("project/").post(project).getStatus());
		
		project.setName("createInvalidProject");
		project.setDescription("createInvalidProject___");
		assertEquals(400, getClient().path("project/").post(project).getStatus());
		
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
	public void createFullProject() throws IOException {
		
		uk.ac.ox.it.ords.api.project.model.Project project = new uk.ac.ox.it.ords.api.project.model.Project();
		project.setName("Test Project J");
		project.setDescription("createFullProject");
		project.setTrialProject(false);
		
		loginUsingSSO("pingu", "pingu");
		assertEquals(403, getClient().path("project/").post(project).getStatus());
		logout();
		
		loginUsingSSO("admin","admin");
		assertEquals(201, getClient().path("project/").post(project).getStatus());
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
		
		String projectPath = getClient().path("project/").post(project).getLocation().getPath();
	
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
		assertEquals(201, response.getStatus());
		response = getClient().path(response.getLocation().getPath()).get();
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
	
	@Test 
	public void testProjectFiltering() throws Exception{
		//
		// Create three projects as Admin; one is private
		//
		loginUsingSSO("admin", "test");
		WebClient client = getClient();
		client.path("project/");
		
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
		client.path("project/");
		client.query("open", true);
		response = client.get();
		List<uk.ac.ox.it.ords.api.project.model.Project> projects = 
				response.readEntity(
						new GenericType<List<uk.ac.ox.it.ords.api.project.model.Project>>() {}
						);
		assertEquals(2, projects.size());
		
		//
		// Now lets see what Admin can see if they ask for the list
		// of all projects - they should see all 3, including the
		// private one
		//
		loginUsingSSO("admin", "test");
		client = getClient();
		client.path("project/");
		client.query("full", true);
		response = client.get();
		projects = response.readEntity(
						new GenericType<List<uk.ac.ox.it.ords.api.project.model.Project>>() {}
						);
		assertEquals(3, projects.size());
		
		//
		// Anonymous user asking for the FULL project list sees the same
		// as the open list
		//
		logout();
		client = getClient();
		client.path("project/");
		client.query("full", true);
		response = client.get();
		projects = response.readEntity(
						new GenericType<List<uk.ac.ox.it.ords.api.project.model.Project>>() {}
						);
		assertEquals(2, projects.size());
		
		//
		// Admin asks for their projects - they'll get all 3
		//
		loginUsingSSO("admin", "test");
		client = getClient();
		client.path("project/");
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
		client.path("project/XXXX");
		Response response = client.get();
		assertEquals(404, response.getStatus());
		
	}
	
	@Test
	public void getNonexistantProject(){
		loginUsingSSO("admin", "test");
		WebClient client = getClient();
		client.path("project/99999");
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
		client.path("project/");
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
		client.path("project/"+id);
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
		client.path("project/"+id);
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
		client.path("project/"+id);
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
		client.path("project/");
		uk.ac.ox.it.ords.api.project.model.Project project = new uk.ac.ox.it.ords.api.project.model.Project();
		project.setName("Test Project V");
		project.setDescription("updateProject");
		Response response = client.post(project);
		
		assertEquals(201, response.getStatus());
		response = getClient().path(response.getLocation().getPath()).get();
		project = response.readEntity(uk.ac.ox.it.ords.api.project.model.Project.class);
		
		assertEquals("Test Project V", project.getName());
		assertEquals(true, project.isTrialProject());
		int id = project.getProjectId();
		logout();

		
		// PUT while logged out
		//
		client = getClient();
		client.path("project/"+id);
		project.setId(id);
		project.setDescription("updateProject - updated BADLY");
		response = client.put(project);
		assertEquals(403, response.getStatus());
		
		//
		// PUT - no old project
		//
		loginUsingSSO("pingu", "pingu");
		client = getClient();
		client.path("project/9999");
		response = client.put(project);
		assertEquals(404, response.getStatus());
		logout();
		
		//
		// PUT - no new project
		//
		loginUsingSSO("pingu", "pingu");
		client = getClient();
		client.path("project/"+id);
		response = client.put(null);
		assertEquals(400, response.getStatus());
		logout();

		//
		// PUT - using the wrong entity
		//
		loginUsingSSO("pingu", "pingu");
		client = getClient();
		client.path("project/"+id);
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
		client.path("project/"+id);
		response = client.get();
		assertEquals(200, response.getStatus());
		project = response.readEntity(uk.ac.ox.it.ords.api.project.model.Project.class);
		assertEquals("Test Project V", project.getName());
		assertEquals("updateProject", project.getDescription());
		logout();
		
		//
		// PUT
		//
		loginUsingSSO("pingu", "pingu");
		client = getClient();
		client.path("project/"+id);
		project.setId(id);
		project.setDescription("updateProject - updated");
		project.setStartDate("2000 BC");
		project.setEndDate("THE END OF THE WORLD");
		response = client.put(project);
		project = response.readEntity(uk.ac.ox.it.ords.api.project.model.Project.class);
		logout();
		
		//
		// GET
		//
		loginUsingSSO("pingu", "pingu");
		client = getClient();
		client.path("project/"+id);
		response = client.get();
		assertEquals(200, response.getStatus());
		project = response.readEntity(uk.ac.ox.it.ords.api.project.model.Project.class);
		assertEquals("Test Project V", project.getName());
		assertEquals("updateProject - updated", project.getDescription());
		assertEquals("2000 BC", project.getStartDate());
		assertEquals("THE END OF THE WORLD", project.getEndDate());
		logout();
		
		//
		// DELETE the project and try to modify it
		//
		loginUsingSSO("pingu", "pingu");
		assertEquals(200, getClient().path("project/"+id).delete().getStatus());
		assertEquals(410, getClient().path("project/"+id).put(project).getStatus());
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
		assertEquals(201, getClient().path("project/").post(project).getStatus());
		
		project.setName("Test Project searchProjects2");
		project.setDescription("searchProjects Octopus");
		assertEquals(201, getClient().path("project/").post(project).getStatus());
		
		project.setName("Test Project searchProjects3");
		project.setDescription("searchProjects Squid");
		assertEquals(201, getClient().path("project/").post(project).getStatus());
		
		//
		// Baseline - all projects
		//
		int openProjects = getClient().path("project/").query("open", "true").get().readEntity(new GenericType<List<uk.ac.ox.it.ords.api.project.model.Project>>() {}).size();

		
		//
		// Search projects
		//
		assertEquals(1, getClient().path("project/").query("q", "searchProjects1").get().readEntity(new GenericType<List<uk.ac.ox.it.ords.api.project.model.Project>>() {}).size());
		assertEquals(2, getClient().path("project/").query("q", "octopus").get().readEntity(new GenericType<List<uk.ac.ox.it.ords.api.project.model.Project>>() {}).size());
		assertEquals(1, getClient().path("project/").query("q", "squid").get().readEntity(new GenericType<List<uk.ac.ox.it.ords.api.project.model.Project>>() {}).size());
		assertEquals(0, getClient().path("project/").query("q", "mussell").get().readEntity(new GenericType<List<uk.ac.ox.it.ords.api.project.model.Project>>() {}).size());
		assertEquals(openProjects, getClient().path("project/").query("q", " ").get().readEntity(new GenericType<List<uk.ac.ox.it.ords.api.project.model.Project>>() {}).size());
		assertEquals(openProjects, getClient().path("project/").query("q", "").get().readEntity(new GenericType<List<uk.ac.ox.it.ords.api.project.model.Project>>() {}).size());

	}
}
