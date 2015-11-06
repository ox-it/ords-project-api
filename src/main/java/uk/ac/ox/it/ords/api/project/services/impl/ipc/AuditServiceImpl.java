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
import org.apache.shiro.UnavailableSecurityManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.it.ords.api.project.services.ProjectAuditService;
import uk.ac.ox.it.ords.security.model.Audit;
import uk.ac.ox.it.ords.security.model.UserRole;
import uk.ac.ox.it.ords.security.services.AuditService;

/**
 * Implements the Audit service interface.
 * At the moment this uses the Common Security package (jar dependency) rather than
 * an IPC metho
 */
public class AuditServiceImpl implements ProjectAuditService {
	
	private Logger log = LoggerFactory.getLogger(AuditServiceImpl.class);
	
	public AuditServiceImpl() {
	}
	
	private String getPrincipalName(){
		try {
			if (SecurityUtils.getSubject() == null || SecurityUtils.getSubject().getPrincipal() == null) return "Unauthenticated";
			return SecurityUtils.getSubject().getPrincipal().toString();
		} catch (UnavailableSecurityManagerException e) {
			log.warn("Audit being called with no valid security context. This is probably caused by being called from unit tests");
			return "Security Manager Not Configured";
		}
	}
	
	@Override
	public void deleteProject(String name, int projectId) {
		Audit audit= new Audit();
		audit.setAuditType(Audit.AuditType.DELETE_PROJECT.name());
		audit.setProjectId(projectId);
		audit.setUserId(getPrincipalName());
		AuditService.Factory.getInstance().createNewAudit(audit);
	}

	@Override
	public void createProject(String name, int projectId) {
		Audit audit= new Audit();
		audit.setAuditType(Audit.AuditType.CREATE_PROJECT.name());
		audit.setProjectId(projectId);
		audit.setUserId(getPrincipalName());
		AuditService.Factory.getInstance().createNewAudit(audit);
	}

	@Override
	public void createProjectFailed(String name) {
		Audit audit= new Audit();
		audit.setAuditType(Audit.AuditType.CREATE_PROJECT_FAILED.name());
		audit.setUserId(getPrincipalName());
		AuditService.Factory.getInstance().createNewAudit(audit);
	}

	@Override
	public void updateProject(String name, int projectId) {
		Audit audit= new Audit();
		audit.setAuditType(Audit.AuditType.UPDATE_PROJECT.name());
		audit.setProjectId(projectId);
		audit.setUserId(getPrincipalName());
		AuditService.Factory.getInstance().createNewAudit(audit);

	}

	@Override
	public void updateProjectUser(UserRole userRole, int projectId) {
		Audit audit= new Audit();
		audit.setAuditType(Audit.AuditType.UPDATE_PROJECT_USER.name());
		audit.setProjectId(projectId);
		audit.setUserId(getPrincipalName());
		audit.setMessage("User role for " + userRole.getPrincipalName() + " updated");
		AuditService.Factory.getInstance().createNewAudit(audit);
		
	}

	@Override
	public void deleteProjectRole(UserRole userRole, int projectId) {
		Audit audit= new Audit();
		audit.setAuditType(Audit.AuditType.DELETE_PROJECT_USER.name());
		audit.setProjectId(projectId);
		audit.setUserId(getPrincipalName());
		audit.setMessage("User role for " + userRole.getPrincipalName() + " deleted");
		AuditService.Factory.getInstance().createNewAudit(audit);
	}

	@Override
	public void createProjectUser(UserRole userRole, int projectId) {
		Audit audit= new Audit();
		audit.setAuditType(Audit.AuditType.CREATE_PROJECT_USER.name());
		audit.setProjectId(projectId);
		audit.setUserId(getPrincipalName());
		audit.setMessage("User role for " + userRole.getPrincipalName() + " created");
		AuditService.Factory.getInstance().createNewAudit(audit);
	}

	@Override
	public void createNotAuthRecord(String request, int projectId) {
		Audit audit= new Audit();
		audit.setAuditType(Audit.AuditType.GENERIC_NOTAUTH.name());
		audit.setUserId(getPrincipalName());
		audit.setMessage(request);
		audit.setProjectId(projectId);
		AuditService.Factory.getInstance().createNewAudit(audit);
	}

}
