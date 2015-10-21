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
package uk.ac.ox.it.ords.api.project.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.it.ords.api.project.model.Project;
import uk.ac.ox.it.ords.api.project.server.ValidationException;
import uk.ac.ox.it.ords.api.project.services.AuditService;
import uk.ac.ox.it.ords.api.project.services.ProjectService;

public abstract class AbstractProjectServiceImpl implements ProjectService{

	private static Logger log = LoggerFactory.getLogger(AbstractProjectServiceImpl.class);

	
	/**
	 * Configures the project with internal system-set properties; this is called once on 
	 * project creation.
	 * TODO use ServerDetailsService to populate
	 * @param project
	 */
	public void configureProject(Project project){
		
		project.setDbServerAddress("localhost");
	}
	

	/* (non-Javadoc)
	 * @see uk.ac.ox.it.ords.api.project.services.ProjectService#validate(uk.ac.ox.it.ords.api.project.model.Project)
	 */
	@Override
	public void validate(Project project) throws ValidationException{
		
		if (project.getName() == null){
			log.error("Invalid input - cannot have no project name");
			AuditService.Factory.getInstance().createProjectFailed("Null project name");
			throw new ValidationException("Invalid input - cannot have no project name");	
		}
		
		if (project.getDescription() == null){
			log.error("Invalid input - cannot have no project description");
			AuditService.Factory.getInstance().createProjectFailed(project.getName());
			throw new ValidationException("Invalid input - cannot have no project description");
		}
		
		if ((project.getName().contains(METADATA_TOKEN)) || (project.getDescription().contains(METADATA_TOKEN))) {
			log.error("Invalid input - cannot have '___' in project");
			AuditService.Factory.getInstance().createProjectFailed(project.getName());
			throw new ValidationException("Invalid input - cannot have '___' in project");
		}
	}

}
