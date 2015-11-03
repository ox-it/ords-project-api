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

import java.util.ArrayList;
import java.util.List;

/**
 * Standard permission sets for roles used in the Project API
 */
public class ProjectPermissionSets {
	
	public static List<String> getPermissionsForAnonymous(){
		ArrayList<String> permissions = new ArrayList<String>();
		permissions.add(ProjectPermissions.PROJECT_VIEW_PUBLIC);
		return permissions;
	}
	public static List<String> getPermissionsForUser(){
		List<String> permissions = getPermissionsForAnonymous();
		return permissions;
	}
	public static List<String> getPermissionsForLocalUser(){
		List<String> permissions = getPermissionsForAnonymous();
		permissions.add(ProjectPermissions.PROJECT_CREATE);
		return permissions;
	}
	public static List<String> getPermissionsForViewer(int id){
		List<String> permissions = getPermissionsForUser();
		permissions.add(ProjectPermissions.PROJECT_VIEW(id));
		return permissions;
	}
	public static List<String> getPermissionsForContributor(int id){
		List<String> permissions = getPermissionsForViewer(id);
		permissions.add(ProjectPermissions.PROJECT_MODIFY(id));
		return permissions;
	}
	public static List<String> getPermissionsForProjectAdmin(int id){
		List<String> permissions = getPermissionsForContributor(id);
		permissions.add(ProjectPermissions.PROJECT_VIEW_INVITATIONS(id));		
		return permissions;
	}
	public static List<String> getPermissionsForOwner(int id){
		List<String> permissions = getPermissionsForContributor(id);
		permissions.add(ProjectPermissions.PROJECT_ANY_ACTION(id));
		return permissions;
	}
	public static List<String> getPermissionsForSysadmin(){
		ArrayList<String> permissions = new ArrayList<String>();
		permissions.add(ProjectPermissions.PROJECT_CREATE_FULL);
		permissions.add(ProjectPermissions.PROJECT_UPGRADE);
		permissions.add(ProjectPermissions.PROJECT_MODIFY_ALL);
		permissions.add(ProjectPermissions.PROJECT_VIEW_ALL);
		return permissions;
	}
}
