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

import uk.ac.ox.it.ords.api.project.model.Project;

/**
 * @TODO Allocates actual database server space to projects
 * See ProjectWork.java in OrdsFrontEnd
 * 
 * @author scottw
 * 
 */
public interface ServerDetailsService {
	
	
	public Project setProjectServerDetails(Project project) throws Exception;
	
//    GeneralWebServicesUtils.ServerDetails details;
//    try {
//        details = GeneralWebServicesUtils.getAvailableDbServerDetails();
//        if (details == null) {
//            log.error("No servers available");
//            m = emd.getMessage("Fpp003");
//        }
//        else {
//            project.setOdbcConnectionURL(ODBCUtils.createOdbcConnectionString(details.serverName));
//            project.setDbServerAddress(details.serverName);
//
//            /*
//             * Check the input is valid - cannot have tripple
//             * underscores since that is used internally
//             */
//            String newMessage = ProjectFunctions.createNewProject(project, user.getUserId(), remoteUser);
//            if (newMessage == null) {
//                m = emd.getMessage("Fpp002");
//            }
//            else {
//                m = emd.getMessage("Fpp003", newMessage);
//            }
//        }
//    }

}
