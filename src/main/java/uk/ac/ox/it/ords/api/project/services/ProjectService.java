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
	 * @param project
	 * @throws Exception if any of the properties of the project violate a validation rule
	 */
	public void validate(Project project) throws Exception;
	
	public List<Project> getProjects();
	public List<Project> getFullProjects();
	public List<Project> getOpenProjects();
	
	public Project getProject(int id);
	public void createProject(Project project) throws ValidationException, Exception;
	public void deleteProject(int id) throws Exception;	
	public Project updateProject(Project project) throws Exception;
	
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
