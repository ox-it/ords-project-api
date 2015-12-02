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

package uk.ac.ox.it.ords.api.project.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name="ordsdb")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Database {
	
	@Id
	@GeneratedValue
	private int logicalDatabaseId;
	
	//
	// Despite the confusing name, this is actually the project ID this belongs to
	//
	private int databaseProjectId;
	private int numberOfPhysicalDatabases;

	@NotNull
	private String databaseType;

	@NotNull
	@Size(min = 2, max = 100)
	private String dbName = "";

	@NotNull
	@Size(min = 2, max = 65000)
	private String dbDescription = "";
	private String creationDate = "";
	private String dataSources = "";
	private String dataGatheringProcess = "";
	private String dataInterfaceName = "";

	public Database() {
	}


	public int getLogicalDatabaseId() {
		return logicalDatabaseId;
	}


	public void setLogicalDatabaseId(int logicalDatabaseId) {
		this.logicalDatabaseId = logicalDatabaseId;
	}


	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public String getDbDescription() {
		return dbDescription;
	}

	public void setDbDescription(String dbDescription) {
		this.dbDescription = dbDescription;
	}

	public String getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}

	public String getDataSources() {
		return dataSources;
	}

	public void setDataSources(String dataSources) {
		this.dataSources = dataSources;
	}

	public String getDataGatheringProcess() {
		return dataGatheringProcess;
	}

	public void setDataGatheringProcess(String dataGatheringProcess) {
		this.dataGatheringProcess = dataGatheringProcess;
	}

	public String getDataInterfaceName() {
		return dataInterfaceName;
	}

	public void setDataInterfaceName(String dataInterfaceName) {
		this.dataInterfaceName = dataInterfaceName;
	}


	public int getDatabaseProjectId() {
		return databaseProjectId;
	}


	public void setDatabaseProjectId(int databaseProjectId) {
		this.databaseProjectId = databaseProjectId;
	}


	public String getDatabaseType() {
		return databaseType;
	}


	public void setDatabaseType(String databaseType) {
		this.databaseType = databaseType;
	}

	public int getNumberOfPhysicalDatabases() {
		return numberOfPhysicalDatabases;
	}

	public void setNumberOfPhysicalDatabases(int numberOfPhysicalDatabases) {
		this.numberOfPhysicalDatabases = numberOfPhysicalDatabases;
	}
	
	@Transient
	private List<DatabaseVersion> databaseVersions;
	
	@JsonProperty
	public List<DatabaseVersion> getDatabaseVersions(){
		return databaseVersions;
	}
	
	@JsonIgnore
	public void setDatabaseVersions(List<DatabaseVersion> databases){
		this.databaseVersions = databases;
	}


}
