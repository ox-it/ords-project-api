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

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.it.ords.api.project.permissions.ProjectPermissions;
import uk.ac.ox.it.ords.api.project.services.ProjectAuditService;
import uk.ac.ox.it.ords.api.project.services.ProjectDatabaseService;
import uk.ac.ox.it.ords.api.project.services.ProjectService;

public class ProjectDatabase {
	
	Logger log = LoggerFactory.getLogger(ProjectDatabase.class);
	
	@Path("/{id}/database/{db}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDatabaseForProject(
			@PathParam("id") final int id,
			@PathParam("db") final int db
			) throws Exception{
		
		uk.ac.ox.it.ords.api.project.model.Project project = ProjectService.Factory.getInstance().getProject(id);
		
		if (project == null){
			return Response.status(404).build();
		}
		
		if (project.isDeleted()){
			return Response.status(Status.GONE).build();
		}
		
		if (project.isPrivateProject() && !SecurityUtils.getSubject().isPermitted(ProjectPermissions.PROJECT_VIEW(id))){
			ProjectAuditService.Factory.getInstance().createNotAuthRecord("project:view", id);
			return Response.status(403).build();
		}
		
		uk.ac.ox.it.ords.api.project.model.ProjectDatabase database;
		
		database = ProjectDatabaseService.Factory.getInstance().getDatabaseForProject(db);

		if (database == null){
			return Response.status(404).build();
		}
		
		return Response.ok(database).build();
	}
	
	@Path("/{id}/database")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDatabasesForProject(
			@PathParam("id") final int id
			) throws Exception{
		
		uk.ac.ox.it.ords.api.project.model.Project project = ProjectService.Factory.getInstance().getProject(id);
		
		if (project == null){
			return Response.status(404).build();
		}
		
		if (project.isDeleted()){
			return Response.status(Status.GONE).build();
		}
		
		if (project.isPrivateProject() && !SecurityUtils.getSubject().isPermitted(ProjectPermissions.PROJECT_VIEW(id))){
			ProjectAuditService.Factory.getInstance().createNotAuthRecord("project:view", id);
			return Response.status(403).build();
		}
		
		List<uk.ac.ox.it.ords.api.project.model.ProjectDatabase> databases = ProjectDatabaseService.Factory.getInstance().getDatabasesForProject(id);
		return Response.ok(databases).build();		
	}
	
	@Path("/{id}/database")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addDatabaseToProject(
			@PathParam("id") final int id,
			uk.ac.ox.it.ords.api.project.model.ProjectDatabase database,
			@Context UriInfo uriInfo
			) throws Exception{
		
		uk.ac.ox.it.ords.api.project.model.Project project = ProjectService.Factory.getInstance().getProject(id);
		
		if (project == null){
			return Response.status(404).build();
		}
		
		if (project.isDeleted()){
			return Response.status(Status.GONE).build();
		}
		
		if (!SecurityUtils.getSubject().isPermitted(ProjectPermissions.PROJECT_MODIFY(id))){
			ProjectAuditService.Factory.getInstance().createNotAuthRecord("project:modify", id);
			return Response.status(403).build();
		}
		
		//
		// Prevent side-attack; ensure the database belongs to the project specified
		//
		if (database.getProjectId() != project.getProjectId()){
			return Response.status(400).build();
		}
		
		database = ProjectDatabaseService.Factory.getInstance().addDatabaseToProject(id, database);

		UriBuilder builder = uriInfo.getAbsolutePathBuilder();
		builder.path(Integer.toString(database.getProjectDatabaseId()));
		return Response.created(builder.build()).build();

	}
	
	@Path("/{id}/database/{db}")
	@DELETE
	public Response removeDatabaseFromProject(
			@PathParam("id") final int id,
			@PathParam("db") final int db
			) throws Exception{
		
		uk.ac.ox.it.ords.api.project.model.Project project = ProjectService.Factory.getInstance().getProject(id);
		
		if (project == null){
			return Response.status(404).build();
		}
		
		if (project.isDeleted()){
			return Response.status(Status.GONE).build();
		}
		
		if (!SecurityUtils.getSubject().isPermitted(ProjectPermissions.PROJECT_MODIFY(id))){
			ProjectAuditService.Factory.getInstance().createNotAuthRecord("project:modify", id);
			return Response.status(403).build();
		}
		
		uk.ac.ox.it.ords.api.project.model.ProjectDatabase database = null; 
		
		database = ProjectDatabaseService.Factory.getInstance().getDatabaseForProject(db);
		
		if (database == null){
			return Response.status(404).build();
		}
		
		//
		// Prevent side-attack; ensure the database belongs to the project specified
		//
		if (database.getProjectId() != id){
			return Response.status(400).build();
		}
		
		ProjectDatabaseService.Factory.getInstance().removeDatabaseFromProject(id, db);
		return Response.ok().build();
		
	}

}
