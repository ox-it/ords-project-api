package uk.ac.ox.it.ords.api.project.services;

import java.util.ServiceLoader;

import uk.ac.ox.it.ords.api.project.model.Project;
import uk.ac.ox.it.ords.api.project.services.impl.hibernate.ProjectServiceImpl;

public interface ProjectService {
	
	public Project getProject(int id);
	public void createProject(Project project) throws Exception;
	public void deleteProject(int id) throws Exception;
	
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
