package uk.ac.ox.it.ords.api.project.permissions;

/**
 * Standard permission definitions used by the Project API.
 */
public class ProjectPermissions {

	//
	// These are generic permissions that apply across projects
	//
	
	public static final String PROJECT_CREATE = "project:create";
	public static final String PROJECT_CREATE_FULL = "project:create-full";
	public static final String PROJECT_UPGRADE = "project:upgrade";
	public static final String PROJECT_MODIFY_ALL = "project:modify:*";
	public static final String PROJECT_VIEW_ALL = "project:view:*";
	public static final String PROJECT_VIEW_PUBLIC = "project:view-public";
	
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
