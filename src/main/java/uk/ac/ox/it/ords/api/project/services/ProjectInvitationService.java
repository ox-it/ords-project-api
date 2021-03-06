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

import java.util.List;
import java.util.ServiceLoader;

import uk.ac.ox.it.ords.api.project.model.Invitation;
import uk.ac.ox.it.ords.api.project.services.impl.hibernate.ProjectInvitationServiceImpl;

public interface ProjectInvitationService {

	public List<Invitation> getInvitations(int projectId) throws Exception;
	public Invitation getInvitation(int invitationId) throws Exception;
	public Invitation getInvitationByInviteCode(String code) throws Exception;
	public void deleteInvitation(Invitation invitation) throws Exception;
	public Invitation createInvitation(Invitation invitation) throws Exception;
	public void confirmInvitation(Invitation invitation) throws Exception;
	public void updateInvitation(Invitation invitation) throws Exception;

	public boolean validate(Invitation invitation);
	
	/**
	 * Factory for obtaining implementations
	 */
    public static class Factory {
		private static ProjectInvitationService provider;
	    public static ProjectInvitationService getInstance() {
	    	//
	    	// Use the service loader to load an implementation if one is available
	    	// Place a file called uk.ac.ox.oucs.ords.utilities.csv in src/main/resources/META-INF/services
	    	// containing the classname to load as the CsvService implementation. 
	    	// By default we load the Hibernate implementation.
	    	//
	    	if (provider == null){
	    		ServiceLoader<ProjectInvitationService> ldr = ServiceLoader.load(ProjectInvitationService.class);
	    		for (ProjectInvitationService service : ldr) {
	    			// We are only expecting one
	    			provider = service;
	    		}
	    	}
	    	//
	    	// If no service provider is found, use the default
	    	//
	    	if (provider == null){
	    		provider = new ProjectInvitationServiceImpl();
	    	}
	    	
	    	return provider;
	    }
	}
}
