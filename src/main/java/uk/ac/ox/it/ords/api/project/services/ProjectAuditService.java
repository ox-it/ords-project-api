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

import java.util.ServiceLoader;

import uk.ac.ox.it.ords.api.project.services.impl.ipc.AuditServiceImpl;
import uk.ac.ox.it.ords.security.model.UserRole;

public interface ProjectAuditService {
	
    /**
     * Create audit message that the user is not authorised to perform a specific action
     * @param request the action that is not authorised
     * @param projectId the project id
     */
    public void createNotAuthRecord(String request, int projectId);
	
    /**
     * Create audit message that a project has been created.
     * @param name Name of the project
     * @param projectId the id of the project
     */
    public void deleteProject(String name, int projectId);
    
    /**
     * Create audit message that a project has been deleted.
     * @param name Name of the project
     * @param projectId the id of the project
     */
    public void createProject(String name, int projectId);
	
    
    /**
     * Create audit message that a create project action was not successful.
     * @param name Name of the project
     */
    public void createProjectFailed(String name);
	
    
    /**
     * Create audit message that a project has been updated.
     * @param name Name of the project
     * @param projectId the id of the project
     */
    public void updateProject(String name, int projectId);
    
    /*
     * Project User functions
     */
    /**
     * Create audit message that an users role within a project has been updated 
     * @param userRole the user role
     * @param projectId the id of the project
     */
    public void updateProjectUser(UserRole userRole, int projectId);
    
    /**
     * Create audit message that an users role within a project has been removed 
     * @param userRole the role that was removed
     * @param projectId the id of the project
     */
    public void deleteProjectRole(UserRole userRole, int projectId);
    
    /**
     * Create audit message that an users role within a project has been created 
     * @param userRole the role that was created
     * @param projectId the id of the project
     */
    public void createProjectUser(UserRole userRole, int projectId);

	
	/**
	 * Factory for obtaining implementations
	 */
    public static class Factory {
		private static ProjectAuditService provider;
	    public static ProjectAuditService getInstance() {
	    	//
	    	// Use the service loader to load an implementation if one is available
	    	// Place a file called uk.ac.ox.oucs.ords.utilities.csv in src/main/resources/META-INF/services
	    	// containing the classname to load as the CsvService implementation. 
	    	// By default we load the Hibernate implementation.
	    	//
	    	if (provider == null){
	    		ServiceLoader<ProjectAuditService> ldr = ServiceLoader.load(ProjectAuditService.class);
	    		for (ProjectAuditService service : ldr) {
	    			// We are only expecting one
	    			provider = service;
	    		}
	    	}
	    	//
	    	// If no service provider is found, use the default
	    	//
	    	if (provider == null){
	    		provider = new AuditServiceImpl();
	    	}
	    	
	    	return provider;
	    }
	}


}
