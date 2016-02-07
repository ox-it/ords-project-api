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

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Properties;

import javax.mail.Message;

import org.junit.Test;
import org.jvnet.mock_javamail.Mailbox;

import uk.ac.ox.it.ords.api.project.model.Invitation;
import uk.ac.ox.it.ords.api.project.model.Project;
import uk.ac.ox.it.ords.api.project.services.impl.SendProjectInvitationEmailServiceImpl;

public class SendProjectInvitationEmailServiceTest{

	
	@Test
	public void testLinkCreate(){
		Invitation invite = new Invitation();
		invite.setUuid("999111");
		
		SendProjectInvitationEmailServiceImpl service = new SendProjectInvitationEmailServiceImpl();
		
		String link = service.getInvitationUrl(invite);
		assertEquals("http://localhost/app/#/invite/999111", link);		
	}
	
	@Test
	public void testMessageCreate(){
		Invitation invite = new Invitation();
		invite.setUuid("999111");
		invite.setSender("Bob");
		
		SendProjectInvitationEmailServiceImpl service = new SendProjectInvitationEmailServiceImpl();

		String messageText = service.createInvitationMessage(invite);
		assertEquals("Hi!\n\nBob has suggested you join their ORDS project, but you have not yet registered with the ORDS. Please click the following link to register with the ORDS and join their project.\n\nhttp://localhost/app/#/invite/999111\n\nIf you believe this email has been sent to you in error then please contact ORDS support.\n\nThe ORDS Team", messageText);		
	}
	
	@Test
	public void sendMailTest() throws Exception {

		Mailbox.clearAll();

		String subject = "Message from ORDS";
		String body = "Hi!\n\nBob has suggested you join their ORDS project, but you have not yet registered with the ORDS. Please click the following link to register with the ORDS and join their project.\n\nhttp://localhost/app/#/invite/999111\n\nIf you believe this email has been sent to you in error then please contact ORDS support.\n\nThe ORDS Team";

		Properties properties = new Properties();
		properties.setProperty("ords.mail.send", "true");				
		properties.setProperty("mail.smtp.host", "test.ords.ox.ac.uk");
		properties.setProperty("mail.smtp.from", "test@test.ords.ox.ac.uk");
		properties.setProperty("mail.smtp.username", "test");
		properties.setProperty("mail.smtp.password", "test");
		
		Invitation invite = new Invitation();
		invite.setUuid("999111");
		invite.setSender("Bob");
		invite.setEmail("scott@test.ords.ox.ac.uk");	
		
	    new SendProjectInvitationEmailServiceImpl(properties).sendProjectInvitation(invite);

		List<Message> inbox = Mailbox.get("scott@test.ords.ox.ac.uk");
				
		assertEquals(1, inbox.size());  
		assertEquals(subject, inbox.get(0).getSubject());
		assertEquals(body, inbox.get(0).getContent());
	}
	
	@Test(expected = Exception.class)
	public void sendMailNoSettingsTest() throws Exception {

		Mailbox.clearAll();

		Properties properties = new Properties();
		properties.setProperty("ords.mail.send", "true");				
		
		Invitation invite = new Invitation();
		invite.setUuid("999111");
		invite.setSender("Bob");
		invite.setEmail("scott@test.ords.ox.ac.uk");
		
	    new SendProjectInvitationEmailServiceImpl(properties).sendProjectInvitation(invite);

	}
	
	@Test
	public void sendAcceptedMailTest() throws Exception {

		Mailbox.clearAll();

		String subject = "Message from ORDS";
		String body = "The user with email address scott@test.ords.ox.ac.uk you invited to your project (Test Project) has now joined ORDS and is a member of your project with role Viewer";
		Properties properties = new Properties();
		properties.setProperty("ords.mail.send", "true");				
		properties.setProperty("mail.smtp.host", "test.ords.ox.ac.uk");
		properties.setProperty("mail.smtp.from", "test@test.ords.ox.ac.uk");
		properties.setProperty("mail.smtp.username", "test");
		properties.setProperty("mail.smtp.password", "test");
		
		Project project = new Project();
		project.setName("Test Project");
		project.setDescription("Test Project");
				
		Invitation invite = new Invitation();
		invite.setUuid("999111");
		invite.setSender("Bob");
		invite.setEmail("scott@test.ords.ox.ac.uk");
		invite.setRoleRequired("Viewer");
		invite.setProjectId(project.getProjectId());
		
	    new SendProjectInvitationEmailServiceImpl(properties).sendProjectInvitationAcceptance(project, invite);

		List<Message> inbox = Mailbox.get("scott@test.ords.ox.ac.uk");
				
		assertEquals(1, inbox.size());  
		assertEquals(subject, inbox.get(0).getSubject());
		assertEquals(body, inbox.get(0).getContent());
		
	}
}
