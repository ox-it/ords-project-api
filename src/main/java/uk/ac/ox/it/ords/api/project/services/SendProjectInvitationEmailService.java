package uk.ac.ox.it.ords.api.project.services;

import java.util.ServiceLoader;

import uk.ac.ox.it.ords.api.project.model.Invitation;
import uk.ac.ox.it.ords.api.project.services.impl.SendMailTLS;

public interface SendProjectInvitationEmailService {

	public void sendProjectInvitation(Invitation invite);
	
	/**
	 * Factory for obtaining implementations
	 */
    public static class Factory {
		private static SendProjectInvitationEmailService provider;
	    public static SendProjectInvitationEmailService getInstance() {
	    	//
	    	// Use the service loader to load an implementation if one is available
	    	// Place a file called uk.ac.ox.oucs.ords.utilities.csv in src/main/resources/META-INF/services
	    	// containing the classname to load as the CsvService implementation. 
	    	// By default we load the Hibernate implementation.
	    	//
	    	if (provider == null){
	    		ServiceLoader<SendProjectInvitationEmailService> ldr = ServiceLoader.load(SendProjectInvitationEmailService.class);
	    		for (SendProjectInvitationEmailService service : ldr) {
	    			// We are only expecting one
	    			provider = service;
	    		}
	    	}
	    	//
	    	// If no service provider is found, use the default
	    	//
	    	if (provider == null){
	    		provider = new SendMailTLS();
	    	}
	    	
	    	return provider;
	    }
	}

}
