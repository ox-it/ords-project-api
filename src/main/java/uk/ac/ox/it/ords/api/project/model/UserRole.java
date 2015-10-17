package uk.ac.ox.it.ords.api.project.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@SuppressWarnings("serial")
@Entity
@Table(name = "userrole")
public class UserRole implements Serializable {

    @Id
    @GeneratedValue
    private int id;
    private String principalName;
    private String role;

    public enum ProjectRoleValue {
        OWNER, PROJECT_ADMINISTRATOR, CONTRIBUTOR, VIEWER, DELETED
    };

    public UserRole() {
    }

	public String getPrincipalName() {
		return principalName;
	}

	public void setPrincipalName(String principalName) {
		this.principalName = principalName;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
    
}
