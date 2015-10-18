package uk.ac.ox.it.ords.api.project.model;

import java.io.*;
import java.util.Date;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("serial")
@Entity
@Table(name = "project")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Project implements Serializable {

    @Id
    @GeneratedValue
    private int projectId;
    private boolean privateProject;
    
    @JsonIgnore 
    private String projectUuid;
    
    private boolean deleted;
    @NotNull
    @Size(min = 2, max = 255)
    private String name;
    private String title;
    private boolean odbcSet;
    private String odbcConnectionURL;
    private String dbServerAddress;
    
    // Trial Project work
    private boolean trialProject = true;
    /*
     * Notes about trial projects
     * Trial projects are limited in terms of how many records can be added and the data space permitted.
     * Owners of trial projects should be contacted at least annually to see whether they wish to retain the 
     * trial projects they have created, discuss converting them into ‘full’ projects, or have them removed 
     * from the ORDS system.
     *
     * Whilst a project has a ‘trial’ status, that project should not be able to publish datasets, 
     * make information publicly accessible, nor add more than 100 records to any given table. A ‘soft’ 
     * limit of 1MB data space should be assigned. If this is exceeded then a member of the ORDS team should 
     * get in touch with the project owner. Database importing should be allowed, but without the ability 
     * to add new records to a table that already has over 100 records. 
     */
    
    /**
     * The is set when a user requests a trail project be made full. An
     * email is sent to admin, and this flag is set so that the user can't keep 
     * making the same request.
     */
    private boolean fullProjectRequested = false;

    private String description;
    private String startDate, endDate;
    
    @JsonIgnore 
    private String dateCreated;

    public Project() {
        Date date = new Date();
        this.setDateCreated(date.toString());
        this.setStartDate(startDate = date.toString());
        this.setProjectUuid(UUID.randomUUID().toString());
    }

    public int getProjectId() {
        return projectId;
    }

    @JsonIgnore
    public void setId(int projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title == null) {
            title = "";
        }
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStartDate() {
        if (startDate == null) {
            startDate = "";
        }
        return startDate;
    }

    public void setStartDate(String startDate) {
        if (startDate == null) {
            startDate = "";
        }
        this.startDate = startDate;
    }

    public String getEndDate() {
        if (endDate == null) {
            endDate = "";
        }
        return endDate;
    }

    public void setEndDate(String endDate) {
        if (endDate == null) {
            endDate = "";
        }
        this.endDate = endDate;
    }

    @JsonIgnore
    public String calculateTestableProjectName() {
        return name.toLowerCase().replace(" ", "");
    }

    public boolean isPrivateProject() {
        return privateProject;
    }

    public void setPrivateProject(boolean privateProject) {
        this.privateProject = privateProject;
    }

    @JsonIgnore
	public void setProjectUuid(String projectUuid) {
        this.projectUuid = projectUuid;
    }

    @JsonProperty
    public String getProjectUuid() {
        return projectUuid;
    }

    @JsonProperty
    public String getDateCreated() {
        return dateCreated;
    }

    @JsonIgnore 
    public void setDateCreated(String dateCreated) {
        if (dateCreated == null) {
            dateCreated = new Date().toString();
        }
        this.dateCreated = dateCreated;
    }

    @JsonIgnore
    public boolean isDeleted() {
        return deleted;
    }

    @JsonIgnore
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
    
    public boolean isOdbcSet() {
        return odbcSet;
    }

    public void setOdbcSet(boolean odbcSet) {
        this.odbcSet = odbcSet;
    }

    public String getDbServerPublicAddress() {
    	if (getDbServerAddress() == null) return null;
    	return getDbServerAddress().replaceAll(".sysdev.tld", ".ox.ac.uk");
    }
    
    public String getOdbcConnectionURL() {
	return odbcConnectionURL;
    }

    public void setOdbcConnectionURL(String odbcConnectionURL) {
        this.odbcConnectionURL = odbcConnectionURL;
    }

    @JsonIgnore
    public String getDbServerAddress() {
        return dbServerAddress;
    }

    @JsonIgnore
    public void setDbServerAddress(String dbServerAddress) {
        this.dbServerAddress = dbServerAddress;
    }

    public boolean isTrialProject() {
        return trialProject;
    }

    public void setTrialProject(boolean trialProject) {
        this.trialProject = trialProject;
    }
    
    public boolean isFullProjectRequested() {
        return fullProjectRequested;
    }

    public void setFullProjectRequested(boolean fullProjectRequested) {
        this.fullProjectRequested = fullProjectRequested;
    }
}
