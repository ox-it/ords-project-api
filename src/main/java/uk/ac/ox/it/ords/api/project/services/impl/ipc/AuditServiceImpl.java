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
package uk.ac.ox.it.ords.api.project.services.impl.ipc;

import org.apache.shiro.SecurityUtils;

import uk.ac.ox.it.ords.api.project.model.UserRole;
import uk.ac.ox.it.ords.api.project.services.AuditService;

/**
 * TODO Implements the Audit service interface. Calls Audit Service via REST using
 * HMAC authorization.
 */
public class AuditServiceImpl implements AuditService {
	
	public AuditServiceImpl() {
		// TODO Auto-generated constructor stub
	}
	
	private String getPrincipalName(){
		if (SecurityUtils.getSubject().getPrincipal() == null) return "Unauthenticated";
		return SecurityUtils.getSubject().getPrincipal().toString();
	}

	@Override
	public void deleteProject(String name, int projectId) {
		// TODO Auto-generated method stub
	}

	@Override
	public void createProject(String name, int projectId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void createProjectFailed(String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateProject(String name, int projectId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateProjectUser(UserRole userRole, int projectId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteProjectRole(UserRole userRole, int projectId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createProjectUser(UserRole userRole, int projectId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createNotAuthRecord(String request, int projectId) {
		// TODO Auto-generated method stub
		
	}

}
