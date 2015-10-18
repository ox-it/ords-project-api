package uk.ac.ox.it.ords.api.project.services;

import java.util.List;
import java.util.ServiceLoader;

import uk.ac.ox.it.ords.api.project.model.UserRole;
import uk.ac.ox.it.ords.api.project.services.impl.hibernate.ProjectRoleServiceImpl;

public interface ProjectRoleService {
	
	public void createInitialPermissions(int projectId) throws Exception;
	public void deletePermissions(int projectId) throws Exception;
	
	/**
	 * Return all the UserRoles that match the pattern of the project
	 * @param projectId
	 * @return
	 * @throws Exception
	 */
	public List<UserRole> getUserRolesForProject(int projectId) throws Exception;
	
	public UserRole getUserRole(int roleId) throws Exception;
	public UserRole addUserRoleToProject(int projectid, UserRole userRole) throws Exception;
	public void removeUserFromRoleInProject(int projectid, int roleId) throws Exception;
	

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
