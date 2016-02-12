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
package uk.ac.ox.it.ords.api.project.resources;

import java.io.*;
import java.util.Date;

@SuppressWarnings("serial")
public class ProjectTestModel implements Serializable {

    private int projectId;
    private boolean privateProject;
    private String projectUuid;
    private boolean deleted;
    private String name;
    private String title;
    private boolean odbcSet;
    private String odbcConnectionURL;
    private String dbServerAddress;
    private boolean trialProject = true;
    private boolean fullProjectRequested = false;
    private String description;
    private String startDate, endDate;
    private Date dateCreated;
    private String dbServerPublicAddress;
    private String owner;
    private boolean canEdit; 
    private boolean canDelete;

    public ProjectTestModel() {
    }
    
    public void setId(int projectId) {
        this.projectId = projectId;
    }

	public int getProjectId() {
		return projectId;
	}

	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}

	public boolean isPrivateProject() {
		return privateProject;
	}

	public void setPrivateProject(boolean privateProject) {
		this.privateProject = privateProject;
	}

	public String getProjectUuid() {
		return projectUuid;
	}

	public void setProjectUuid(String projectUuid) {
		this.projectUuid = projectUuid;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
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
		this.title = title;
	}

	public boolean isOdbcSet() {
		return odbcSet;
	}

	public void setOdbcSet(boolean odbcSet) {
		this.odbcSet = odbcSet;
	}

	public String getOdbcConnectionURL() {
		return odbcConnectionURL;
	}

	public void setOdbcConnectionURL(String odbcConnectionURL) {
		this.odbcConnectionURL = odbcConnectionURL;
	}

	public String getDbServerAddress() {
		return dbServerAddress;
	}

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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public String getDbServerPublicAddress() {
		return dbServerPublicAddress;
	}

	public void setDbServerPublicAddress(String dbServerPublicAddress) {
		this.dbServerPublicAddress = dbServerPublicAddress;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public boolean canEdit() {
		return canEdit;
	}

	public void setCanEdit(boolean canEdit) {
		this.canEdit = canEdit;
	}

	public boolean canDelete() {
		return canDelete;
	}

	public void setCanDelete(boolean canDelete) {
		this.canDelete = canDelete;
	}
    
    
  
}
