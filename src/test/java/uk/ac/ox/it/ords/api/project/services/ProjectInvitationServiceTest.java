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
package uk.ac.ox.it.ords.api.project.services;

import static org.junit.Assert.*;

import org.junit.Test;

import uk.ac.ox.it.ords.api.project.model.Invitation;

public class ProjectInvitationServiceTest {

	
	@Test
	public void inviteNoEmail(){
		Invitation invitation = new Invitation();
		invitation.setProjectId(99);
		invitation.setSender("Bob");
		invitation.setRoleRequired("viewer");;
		assertFalse(ProjectInvitationService.Factory.getInstance().validate(invitation));
		invitation.setEmail("");
		assertFalse(ProjectInvitationService.Factory.getInstance().validate(invitation));
		invitation.setEmail(" ");
		assertFalse(ProjectInvitationService.Factory.getInstance().validate(invitation));
	}
	
	@Test
	public void inviteNoSender(){
		Invitation invitation = new Invitation();
		invitation.setEmail("pinga@mailinator.com");
		invitation.setProjectId(99);
		invitation.setRoleRequired("viewer");
		assertFalse(ProjectInvitationService.Factory.getInstance().validate(invitation));
		invitation.setSender("");
		assertFalse(ProjectInvitationService.Factory.getInstance().validate(invitation));
		invitation.setSender(" ");
		assertFalse(ProjectInvitationService.Factory.getInstance().validate(invitation));
	}
	
	
	@Test
	public void inviteNoRole(){
		Invitation invitation = new Invitation();
		invitation.setEmail("pinga@mailinator.com");
		invitation.setProjectId(99);
		invitation.setSender("Bob");
		assertFalse(ProjectInvitationService.Factory.getInstance().validate(invitation));
	}
	
	@Test
	public void inviteInvalidRole(){
		Invitation invitation = new Invitation();
		invitation.setEmail("pinga@mailinator.com");
		invitation.setProjectId(99);
		invitation.setSender("Bob");
		invitation.setRoleRequired("ninja");;
		assertFalse(ProjectInvitationService.Factory.getInstance().validate(invitation));
	}
	
	@Test
	public void inviteNoProject(){
		Invitation invitation = new Invitation();
		invitation.setEmail("pinga@mailinator.com");
		invitation.setSender("Bob");
		invitation.setRoleRequired("viewer");;
		assertFalse(ProjectInvitationService.Factory.getInstance().validate(invitation));
	}
}
