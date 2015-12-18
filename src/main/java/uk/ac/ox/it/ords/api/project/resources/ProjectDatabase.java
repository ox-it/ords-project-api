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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;

import java.net.URI;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.it.ords.api.project.model.Database;
import uk.ac.ox.it.ords.api.project.model.DatabaseVersion;
import uk.ac.ox.it.ords.api.project.permissions.ProjectPermissions;
import uk.ac.ox.it.ords.api.project.services.DatabaseVersionService;
import uk.ac.ox.it.ords.api.project.services.ProjectAuditService;
import uk.ac.ox.it.ords.api.project.services.ProjectDatabaseService;
import uk.ac.ox.it.ords.api.project.services.ProjectService;

@Api(value="Project Database")
@CrossOriginResourceSharing(allowAllOrigins=true)
public class ProjectDatabase {
	
	Logger log = LoggerFactory.getLogger(ProjectDatabase.class);
	
	@ApiOperation(value="Modifies a logical database")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Project databases successfully updated.", response=Database.class),
		    @ApiResponse(code = 400, message = "Invalid data supplied."),
		    @ApiResponse(code = 403, message = "Not authorized to update this database."),
		    @ApiResponse(code = 404, message = "Database not found."),
		    @ApiResponse(code = 410, message = "Project has been deleted."),
	})
	// 
	// These are used by upstream gateways; including them here makes it easier to use an API portal
	//
	@ApiImplicitParams({
	    @ApiImplicitParam(name = "Version", value = "API version number", required = false, dataType = "string", paramType = "header"),
	    @ApiImplicitParam(name = "Authorization", value = "API key", required = false, dataType = "string", paramType = "header"),
	  })
	@Path("/{id}/database/{db}")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)	
	public Response updateDatabase(
			@PathParam("id") final int id,
			@PathParam("db") final int db,
			final Database database
			) throws Exception{
		
		uk.ac.ox.it.ords.api.project.model.Project project = ProjectService.Factory.getInstance().getProject(id);
		
		if (project == null){
			return Response.status(404).build();
		}
		
		if (project.isDeleted()){
			return Response.status(Status.GONE).build();
		}
		
		if (!SecurityUtils.getSubject().isPermitted(ProjectPermissions.PROJECT_MODIFY(id))){
			ProjectAuditService.Factory.getInstance().createNotAuthRecord("projectdatabase:modify", id);
			return Response.status(403).build();
		}
		
		Database existing;
		
		existing = ProjectDatabaseService.Factory.getInstance().getDatabase(db);

		if (existing == null){
			return Response.status(404).build();
		}
		if (database == null){
			return Response.status(400).build();
		}
		
		//
		// Validate the input
		//
		ProjectDatabaseService.Factory.getInstance().validate(database);
		
		//
		// Check for cross-resource attacks
		//
		if (existing.getLogicalDatabaseId() != database.getLogicalDatabaseId()){
			return Response.status(400).build();
		}
		
		//
		// Update the database
		//
		Database updated = ProjectDatabaseService.Factory.getInstance().updateDatabase(database);
		
		return Response.ok(updated).build();
	}
	
	@ApiOperation(value="Get a logical database")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Project databases successfully retrieved.", response=Database.class),
		    @ApiResponse(code = 400, message = "Invalid data supplied."),
		    @ApiResponse(code = 403, message = "Not authorized to view this database."),
		    @ApiResponse(code = 404, message = "Database not found."),
		    @ApiResponse(code = 410, message = "Project has been deleted."),
	})
	// 
	// These are used by upstream gateways; including them here makes it easier to use an API portal
	//
	@ApiImplicitParams({
	    @ApiImplicitParam(name = "Version", value = "API version number", required = false, dataType = "string", paramType = "header"),
	    @ApiImplicitParam(name = "Authorization", value = "API key", required = false, dataType = "string", paramType = "header"),
	  })
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
		
		Database database;
		
		database = ProjectDatabaseService.Factory.getInstance().getDatabase(db);

		if (database == null){
			return Response.status(404).build();
		}
		
		//
		// Add database versions
		//
		List<DatabaseVersion> databaseVersions = DatabaseVersionService.Factory.getInstance().getDatabaseVersions(database.getLogicalDatabaseId());
		database.setDatabaseVersions(databaseVersions);
		database.setNumberOfPhysicalDatabases(databaseVersions.size());
		
		return Response.ok(database).build();
	}
	
	@ApiOperation(value="Gets all logical databases for a project")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Project databases successfully updated.", response=Database.class, responseContainer="List"),
		    @ApiResponse(code = 400, message = "Invalid data supplied."),
		    @ApiResponse(code = 403, message = "Not authorized to view this project."),
		    @ApiResponse(code = 404, message = "Project not found."),
		    @ApiResponse(code = 410, message = "Project has been deleted."),
	})
	// 
	// These are used by upstream gateways; including them here makes it easier to use an API portal
	//
	@ApiImplicitParams({
	    @ApiImplicitParam(name = "Version", value = "API version number", required = false, dataType = "string", paramType = "header"),
	    @ApiImplicitParam(name = "Authorization", value = "API key", required = false, dataType = "string", paramType = "header"),
	  })
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
		
		List<Database> databases = ProjectDatabaseService.Factory.getInstance().getDatabasesForProject(id);
		return Response.ok(databases).build();		
	}
	
	@ApiOperation(value="Creates a logical database")
	@ApiResponses(value = { 
			@ApiResponse(code = 201, message = "Project databases successfully created.",					
					responseHeaders = @ResponseHeader(name = "Location", description = "The URI of the logical Database", response = URI.class)
					),
		    @ApiResponse(code = 400, message = "Invalid data supplied."),
		    @ApiResponse(code = 403, message = "Not authorized to create a database for this project."),
		    @ApiResponse(code = 404, message = "Database not found."),
		    @ApiResponse(code = 410, message = "Project has been deleted."),
	})
	// 
	// These are used by upstream gateways; including them here makes it easier to use an API portal
	//
	@ApiImplicitParams({
	    @ApiImplicitParam(name = "Version", value = "API version number", required = false, dataType = "string", paramType = "header"),
	    @ApiImplicitParam(name = "Authorization", value = "API key", required = false, dataType = "string", paramType = "header"),
	  })
	@Path("/{id}/database")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addDatabaseToProject(
			@PathParam("id") final int id,
			Database database,
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
		if (database.getDatabaseProjectId() != project.getProjectId()){
			return Response.status(400).build();
		}
		
		database = ProjectDatabaseService.Factory.getInstance().addDatabase(database);

		UriBuilder builder = uriInfo.getAbsolutePathBuilder();
		builder.path(Integer.toString(database.getLogicalDatabaseId()));
		return Response.created(builder.build()).build();

	}
	
	@ApiOperation(value="Deletes a logical database")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Project database successfully deleted."),
		    @ApiResponse(code = 400, message = "Invalid data supplied."),
		    @ApiResponse(code = 403, message = "Not authorized to delete this database."),
		    @ApiResponse(code = 404, message = "Database not found."),
		    @ApiResponse(code = 410, message = "Project has been deleted."),
	})
	// 
	// These are used by upstream gateways; including them here makes it easier to use an API portal
	//
	@ApiImplicitParams({
	    @ApiImplicitParam(name = "Version", value = "API version number", required = false, dataType = "string", paramType = "header"),
	    @ApiImplicitParam(name = "Authorization", value = "API key", required = false, dataType = "string", paramType = "header"),
	  })
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
		
		Database database = null; 
		
		database = ProjectDatabaseService.Factory.getInstance().getDatabase(db);
		
		if (database == null){
			return Response.status(404).build();
		}
		
		//
		// Prevent side-attack; ensure the database belongs to the project specified
		//
		if (database.getDatabaseProjectId() != id){
			return Response.status(400).build();
		}
		
		ProjectDatabaseService.Factory.getInstance().removeDatabase(db);
		return Response.ok().build();
		
	}

}
