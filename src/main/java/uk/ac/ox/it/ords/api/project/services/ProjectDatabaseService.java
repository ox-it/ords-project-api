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

import uk.ac.ox.it.ords.api.project.model.Database;
import uk.ac.ox.it.ords.api.project.server.ValidationException;
import uk.ac.ox.it.ords.api.project.services.impl.hibernate.ProjectDatabaseServiceImpl;

public interface ProjectDatabaseService {
	
	public void validate(Database database) throws ValidationException;
	
	/**
	 * Get a logical Database by its ID
	 * @param id the id of the logical database
	 * @return the database, or null if no database exists with this id
	 * @throws Exception if there is a problem obtaining the database
	 */
	public Database getDatabase(int id) throws Exception;
	
	/**
	 * Get all the logical Databases for the specified project
	 * @param projectId the id of the project
	 * @return List of Database instances for this project
	 * @throws Exception if there is a problem obtanining the databases
	 */
	public List<Database> getDatabasesForProject(int projectId) throws Exception;
	
	/**
	 * Create a logical Database
	 * @param db the database to create
	 * @return the created database
	 * @throws Exception if there was a problem creating the database
	 */
	public Database addDatabase(Database db)  throws Exception;
	
	/**
	 * Update a logical Database
	 * @param db the database to update
	 * @return the updated database
	 * @throws Exception if there was a problem updating the database
	 */
	public Database updateDatabase(Database db)  throws Exception;
	
	/**
	 * Delete a logical Database
	 * @param databaseId the database to delete
	 * @throws Exception if there was a problem deleting the datbase
	 */
	public void removeDatabase(int databaseId) throws Exception;
	
	
	/**
	 * Enable ODBC for a database
	 * @param database the database to enable ODBC access for
	 * @throws Exception if there was a problem enabling ODBC access
	 */
	public void enableODBC(Database database) throws Exception;	
	
	/**
	 * Disable ODBC for a database
	 * @param database the database to disable ODBC access for
	 * @throws Exception if there was a problem disabling ODBC access
	 */
	public void disableODBC(Database database) throws Exception;	
	
	/**
	 * Enable ODBC for all databases in the specified project
	 * @param projectId the project to enable ODBC for
	 * @throws Exception if there was a problem enabling ODBC access
	 */
	public void enableODBC(int projectId) throws Exception;
	
	/**
	 * Disable ODBC connections for all databases in the specified project
	 * @param projectId the project to disable ODBC access for
	 * @throws Exception if there was a problem disabling ODBC access
	 */
	public void disableODBC(int projectId) throws Exception;
	
	/**
	 * Factory for obtaining implementations
	 */
    public static class Factory {
		private static ProjectDatabaseService provider;
	    public static ProjectDatabaseService getInstance() {
	    	//
	    	// Use the service loader to load an implementation if one is available
	    	// Place a file called uk.ac.ox.oucs.ords.utilities.csv in src/main/resources/META-INF/services
	    	// containing the classname to load as the CsvService implementation. 
	    	// By default we load the Hibernate implementation.
	    	//
	    	if (provider == null){
	    		ServiceLoader<ProjectDatabaseService> ldr = ServiceLoader.load(ProjectDatabaseService.class);
	    		for (ProjectDatabaseService service : ldr) {
	    			// We are only expecting one
	    			provider = service;
	    		}
	    	}
	    	//
	    	// If no service provider is found, use the default
	    	//
	    	if (provider == null){
	    		provider = new ProjectDatabaseServiceImpl();
	    	}
	    	
	    	return provider;
	    }
	}

}
