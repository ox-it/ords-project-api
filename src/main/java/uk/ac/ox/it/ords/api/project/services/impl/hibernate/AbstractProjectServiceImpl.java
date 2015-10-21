package uk.ac.ox.it.ords.api.project.services.impl.hibernate;

import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.it.ords.api.project.model.Project;
import uk.ac.ox.it.ords.api.project.services.AuditService;
import uk.ac.ox.it.ords.api.project.services.ProjectService;

public abstract class AbstractProjectServiceImpl implements ProjectService{

	private static Logger log = LoggerFactory.getLogger(AbstractProjectServiceImpl.class);

	
	/**
	 * Configures the project with internal system-set properties; this is called once on 
	 * project creation.
	 * @param project
	 */
	public void configureProject(Project project){
		// TODO use ServerDetailsService to populate
		project.setDbServerAddress("localhost");
	}
	

	/* (non-Javadoc)
	 * @see uk.ac.ox.it.ords.api.project.services.ProjectService#validate(uk.ac.ox.it.ords.api.project.model.Project)
	 */
	@Override
	public void validate(Project project) throws Exception{
		if ((project.getName().contains(METADATA_TOKEN)) || (project.getDescription().contains(METADATA_TOKEN))) {
			log.error("Invalid input - cannot have '___' in project");
			AuditService.Factory.getInstance().createProjectFailed(project.getName());
			throw new Exception("Invalid input - cannot have '___' in project");
		}
	}

}
