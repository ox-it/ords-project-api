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

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@Entity
@Table(name = "ordsPhysicalDatabase")
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatabaseVersion implements Cloneable {

    public enum EntityType {

        MAIN, TEST, MILESTONE;
    }

    public enum ImportType {

        QUEUED, SECONDARY_CSV_QUEUED, SECONDARY_CSV_IN_PROGRESS, IN_PROGRESS, FINISHED;
    }
    @Id
    @GeneratedValue
    private int physicalDatabaseId;
    private int logicalDatabaseId;
    private long fileSize;
    @Enumerated(EnumType.ORDINAL)
    private EntityType entityType;
    private String uploadedHost = null; // The host where the upload took place
    @NotNull
    private String fullPathToDirectory;
    @NotNull
    private String fileName;
    protected String databaseType;
    private int actorId;

    @Enumerated(EnumType.ORDINAL)
    private ImportType importProgress;

    /**
     * Does this entry exist as a real database in the server. TODO It may be
     * that this variable is now antiquated
     */
    private boolean representationExists;
    private boolean dbConsumed;
    @Column(name = "dbconsumedname", unique = true)
    private String dbConsumedName;

    @NotNull
    private String uuid;

    public DatabaseVersion() {
        setUuid(UUID.randomUUID().toString());
        setImportProgress(ImportType.QUEUED);
    }

    public DatabaseVersion clone() throws CloneNotSupportedException {
        return (DatabaseVersion) super.clone();
    }

    public int getPhysicalDatabaseId() {
        return physicalDatabaseId;
    }

    public void setPhysicalDatabaseId(int physicalDatabaseId) {
        this.physicalDatabaseId = physicalDatabaseId;
    }

    public int getLogicalDatabaseId() {
        return logicalDatabaseId;
    }

    public void setLogicalDatabaseId(int logicalDatabaseId) {
        this.logicalDatabaseId = logicalDatabaseId;
    }

    public String getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
    }

    public String getFullPathToDirectory() {
        return fullPathToDirectory;
    }

    public void setFullPathToDirectory(String fullPathToDirectory) {
        this.fullPathToDirectory = fullPathToDirectory;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long l) {
        this.fileSize = l;
    }

    public ImportType getImportProgress() {
        return importProgress;
    }

    public void setImportProgress(ImportType importProgress) {
        this.importProgress = importProgress;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public boolean isRepresentationExists() {
        return representationExists;
    }

    public void setRepresentationExists(boolean representationExists) {
        this.representationExists = representationExists;
    }

    public boolean isDbConsumed() {
        return dbConsumed;
    }

    public int getActorId() {
        return actorId;
    }

    public void setActorId(int actorId) {
        this.actorId = actorId;
    }

    public void setDbConsumed(boolean dbConsumed) {
        this.dbConsumed = dbConsumed;
    }

    public String getDbConsumedName() {
        /*
         * FIXME
         * A getter should not alter the data - needs to be sorted out
         */
        if (dbConsumedName == null) {
            String name = (getEntityType().toString() + "_" + getPhysicalDatabaseId() + "_" + getLogicalDatabaseId()).toLowerCase();

            setDbConsumedName(name);
        }
        return dbConsumedName;
    }

    public void setDbConsumedName(String dbConsumedName) {
        this.dbConsumedName = dbConsumedName;
    }

    public String getUploadedHost() {
        return uploadedHost;
    }

    public void setUploadedHost(String uploadedHost) {
        this.uploadedHost = uploadedHost;
    }
    
}
