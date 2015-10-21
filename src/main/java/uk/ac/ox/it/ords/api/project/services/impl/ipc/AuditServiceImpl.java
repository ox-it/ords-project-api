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
