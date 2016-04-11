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
import java.util.List;

import uk.ac.ox.it.ords.api.project.model.DatabaseVersion;
import uk.ac.ox.it.ords.api.project.services.impl.hibernate.DatabaseVersionServiceImpl;

public interface DatabaseVersionService {
	
	/**
	 * Obtain the database versions associated with a logical Database
	 * @param logicalDatabaseId the ID of the logical database
	 * @return List of DatabaseVersion instances for this logical database
	 */
	public List<DatabaseVersion> getDatabaseVersions(int logicalDatabaseId);
	
	/**
	 * Factory for obtaining implementations
	 */
    public static class Factory {
		private static DatabaseVersionService provider;
	    public static DatabaseVersionService getInstance() {
	    	//
	    	// Use the service loader to load an implementation if one is available
	    	// Place a file called uk.ac.ox.oucs.ords.utilities.csv in src/main/resources/META-INF/services
	    	// containing the classname to load as the CsvService implementation. 
	    	// By default we load the Hibernate implementation.
	    	//
	    	if (provider == null){
	    		ServiceLoader<DatabaseVersionService> ldr = ServiceLoader.load(DatabaseVersionService.class);
	    		for (DatabaseVersionService service : ldr) {
	    			// We are only expecting one
	    			provider = service;
	    		}
	    	}
	    	//
	    	// If no service provider is found, use the default
	    	//
	    	if (provider == null){
	    		provider = new DatabaseVersionServiceImpl();
	    	}
	    	
	    	return provider;
	    }
	}

}
