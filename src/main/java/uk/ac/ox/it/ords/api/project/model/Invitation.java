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

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author dave
 */
@Entity
@Table(name = "ordsinvitationtable")
public class Invitation implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = -562541909157229542L;
	
    @Id
    @GeneratedValue
	private int id;
    
    /**
     * The project the invite applies to
     */
    private int projectId;
    
    /**
     * The verification code for the invite
     */
    @JsonProperty
    private String uuid;
    
    /**
     * The requested role
     */
    private String roleRequired;
    
    /**
     * The email address of the invitee
     */
    private String email;
    
    /**
     * The person or entity that sent the invitation
     */
    private String sender;
    
    public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Invitation() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }
    
    @JsonProperty
    public String getUuid() {
        return uuid;
    }

    @JsonIgnore 
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getRoleRequired() {
        return roleRequired;
    }

    public void setRoleRequired(String roleRequired) {
        this.roleRequired = roleRequired;
    }

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}
}
