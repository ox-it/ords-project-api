package uk.ac.ox.it.ords.api.project.services.impl;

import uk.ac.ox.it.ords.api.project.services.ProjectRoleService;

public abstract class AbstractProjectRoleService implements ProjectRoleService {

	public static boolean isValidRole(String role){
		for (ProjectRole projectRole : ProjectRole.values()){
			if (projectRole.name().equals(role)) return true;
		}
		return false;
	}

}
