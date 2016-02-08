package uk.ac.ox.it.ords.api.project.services.impl;


import org.apache.shiro.SecurityUtils;

import uk.ac.ox.it.ords.api.project.model.Invitation;
import uk.ac.ox.it.ords.api.project.model.Project;
import uk.ac.ox.it.ords.api.project.services.ProjectInvitationService;
import uk.ac.ox.it.ords.api.project.services.ProjectRoleService;
import uk.ac.ox.it.ords.api.project.services.ProjectService;
import uk.ac.ox.it.ords.api.project.services.SendProjectInvitationEmailService;
import uk.ac.ox.it.ords.security.model.UserRole;

public abstract class AbstractProjectInvitationService implements
		ProjectInvitationService {

	/* (non-Javadoc)
	 * @see uk.ac.ox.it.ords.api.project.services.ProjectInvitationService#confirmInvitation(java.lang.String)
	 */
	@Override
	public void confirmInvitation(Invitation invitation) throws Exception {

		//
		// Create the desired role
		//
		UserRole userRole = new UserRole();
		
		//
		// If the user is logged in, use the principal name. If not, we'll use the
		// email address as the principal name.
		//
		if (SecurityUtils.getSubject() != null && SecurityUtils.getSubject().getPrincipal() != null){
			userRole.setPrincipalName(SecurityUtils.getSubject().getPrincipal().toString());
		} else {
			userRole.setPrincipalName(invitation.getEmail());
		}
		
		userRole.setRole(invitation.getRoleRequired());
		ProjectRoleService.Factory.getInstance().addUserRoleToProject(invitation.getProjectId(), userRole);
		
		//
		// Send out the acceptance email
		//
		Project project = ProjectService.Factory.getInstance().getProject(invitation.getProjectId());
		SendProjectInvitationEmailService.Factory.getInstance().sendProjectInvitationAcceptance(project, invitation);

		//
		// Remove the invitation
		//
		deleteInvitation(invitation);
	}

	@Override
	public boolean validate(Invitation invitation) {
		
		//
		// Must contain an email to send to
		//
		if (invitation.getEmail() == null || invitation.getEmail().trim().length() == 0){
			return false;
		}
		
		//
		// Must contain a sender
		//
		if (invitation.getSender() == null || invitation.getSender().trim().length() == 0){
			return false;
		}
		
		//
		// Must have a project
		//
		if (invitation.getProjectId() <= 0){
			return false;
		}
		
		//
		// Must have a valid required role
		//
		if (!ProjectRoleService.Factory.getInstance().isValidRole(invitation.getRoleRequired())){
			return false;
		}
		
		//
		// Invitation is valid
		//
		return true;
	}
	
	

}
