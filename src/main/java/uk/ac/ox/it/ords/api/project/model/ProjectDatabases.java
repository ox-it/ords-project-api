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
@Table(name = "projectdatabases")
public class ProjectDatabases {
    @Id
	@GeneratedValue
	private int projectDatabasesId;
    private int projectId;
    private String dbName;
    
    
    

    public int getProjectDatabasesId() {
        return projectDatabasesId;
    }

    public void setProjectDatabasesId(int projectDatabasesId) {
        this.projectDatabasesId = projectDatabasesId;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }
    
}
