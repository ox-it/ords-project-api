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

import uk.ac.ox.it.ords.api.project.model.UserRole;
import uk.ac.ox.it.ords.api.project.server.ValidationException;
import uk.ac.ox.it.ords.api.project.services.ProjectRoleService;

public abstract class AbstractProjectRoleService implements ProjectRoleService {
	
	public boolean validate(UserRole userRole) throws ValidationException{
		if (userRole == null) throw new ValidationException("Invalid role");
		if (userRole.getPrincipalName() == null) throw new ValidationException("No user principal set for role");
		if (userRole.getRole() == null) throw new ValidationException("No role set");
		if (!isValidRole(userRole.getRole())) throw new ValidationException("Invalid role type");
		return true;
	}

	private boolean isValidRole(String role){
		for (ProjectRole projectRole : ProjectRole.values()){
			if (projectRole.name().equals(role)) return true;
		}
		return false;
	}

}
