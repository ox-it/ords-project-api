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

import java.io.StringReader;

import org.apache.commons.configuration.XMLConfiguration;
import org.junit.Test;

import uk.ac.ox.it.ords.api.project.services.impl.ServerConfigurationServiceImpl;

public class ServerConfigurationServiceImplTest extends ServerConfigurationServiceImpl{
	
	@Test
	public void getDefaultServerList() throws Exception{
		ServerConfigurationService service = ServerConfigurationService.Factory.getInstance();
		assertEquals("localhost", service.getAvailableDbServer());
	}
	
	@Test
	public void testEmptyConfig() throws Exception{
		String config = "<config></config>";
		XMLConfiguration serverConfiguration = new XMLConfiguration();
		serverConfiguration.load(new StringReader(config));
		assertNull(getAvailableDbServer(serverConfiguration));
	}
	
	@Test
	public void testMaxProject() throws Exception{
		String config = "<ordsServerConfig><serverList><server name=\"localhost\" maxProjects = \"10000\" /></serverList></ordsServerConfig>";
		XMLConfiguration serverConfiguration = new XMLConfiguration();
		serverConfiguration.load(new StringReader(config));
		assertEquals("localhost", getAvailableDbServer(serverConfiguration));
	}
	
	@Test
	public void testMaxProject2() throws Exception{
		String config = "<ordsServerConfig><serverList><server name=\"localhost\" maxProjects = \"0\" /></serverList></ordsServerConfig>";
		XMLConfiguration serverConfiguration = new XMLConfiguration();
		serverConfiguration.load(new StringReader(config));
		assertNull(getAvailableDbServer(serverConfiguration));
	}

	@Test
	public void testMaxProjects3() throws Exception{
		String config = "<ordsServerConfig><serverList><server name=\"server1\" maxProjects = \"0\"  /><server name=\"server2\" /></serverList></ordsServerConfig>";
		XMLConfiguration serverConfiguration = new XMLConfiguration();
		serverConfiguration.load(new StringReader(config));
		assertEquals("server2", getAvailableDbServer(serverConfiguration));
	}
	
	@Test
	public void testServerMaintenance() throws Exception{
		String config = "<ordsServerConfig><serverList><server name=\"server1\" maintenance=\"true\" /><server name=\"server2\" /></serverList></ordsServerConfig>";
		XMLConfiguration serverConfiguration = new XMLConfiguration();
		serverConfiguration.load(new StringReader(config));
		assertEquals("server2", getAvailableDbServer(serverConfiguration));
	}
	
	@Test
	public void testNoServerName() throws Exception{
		String config = "<ordsServerConfig><serverList><server name=\"server1\" maintenance=\"true\" /> <server name=\"\" ip=\"1.2.3.4\" /></serverList></ordsServerConfig>";
		XMLConfiguration serverConfiguration = new XMLConfiguration();
		serverConfiguration.load(new StringReader(config));
		assertEquals("1.2.3.4", getAvailableDbServer(serverConfiguration));
	}
	
	@Test(expected = Exception.class)
	public void testInvalidConfig() throws Exception{
		String config = "sfgsfgdgdsfgds";
		XMLConfiguration serverConfiguration = new XMLConfiguration();
		serverConfiguration.load(new StringReader(config));
	}

}
