package uk.ac.ox.it.ords.api.project.services.impl.hibernate;

import uk.ac.ox.it.ords.api.project.model.Project;

public abstract class AbstractProjectServiceImpl {

	public AbstractProjectServiceImpl() {
	}
	
	public Project configureProject(Project project){
		project.setDbServerAddress("localhost");
		return project;
	}

}
