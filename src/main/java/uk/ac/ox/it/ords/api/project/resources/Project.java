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
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ResponseHeader;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;
import org.apache.shiro.SecurityUtils;

import uk.ac.ox.it.ords.api.project.permissions.ProjectPermissions;
import uk.ac.ox.it.ords.api.project.services.ProjectAuditService;
import uk.ac.ox.it.ords.api.project.services.ProjectService;

@Api(value="Project")
@CrossOriginResourceSharing(allowAllOrigins=true)
public class Project {
	
	Logger log = LoggerFactory.getLogger(Project.class);

	public Project() {
	}
	
	@ApiOperation(
			value="Gets a list of projects", 
			notes="Returns by default the projects the authenticated user is a member of, or can be used to "
					+ "return the list of open projects, the list of full (non-demo) projects, or the results of a query", 
			response = uk.ac.ox.it.ords.api.project.model.Project.class, 
			responseContainer = "List"
			)
	// 
	// These are used by upstream gateways; including them here makes it easier to use an API portal
	//
	@ApiImplicitParams({
	    @ApiImplicitParam(name = "Version", value = "API version number", required = false, dataType = "string", paramType = "header"),
	    @ApiImplicitParam(name = "Authorization", value = "API key", required = false, dataType = "string", paramType = "header"),
	  })
	@Path("/")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getProjects(
			@ApiParam(value = "return all open projects", required = false) @QueryParam("open") final boolean open,
			@ApiParam(value = "return all full projects", required = false) @QueryParam("full") final boolean full,
			@ApiParam(value = "return projects matching the query", required = false) @QueryParam("q") final String q
			) {
		
		List<uk.ac.ox.it.ords.api.project.model.Project> projects;
		
		if (open){
			
			//
			// All open projects i.e. not private or deleted
			//
			projects = ProjectService.Factory.getInstance().getOpenProjects();
			
		} else {
			
			if (full){	
				
				//
				// To get the list of FULL projects, including private,
				// requires a security check. This is carried out by
				// the service implementation.
				//
				projects = ProjectService.Factory.getInstance().getFullProjects();
				
			} else {
				
				if (q != null){
					
					//
					// Search projects. Only visible projects are included.
					//
					projects = ProjectService.Factory.getInstance().searchProjects(q);
					
				} else {
					
					//
					// The projects the user is involved in as a member.
					//
					projects = ProjectService.Factory.getInstance().getProjects();
				}
			}
		}
		return Response.ok(projects).build();
		
		
	}

	@ApiOperation(value="Gets a project")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Project successfully returned.", response=uk.ac.ox.it.ords.api.project.model.Project.class),
		    @ApiResponse(code = 400, message = "Invalid ID supplied."),
		    @ApiResponse(code = 403, message = "Project is private and client not authorized to view it."),
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
	@Path("/{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getProject(
			@PathParam("id") final int id, 
			@Context HttpServletResponse response
			) throws IOException{
		
		
		uk.ac.ox.it.ords.api.project.model.Project project = ProjectService.Factory.getInstance().getProject(id);
		
		if (project == null){
			return Response.status(404).build();
		}
		
		if (project.isPrivateProject()){
			if (!SecurityUtils.getSubject().isPermitted(ProjectPermissions.PROJECT_VIEW(id))){
				ProjectAuditService.Factory.getInstance().createNotAuthRecord("project:view", id);
				return Response.status(403).build();
			}
		}
		if (project.isDeleted()){
			return Response.status(Status.GONE).build();
		}
		
		return Response.ok(project).build();
	}

	@ApiOperation(value="Deletes a project")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Project successfully deleted."),
		    @ApiResponse(code = 400, message = "Invalid ID supplied."),
		    @ApiResponse(code = 403, message = "Not authorized to delete this project."),
		    @ApiResponse(code = 404, message = "Project not found."),
		    @ApiResponse(code = 410, message = "Project has already been deleted."),
	})
	// 
	// These are used by upstream gateways; including them here makes it easier to use an API portal
	//
	@ApiImplicitParams({
	    @ApiImplicitParam(name = "Version", value = "API version number", required = false, dataType = "string", paramType = "header"),
	    @ApiImplicitParam(name = "Authorization", value = "API key", required = false, dataType = "string", paramType = "header"),
	  })
	@Path("/{id}")
	@DELETE
	public Response deleteProject(
			@PathParam("id") final int id, 
			@Context SecurityContext securityContext
			) throws Exception{
		
		uk.ac.ox.it.ords.api.project.model.Project project = ProjectService.Factory.getInstance().getProject(id);
		
		if (project == null){
			return Response.status(404).build();
		} 
		
		if (project.isDeleted()){
			return Response.status(Status.GONE).build();
		}
		
		if (!SecurityUtils.getSubject().isPermitted(ProjectPermissions.PROJECT_DELETE(id))){
			ProjectAuditService.Factory.getInstance().createNotAuthRecord("project:delete", id);
			return Response.status(403).build();
		}

		ProjectService.Factory.getInstance().deleteProject(id);
		return Response.ok().build();
	}
	
	@ApiOperation(value="Updates a project")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Project successfully updated.", response=uk.ac.ox.it.ords.api.project.model.Project.class),
		    @ApiResponse(code = 400, message = "Invalid Project supplied."),
		    @ApiResponse(code = 403, message = "Not authorized to update this project."),
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
	@Path("/{id}")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateProject(
			uk.ac.ox.it.ords.api.project.model.Project project,
			@PathParam("id") final int id 
			) throws Exception {
		
		uk.ac.ox.it.ords.api.project.model.Project oldProject = ProjectService.Factory.getInstance().getProject(id);
		
		if (oldProject == null){
			return Response.status(404).build();
		}
		
		if (project == null){
			return Response.status(400).build();
		}
		
		if (oldProject.isDeleted()){
			return Response.status(Status.GONE).build();
		}
		
		//
		// Prevent side-modification hacks
		//
		if (id != project.getProjectId()){
			return Response.status(400).build();
		}
		
		//
		// Is this an upgrade request? If so check if the user has the project:upgrade permission.
		//
		if (oldProject.isTrialProject() == true && project.isTrialProject() == false){
			if (!SecurityUtils.getSubject().isPermitted(ProjectPermissions.PROJECT_UPGRADE)){
				ProjectAuditService.Factory.getInstance().createNotAuthRecord("project:upgrade", id);
				return Response.status(403).build();
			}
		}

		if (!SecurityUtils.getSubject().isPermitted(ProjectPermissions.PROJECT_MODIFY(id))){
			ProjectAuditService.Factory.getInstance().createNotAuthRecord("project:update", id);
			return Response.status(403).build();
		}
		
		//
		// Fulfill update request
		//
		uk.ac.ox.it.ords.api.project.model.Project updatedProject = ProjectService.Factory.getInstance().updateProject(project);
		return Response.ok(updatedProject).build();
	}

	@ApiOperation(value="Creates a project")
	@ApiResponses(value = { 
			@ApiResponse(code = 201, message = "Project successfully created.",
					responseHeaders = @ResponseHeader(name = "Location", description = "The URI of the Project", response = URI.class)
					),
		    @ApiResponse(code = 400, message = "Invalid Project."),
		    @ApiResponse(code = 403, message = "Not authorized to create a Project.")
	})
	// 
	// These are used by upstream gateways; including them here makes it easier to use an API portal
	//
	@ApiImplicitParams({
	    @ApiImplicitParam(name = "Version", value = "API version number", required = false, dataType = "string", paramType = "header"),
	    @ApiImplicitParam(name = "Authorization", value = "API key", required = false, dataType = "string", paramType = "header"),
	  })
	@Path("/")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createProject(
			uk.ac.ox.it.ords.api.project.model.Project project,
			@Context UriInfo uriInfo
			) throws Exception {

		if (!SecurityUtils.getSubject().isPermitted(ProjectPermissions.PROJECT_CREATE) && !SecurityUtils.getSubject().isPermitted(ProjectPermissions.PROJECT_CREATE_FULL)){
			ProjectAuditService.Factory.getInstance().createNotAuthRecord("project:create", -1);
			return Response.status(403).build();
		}

		if (project == null){
			return Response.status(400).build();
		}
		
		//
		// Only users with the project:create-full permission can create a new project that has TrialProject set to False
		//
		if (project.isTrialProject() == false && !SecurityUtils.getSubject().isPermitted(ProjectPermissions.PROJECT_CREATE_FULL)){
			ProjectAuditService.Factory.getInstance().createNotAuthRecord("project:create-full", -1);
			return Response.status(403).build();
		}

		ProjectService.Factory.getInstance().createProject(project);		
	    UriBuilder builder = uriInfo.getAbsolutePathBuilder();
	    builder.path(Integer.toString(project.getProjectId()));
	    return Response.created(builder.build()).build();
	}


}
