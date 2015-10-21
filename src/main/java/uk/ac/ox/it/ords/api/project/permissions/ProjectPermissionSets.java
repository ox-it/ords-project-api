package uk.ac.ox.it.ords.api.project.permissions;

import java.util.ArrayList;
import java.util.List;

public class ProjectPermissionSets {
	
	public static List<String> getPermissionsForAnonymous(){
		ArrayList<String> permissions = new ArrayList<String>();
		permissions.add(ProjectPermissions.PROJECT_VIEW_PUBLIC);
		return permissions;
	}
	public static List<String> getPermissionsForUser(){
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