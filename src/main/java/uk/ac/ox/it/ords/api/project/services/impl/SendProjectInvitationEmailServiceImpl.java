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

import java.util.Properties;

import org.apache.commons.configuration.ConfigurationConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.it.ords.api.project.model.Invitation;
import uk.ac.ox.it.ords.api.project.model.Project;
import uk.ac.ox.it.ords.api.project.services.SendProjectInvitationEmailService;
import uk.ac.ox.it.ords.security.configuration.MetaConfiguration;

public class SendProjectInvitationEmailServiceImpl extends SendMailTLS
		implements SendProjectInvitationEmailService {
	
	private Logger log = LoggerFactory.getLogger(SendProjectInvitationEmailServiceImpl.class);
	
	public static final String ORDS_MAIL_INVITATION_SUBJECT = "Message from ORDS";
	public static final String ORDS_MAIL_INVITATION_MESSAGE = "Hi!\n\n%s has suggested you join their ORDS project, but you have not yet registered with the ORDS. Please click the following link to register with the ORDS and join their project.\n\n%s\n\nIf you believe this email has been sent to you in error then please contact ORDS support.\n\nThe ORDS Team";

	public static final String ORDS_MAIL_INVITATION_ADDRESS = "http://localhost/app/#/invite/%s";

	public static final String ORDS_MAIL_ACCEPTED_SUBJECT = "Message from ORDS";
	public static final String ORDS_MAIL_ACCEPTED_MESSAGE = "The user with email address %s you invited to your project (%s) has now joined ORDS and is a member of your project with role %s";


	
	public SendProjectInvitationEmailServiceImpl(){
		props = ConfigurationConverter.getProperties(MetaConfiguration.getConfiguration());	
	}
	
	/**
	 * Construct the service using the given properties; used for testing
	 * @param props
	 */
	public SendProjectInvitationEmailServiceImpl(Properties props){
		this.props = props;
	}
	
	@Override
	public void sendProjectInvitation(Invitation invite) throws Exception {
		
		String subject = null;
		if (props.containsKey("ords.mail.invitation.subject")){
			subject = props.getProperty("ords.mail.invitation.subject");			
		}
		if (subject == null || subject.isEmpty()){
			subject = ORDS_MAIL_INVITATION_SUBJECT;
		}
		
		String messageText = createInvitationMessage(invite);
		sendMail(subject, messageText);	
	}
	
	@Override
	public void sendProjectInvitationAcceptance(Project project, Invitation invite)
			throws Exception {
		String subject = null;
		if (props.containsKey("ords.mail.accepted.subject")){
			subject = props.getProperty("ords.mail.accepted.subject");			
		}
		if (subject == null || subject.isEmpty()){
			subject = ORDS_MAIL_ACCEPTED_SUBJECT;
		}
		
		String messageText = createAcceptedMessage(project, invite);
		sendMail(subject, messageText);	
		
	}

	/**
	 * Generate the invitation URL the user should use to click through.
	 * @param user
	 * @return
	 */
	public String getInvitationUrl(Invitation invite){
		
		String link = null;
		if (props.containsKey("ords.mail.invitation.address")){
			link = props.getProperty("ords.mail.invitation.address");
		}
		
		if (link == null || link.isEmpty()){
			link = ORDS_MAIL_INVITATION_ADDRESS;
		}
		
		link = String.format(link, invite.getUuid());
		return link;
	}

	/**
	 * Generates the message body
	 * @param invite
	 * @return
	 */
	public String createAcceptedMessage(Project project, Invitation invite){
		
		String messageText = null;
		
		if (props.containsKey("ords.mail.accepted.message")){
			messageText = props.getProperty("ords.mail.accepted.message");
		}
		
		if (messageText == null || messageText.isEmpty()){
			messageText = ORDS_MAIL_ACCEPTED_MESSAGE;
		}
	
		messageText = String.format(messageText, invite.getEmail(), project.getName(), invite.getRoleRequired());
		
		email = invite.getEmail();
		if (log.isDebugEnabled()) {
			log.debug("The email I want to send is:" + messageText);
		}
		return messageText;
	}
	
	/**
	 * Generates the message body
	 * @param invite
	 * @return
	 */
	public String createInvitationMessage(Invitation invite){
		
		String messageText = null;
		
		if (props.containsKey("ords.mail.invitation.message")){
			messageText = props.getProperty("ords.mail.invitation.message");
		}
		
		if (messageText == null || messageText.isEmpty()){
			messageText = ORDS_MAIL_INVITATION_MESSAGE;
		}
				
		messageText = String.format(messageText, invite.getSender(), getInvitationUrl(invite));
		
		email = invite.getEmail();
		if (log.isDebugEnabled()) {
			log.debug("The email I want to send is:" + messageText);
		}
		return messageText;
	}


}
