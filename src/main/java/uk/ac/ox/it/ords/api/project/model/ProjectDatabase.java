/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ox.it.ords.api.project.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "projectdatabase")
public class ProjectDatabase {
    @Id
	@GeneratedValue
	private int projectDatabaseId;
    private int projectId;

    public int getProjectDatabaseId() {
        return projectDatabaseId;
    }

    public void setProjectDatabaseId(int projectDatabaseId) {
        this.projectDatabaseId = projectDatabaseId;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }
}
