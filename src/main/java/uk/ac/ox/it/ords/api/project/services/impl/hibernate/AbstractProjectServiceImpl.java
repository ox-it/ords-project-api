package uk.ac.ox.it.ords.api.project.services.impl.hibernate;

import uk.ac.ox.it.ords.api.project.model.Project;
import uk.ac.ox.it.ords.api.project.services.ProjectService;

public abstract class AbstractProjectServiceImpl implements ProjectService{

	public AbstractProjectServiceImpl() {
	}
	
	/**
	 * Configures the project with internal system-set properties; this is called once on 
	 * project creation.
	 * @param project
	 */
	public void configureProject(Project project){
		// TODO use ServerDetailsService to populate
		project.setDbServerAddress("localhost");
	}
	
	/**
	 * Validates the project fields against the business rules for creating and updating projects
	 * @param project
	 * @throws Exception if any of the properties of the project violate a validation rule
	 */
	public void validateProject(Project project) throws Exception{
		if ((project.getName().contains(METADATA_TOKEN)) || (project.getDescription().contains(METADATA_TOKEN))) {
			//log.error("Invalid input - cannot have '___' in project");
			//Audit.createProjectFailed(project.getName(), userId);
			throw new Exception("Invalid input - cannot have '___' in project");
		}
	}

}
