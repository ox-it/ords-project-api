package uk.ac.ox.it.ords.api.project.services;

import java.util.List;
import java.util.ServiceLoader;

import uk.ac.ox.it.ords.api.project.model.ProjectDatabase;
import uk.ac.ox.it.ords.api.project.services.impl.hibernate.ProjectDatabaseServiceImpl;

public interface ProjectDatabaseService {
	
	public ProjectDatabase getDatabaseForProject(int id) throws Exception;
	public List<ProjectDatabase> getDatabasesForProject(int id) throws Exception;
	public ProjectDatabase addDatabaseToProject(int projectId, ProjectDatabase db)  throws Exception;
	public void removeDatabaseFromProject(int projectId, int databaseId) throws Exception;
	
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
