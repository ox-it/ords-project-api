package uk.ac.ox.it.ords.api.project.services.impl;


import uk.ac.ox.it.ords.api.project.model.Invitation;
import uk.ac.ox.it.ords.api.project.services.ProjectInvitationService;
import uk.ac.ox.it.ords.api.project.services.ProjectRoleService;
import uk.ac.ox.it.ords.security.model.UserRole;

public abstract class AbstractProjectInvitationService implements
		ProjectInvitationService {

	/* (non-Javadoc)
	 * @see uk.ac.ox.it.ords.api.project.services.ProjectInvitationService#confirmInvitation(java.lang.String)
	 */
	@Override
	public void confirmInvitation(String code) throws Exception {
		Invitation invitation = getInvitationByInviteCode(code);
		if (invitation == null) throw new Exception("invalid invitation code");

		//
		// Create the desired role
		//
		UserRole userRole = new UserRole();
		userRole.setPrincipalName(invitation.getPrincipalName());
		userRole.setRole(invitation.getRoleRequired());
		ProjectRoleService.Factory.getInstance().addUserRoleToProject(invitation.getProjectId(), userRole);

		//
		// Remove the invitation
		//
		deleteInvitation(invitation);
	}

}
