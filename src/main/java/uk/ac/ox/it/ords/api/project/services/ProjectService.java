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
package uk.ac.ox.it.ords.api.project.services;

import java.util.List;
import java.util.ServiceLoader;

import uk.ac.ox.it.ords.api.project.model.Project;
import uk.ac.ox.it.ords.api.project.server.ValidationException;
import uk.ac.ox.it.ords.api.project.services.impl.hibernate.ProjectServiceImpl;

public interface ProjectService {
	
	public static final String METADATA_TOKEN = "___";
	
	/**
	 * Validates the project fields against the business rules for creating and updating projects
	 * @param project the project to validate
	 * @throws Exception if any of the properties of the project violate a validation rule
	 */
	public void validate(Project project) throws Exception;
	
	/**
	 * Get the projects associated with the current user
	 * @return List of Project instances for the current user
	 */
	public List<Project> getProjects();
	
	/**
     * Find all projects that meet the criteria. Criteria are searched for in the project abstract.
     *
     * @param searchTerms a comma separated list of search terms or null, meaning get all projects
     * @return List of project objects that contain data that may be shown to the user
     */
	public List<Project> searchProjects(String searchTerms);
	
	/**
	 * get all projects visible to the current subject.
	 * @return List of visible projects
	 */
	public List<Project> getAllProjects();
	
	/**
	 * get all open projects visible  to the current subject.
	 * @return List of visible open projects
	 */
	public List<Project> getOpenProjects();
	
	/**
	 * get a project
	 * @param id the project to return
	 * @return the project, or null if it doesn't exist
	 */
	public Project getProject(int id);
	
	/**
	 * Creates a project
	 * @param project the project to create
	 * @throws ValidationException if the project metadata is invalid
	 * @throws Exception if there was a problem creating the project
	 */
	public void createProject(Project project) throws ValidationException, Exception;
	
	/**
	 * Deletes a project
	 * @param id the project to delete
	 * @throws Exception if there was a problem deleting the project
	 */
	public void deleteProject(int id) throws Exception;	
	
	/**
	 * Updates a project
	 * @param project the project to update
	 * @return the updated project
	 * @throws Exception if there was a problem updating the project
	 */
	public Project updateProject(Project project) throws Exception;
	
	/**
	 * Return the number of projects using the specified database server
	 * @param server the server to search
	 * @return the number of projects on the server.
	 */
	public int getNumberOfProjectsOnServer(String server);
	
	/**
	 * Factory for obtaining implementations
	 */
    public static class Factory {
		private static ProjectService provider;
	    public static ProjectService getInstance() {
	    	//
	    	// Use the service loader to load an implementation if one is available
	    	// Place a file called uk.ac.ox.oucs.ords.utilities.csv in src/main/resources/META-INF/services
	    	// containing the classname to load as the CsvService implementation. 
	    	// By default we load the Hibernate implementation.
	    	//
	    	if (provider == null){
	    		ServiceLoader<ProjectService> ldr = ServiceLoader.load(ProjectService.class);
	    		for (ProjectService service : ldr) {
	    			// We are only expecting one
	    			provider = service;
	    		}
	    	}
	    	//
	    	// If no service provider is found, use the default
	    	//
	    	if (provider == null){
	    		provider = new ProjectServiceImpl();
	    	}
	    	
	    	return provider;
	    }
	}
	  
}
