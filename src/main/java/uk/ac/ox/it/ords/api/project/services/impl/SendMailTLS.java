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

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import uk.ac.ox.it.ords.api.project.model.Invitation;
import uk.ac.ox.it.ords.api.project.services.SendProjectInvitationEmailService;
import uk.ac.ox.it.ords.security.configuration.MetaConfiguration;

/**
 *
 * @author dave
 */
public class SendMailTLS implements SendProjectInvitationEmailService {

	private Logger log = LoggerFactory.getLogger(SendMailTLS.class);
	private Properties props;
	private String email;
	protected Message message;

	public SendMailTLS() {
		props = ConfigurationConverter.getProperties(MetaConfiguration.getConfiguration());
	}
	
	@Override
	public void sendProjectInvitation(Invitation invite) {
		String messageText = createVerificationMessage(invite);
		sendMail(messageText);	
	}
	
	/**
	 * Generate the verification URL the user should use to click through.
	 * @param user
	 * @return
	 */
	protected String getVerificationUrl(Invitation invite){
		String link = String.format(props.getProperty("ords.mail.invitation.address"), invite.getUuid());
		return link;
	}
	
	protected String createVerificationMessage(Invitation invite){
		String messageText = String.format(props.getProperty("ords.mail.invitation.message"), invite.getSender(), getVerificationUrl(invite));
		email = invite.getEmail();
		if (log.isDebugEnabled()) {
			log.debug("The email I want to send is:" + messageText);
		}
		return messageText;
	}

	protected void sendMail(String messageText) {
		if (props.get("mail.smtp.username") == null) {
			log.error("Unable to send emails due to null user");
			return;
		}
		
		Session session = Session.getInstance(props,
				new javax.mail.Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(props.get("mail.smtp.username").toString(), props.get("mail.smtp.password").toString());
			}
		});

		try {
			message = new MimeMessage(session);
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(email));
			message.setSubject(props.getProperty("ords.mail.invitation.subject"));
			message.setText(messageText);
			message.setFrom(new InternetAddress(props.getProperty("mail.smtp.from")));

			//
			// This is just to help with testing
			//
			if ( MetaConfiguration.getConfiguration().getBoolean("ords.mail.send")){
				Transport.send(message);
			}

			if (log.isDebugEnabled()) {
				log.debug(String.format("Sent email to %s", email));
				log.debug("with content: " + messageText);
			}
		}
		catch (MessagingException e) {
			log.error("Unable to send email to " + email, e);
		}
	}


}
