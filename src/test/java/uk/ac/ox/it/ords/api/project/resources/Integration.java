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

import java.util.List;

import javax.ws.rs.core.GenericType;
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
		assertEquals(201, response.getStatus());
		response = getClient().path(response.getLocation().getPath()).get();
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
		loginUsingSSO("pingu", "pingu");
		client = getClient();
		client.path("project/"+project2+"/role");
		role = new UserRole();
		role.setPrincipalName("pinga");
		role.setRole("viewer");;
		response = client.post(role);
		assertEquals(201, response.getStatus());
		
		//
		// If we now get the project list as Pinga...
		//
		loginUsingSSO("pinga", "pinga");
		client = getClient();
		client.path("project/");
		response = client.get();
		List<uk.ac.ox.it.ords.api.project.model.Project> projects = response.readEntity(
				new GenericType<List<uk.ac.ox.it.ords.api.project.model.Project>>() {}
				);
		assertEquals(1, projects.size());
		
		//
		// Finally we'll delete the project
		//
		loginUsingSSO("pingu", "pingu");
		client = getClient();
		client.path("/project/"+project2);
		response = client.delete();
		assertEquals(200, response.getStatus());
		
		
		logout();
	}

}
