package uk.ac.ox.it.ords.api.project.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "permissions")
public class Permission {

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getPermission() {
		return permission;
	}

	public void setPermission(String permission) {
		this.permission = permission;
	}

    @Id
    @GeneratedValue
    private int id;
	private String role;
	private String permission;
	
	public Permission() {
	}

}
