package uk.ac.ox.it.ords.api.project.services;

import java.util.ServiceLoader;

import uk.ac.ox.it.ords.api.project.model.UserRole;
import uk.ac.ox.it.ords.api.project.services.impl.ipc.AuditServiceImpl;

public interface AuditService {
	
    /**
     * Create audit message that the user is not authorised to perform a specific action
     * @param request the action that is not authorised
     * @param projectIdString a string representation of the project id
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
     * @param message a message detailing the operation
     * @param projectId the id of the project
     */
    public void updateProjectUser(UserRole userRole, int projectId);
    
    /**
     * Create audit message that an users role within a project has been removed 
     * @param message a message detailing the operation
     * @param userId the id of the user who performed the action
     * @param projectId the id of the project
     */
    public void deleteProjectRole(UserRole userRole, int projectId);
    
    /**
     * Create audit message that an users role within a project has been created 
     * @param message a message detailing the operation
     * @param userId the id of the user who performed the action
     * @param projectId the id of the project
     */
    public void createProjectUser(UserRole userRole, int projectId);

	
	/**
	 * Factory for obtaining implementations
	 */
    public static class Factory {
		private static AuditService provider;
	    public static AuditService getInstance() {
	    	//
	    	// Use the service loader to load an implementation if one is available
	    	// Place a file called uk.ac.ox.oucs.ords.utilities.csv in src/main/resources/META-INF/services
	    	// containing the classname to load as the CsvService implementation. 
	    	// By default we load the Hibernate implementation.
	    	//
	    	if (provider == null){
	    		ServiceLoader<AuditService> ldr = ServiceLoader.load(AuditService.class);
	    		for (AuditService service : ldr) {
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
