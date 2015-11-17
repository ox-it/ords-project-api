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

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import org.junit.Test;

import uk.ac.ox.it.ords.api.project.model.Invitation;
import uk.ac.ox.it.ords.api.project.services.impl.SendMailTLS;

public class SendMailTLSTest extends SendMailTLS{

	
	@Test
	public void testLinkCreate(){
		Invitation invite = new Invitation();
		invite.setUuid("999111");
		String link = this.getVerificationUrl(invite);
		assertEquals("http://localhost/app/#/invite/999111", link);		
	}
	
	@Test
	public void testMessageCreate(){
		Invitation invite = new Invitation();
		invite.setUuid("999111");
		invite.setSender("Bob");
		String messageText = this.createVerificationMessage(invite);
		assertEquals("Hi!\n\nBob has suggested you join their ORDS project, but you have not yet registered with the ORDS. Please click the following link to register with the ORDS and join their project.\n\nhttp://localhost/app/#/invite/999111\n\nIf you believe this email has been sent to you in error then please contact ORDS support.\n\nThe ORDS Team", messageText);		
	}
	
	@Test
	public void testMessageSend() throws MessagingException{
		Invitation invite = new Invitation();
		invite.setUuid("999111");
		invite.setSender("Bob");
		invite.setEmail("pingo@mailinator.com");		
		sendProjectInvitation(invite);
		assertEquals("daemons@sysdev.oucs.ox.ac.uk", ((InternetAddress) message.getFrom()[0]).getAddress());
		assertEquals("pingo@mailinator.com", ((InternetAddress) message.getAllRecipients()[0]).getAddress());

	}
}
