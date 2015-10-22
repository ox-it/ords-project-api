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

import uk.ac.ox.it.ords.api.project.services.impl.hibernate.ProjectRoleServiceImpl;
import uk.ac.ox.it.ords.security.model.UserRole;

public interface ProjectRoleService {
	
	/**
	 * Create the Owner role and their permissions; called once when a new project is created
	 * @param projectId
	 * @throws Exception
	 */
	public void createInitialPermissions(int projectId) throws Exception;
	
	/**
	 * Delete all the permissions and roles associated with a project; called once when a project is deleted
	 * @param projectId
	 * @throws Exception
	 */
	public void deletePermissions(int projectId) throws Exception;
	
	/**
	 * Return all the UserRoles that match the pattern of the project
	 * @param projectId
	 * @return a List of UserRole objects
	 * @throws Exception
	 */
	public List<UserRole> getUserRolesForProject(int projectId) throws Exception;
	
	/**
	 * Return the specified UserRole instance
	 * @param roleId 
	 * @return the UserRole specified, or null if there is no match
	 * @throws Exception
	 */
	public UserRole getUserRole(int roleId) throws Exception;
	
	/**
	 * Create the UserRole 
	 * @param projectid
	 * @param userRole
	 * @return the UserRole that has been persisted
	 * @throws Exception
	 */
	public UserRole addUserRoleToProject(int projectid, UserRole userRole) throws Exception;
	
	/**
	 * Remove the UserRole
	 * @param projectid
	 * @param roleId
	 * @throws Exception
	 */
	public void removeUserFromRoleInProject(int projectid, int roleId) throws Exception;	

	/**
	 * The enumeration of valid UserRole types
	 */
    public enum ProjectRole {
        owner, projectadministrator, contributor, viewer, deleted
    };
	
	/**
	 * Factory for obtaining implementations
	 */
    public static class Factory {
		private static ProjectRoleService provider;
	    public static ProjectRoleService getInstance() {
	    	//
	    	// Use the service loader to load an implementation if one is available
	    	// Place a file called uk.ac.ox.oucs.ords.utilities.csv in src/main/resources/META-INF/services
	    	// containing the classname to load as the CsvService implementation. 
	    	// By default we load the Hibernate implementation.
	    	//
	    	if (provider == null){
	    		ServiceLoader<ProjectRoleService> ldr = ServiceLoader.load(ProjectRoleService.class);
	    		for (ProjectRoleService service : ldr) {
	    			// We are only expecting one
	    			provider = service;
	    		}
	    	}
	    	//
	    	// If no service provider is found, use the default
	    	//
	    	if (provider == null){
	    		provider = new ProjectRoleServiceImpl();
	    	}
	    	
	    	return provider;
	    }
	}

}
