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

import java.util.ArrayList;
import java.util.List;

import uk.ac.ox.it.ords.api.project.model.Member;
import uk.ac.ox.it.ords.api.project.server.ValidationException;
import uk.ac.ox.it.ords.api.project.services.ProjectRoleService;
import uk.ac.ox.it.ords.security.model.UserRole;

public abstract class AbstractProjectRoleService implements ProjectRoleService {

	/* (non-Javadoc)
	 * @see uk.ac.ox.it.ords.api.project.services.ProjectRoleService#getProjectMembers(int)
	 */
	@Override
	public List<Member> getProjectMembers(int projectId) throws Exception {
		List<UserRole> userRoles = getUserRolesForProject(projectId);
		ArrayList<Member> members = new ArrayList<Member>();
		for (UserRole userRole : userRoles){
			members.add(new Member(userRole));
		}
		return members;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.it.ords.api.project.services.ProjectRoleService#getPublicUserRole(java.lang.String)
	 */
	@Override
	public String getPublicUserRole(String role) {
		if (!role.contains("_")) return role;
		return role.split("_")[0];
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.it.ords.api.project.services.ProjectRoleService#getPrivateUserRole(java.lang.String, int)
	 */
	@Override
	public String getPrivateUserRole(String role, int projectId) {
		if (role.contains("_")) return role;
		return role+"_"+projectId;
	}

	/**
	 * Validates a user role
	 * @param userRole the user role to validate
	 * @return true if the role is valid, otherwise false
	 * @throws ValidationException if the role cannot be validated
	 */
	public boolean validate(UserRole userRole) throws ValidationException{
		if (userRole == null) throw new ValidationException("Invalid role");
		if (userRole.getPrincipalName() == null) throw new ValidationException("No user principal set for role");
		if (userRole.getRole() == null) throw new ValidationException("No role set");
		if (!isValidRole(userRole.getRole())) throw new ValidationException("Invalid role type");
		return true;
	}

	/**
	 * Validates a role matches the allowed role enum (e.g. Contributor, Viewer etc...)
	 * @param role the role to validate
	 * @return true if the role is valid
	 */
	public boolean isValidRole(String role){
		for (ProjectRole projectRole : ProjectRole.values()){
			if (projectRole.name().equals(role)) return true;
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.it.ords.api.project.services.ProjectRoleService#getProjectOwner(int)
	 */
	@Override
	public UserRole getProjectOwner(int projectId) throws Exception {
		List<UserRole> userRoles = getUserRolesForProject(projectId);
		for (UserRole userRole : userRoles){
			if (userRole.getRole().startsWith("owner_")) return userRole;
		}
		return null;
	}

}
