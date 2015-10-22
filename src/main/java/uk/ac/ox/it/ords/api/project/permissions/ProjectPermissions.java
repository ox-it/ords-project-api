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
package uk.ac.ox.it.ords.api.project.permissions;

import uk.ac.ox.it.ords.security.permissions.Permissions;

/**
 * Standard permission definitions used by the Project API.
 */
public class ProjectPermissions extends Permissions{

	//
	// Contextual permissions that apply to individual projects
	//
	public static String PROJECT_ANY_ACTION(int id){
		return "project:*:"+id;
	}
	public static String PROJECT_DELETE(int id){
		return "project:delete:"+id;
	}
	
	public static String PROJECT_MODIFY(int id){
		return "project:modify:"+id;
	}
	
	public static String PROJECT_VIEW(int id){
		return "project:view:"+id;
	}
	
	public static String PROJECT_VIEW_INVITATIONS(int id){
		return "project:view-invitations:"+id;
	}
	

}
