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

import org.apache.commons.configuration.XMLConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.it.ords.api.project.services.ProjectService;
import uk.ac.ox.it.ords.api.project.services.ServerConfigurationService;
import uk.ac.ox.it.ords.security.configuration.MetaConfiguration;

public class ServerConfigurationServiceImpl implements
ServerConfigurationService {

	public static Logger log = LoggerFactory.getLogger(ServerConfigurationServiceImpl.class);

	public static final String DEFAULT_SERVER_CONFIG_LOCATION = "serverConfig.xml";

	/**
	 * Check which server is suitable and return it.
	 * @param serverConfiguration the cconfiguration to check
	 * @return the name of the available server
	 * @throws Exception if no servers can be found
	 */
	protected String getAvailableDbServer(XMLConfiguration serverConfiguration)  throws Exception {

		//
		// Read the server list
		//
		
		int servers = serverConfiguration.getStringArray("serverList.server[@name]").length;

		//
		// Identify a suitable server and return its name
		//
		String server = null;

		for (int i = 0; i < servers; i++){
			String serverName = serverConfiguration.getString("serverList.server("+i+")[@name]");
			Object isMaintenance = serverConfiguration.getProperty("serverList.server("+i+")[@maintenance]");
			Integer maxProjects = serverConfiguration.getInteger("serverList.server("+i+")[@maxProjects]", null);
			String ip = serverConfiguration.getString("serverList.server("+i+")[@ip]");

			if (serverName.isEmpty()){
				serverName = ip;
			}

			//
			// Servers marked as undergoing maintenance are not used
			//
			if (isMaintenance == null){

				//
				// Check there is space; if there is no max, we can use 
				// this as the preferred server
				//
				if (maxProjects == null) {
					server = serverName;
					break;

				} else {

					//
					// Check space available on server
					//
					if (ProjectService.Factory.getInstance().getNumberOfProjectsOnServer(serverName) < maxProjects){
						server = serverName;
						break;
					}
				}
			}
		}

		if (server == null){
			log.error("No servers available for new project");
		}
		return server;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.it.ords.api.project.services.ServerConfigurationService#getAvailableDbServer()
	 */
	@Override
	public String getAvailableDbServer() throws Exception {

		String serverConfigurationLocation = DEFAULT_SERVER_CONFIG_LOCATION;

		try {
			serverConfigurationLocation = MetaConfiguration.getConfiguration().getString("ords.server.configuration");
			if (serverConfigurationLocation == null){
				log.warn("No server configuration location set; using defaults");
				serverConfigurationLocation = DEFAULT_SERVER_CONFIG_LOCATION;
			}
		} catch (Exception e) {
			log.warn("No server configuration location set; using defaults");
			serverConfigurationLocation = DEFAULT_SERVER_CONFIG_LOCATION;
		}

		//
		// Load the Server Configuration file
		//
		XMLConfiguration serverConfiguration  = new XMLConfiguration();
		try {
			serverConfiguration.setFileName(serverConfigurationLocation);
			serverConfiguration.load();
		} catch (Exception e1) {
			log.error("Cannot read server configuration at " + serverConfigurationLocation);
			throw new Exception("Cannot read server configuration");
		}

		return getAvailableDbServer(serverConfiguration);

	} 
}
