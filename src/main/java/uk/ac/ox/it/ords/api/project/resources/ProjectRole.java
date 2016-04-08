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

import java.net.URI;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;

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

import uk.ac.ox.it.ords.api.project.model.Member;
import uk.ac.ox.it.ords.api.project.permissions.ProjectPermissions;
import uk.ac.ox.it.ords.api.project.services.ProjectAuditService;
import uk.ac.ox.it.ords.api.project.services.ProjectRoleService;
import uk.ac.ox.it.ords.api.project.services.ProjectService;
import uk.ac.ox.it.ords.security.model.UserRole;

@Api(value="Project Role (Members)")
@CrossOriginResourceSharing(allowAllOrigins=true)
public class ProjectRole {

	Logger log = LoggerFactory.getLogger(ProjectRole.class);
	
	@ApiOperation(value="Update the role of a project member")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Project member role successfully updated."),
		    @ApiResponse(code = 400, message = "Invalid data supplied."),
		    @ApiResponse(code = 403, message = "Not authorized to update this member role."),
		    @ApiResponse(code = 404, message = "Role not found."),
		    @ApiResponse(code = 410, message = "Project has been deleted."),
	})
	// 
	// These are used by upstream gateways; including them here makes it easier to use an API portal
	//
	@ApiImplicitParams({
	    @ApiImplicitParam(name = "Version", value = "API version number", required = false, dataType = "string", paramType = "header"),
	    @ApiImplicitParam(name = "Authorization", value = "API key", required = false, dataType = "string", paramType = "header"),
	  })
	@Path("/{projectId}/role/{roleId}")
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateProjectRole(
			@PathParam("projectId") final int projectId,
			@PathParam("roleId") final int roleId,
			UserRole member
			) throws Exception{
		
		uk.ac.ox.it.ords.api.project.model.Project project = ProjectService.Factory.getInstance().getProject(projectId);

		if (project == null){
			return Response.status(404).build();
		}
		
		if (project.isDeleted()){
			return Response.status(Status.GONE).build();
		}
		
		if (!SecurityUtils.getSubject().isPermitted(ProjectPermissions.PROJECT_MODIFY(projectId))){
			ProjectAuditService.Factory.getInstance().createNotAuthRecord("project:modify", projectId);
			return Response.status(403).build();
		}

		
		UserRole userRole = ProjectRoleService.Factory.getInstance().getUserRole(roleId);
		
		if (userRole == null){
			return Response.status(404).build();
		}
		
		//
		// Prevent cross-resource attack
		//
		if(!userRole.getRole().endsWith(String.valueOf(projectId))){
			return Response.status(400).build();
		}
		
		//
		// The only property we can update is the role required
		//
		userRole.setRole(member.getRole());
		
		ProjectRoleService.Factory.getInstance().updateProjectRole(userRole, projectId);
		
		//
		// TODO When we change a role, we have to drop the existing ODBC role associated with it
		//
		
		return Response.ok().build();
	}
	
	@ApiOperation(value="Get a member role")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Project role successfully retrieved.", response=Member.class),
		    @ApiResponse(code = 400, message = "Invalid data supplied."),
		    @ApiResponse(code = 403, message = "Not authorized to get this member role."),
		    @ApiResponse(code = 404, message = "Role not found."),
		    @ApiResponse(code = 410, message = "Project has been deleted."),
	})
	// 
	// These are used by upstream gateways; including them here makes it easier to use an API portal
	//
	@ApiImplicitParams({
	    @ApiImplicitParam(name = "Version", value = "API version number", required = false, dataType = "string", paramType = "header"),
	    @ApiImplicitParam(name = "Authorization", value = "API key", required = false, dataType = "string", paramType = "header"),
	  })
	@Path("/{projectId}/role/{roleId}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getProjectRole(
				@PathParam("projectId") final int projectId,
				@PathParam("roleId") final int roleId
			) throws Exception{
		
		uk.ac.ox.it.ords.api.project.model.Project project = ProjectService.Factory.getInstance().getProject(projectId);
		
		if (project == null){
			return Response.status(404).build();
		}
		
		if (project.isDeleted()){
			return Response.status(Status.GONE).build();
		}
		
		if (project.isPrivateProject()){
			if (!SecurityUtils.getSubject().isPermitted(ProjectPermissions.PROJECT_VIEW(projectId))){
				ProjectAuditService.Factory.getInstance().createNotAuthRecord("project:view", projectId);
				return Response.status(403).build();
			}
		}
		
		UserRole userRole = ProjectRoleService.Factory.getInstance().getUserRole(roleId);
		
		if (userRole == null){
			return Response.status(404).build();
		}
		
		//
		// Prevent cross-resource attack
		//
		if(!userRole.getRole().endsWith(String.valueOf(projectId))){
			return Response.status(400).build();
		}
		
		return Response.ok(new Member(userRole)).build();
		
	}
	
	@ApiOperation(value="Get all member roles in a project")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Project roles successfully retrieved.", response=Member.class, responseContainer="List"),
		    @ApiResponse(code = 400, message = "Invalid data supplied."),
		    @ApiResponse(code = 403, message = "Not authorized to retrieve roles for this project."),
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
	@Path("/{id}/role")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRoles(
			@PathParam("id") final int projectId
			) throws Exception{
		
		uk.ac.ox.it.ords.api.project.model.Project project = ProjectService.Factory.getInstance().getProject(projectId);
		
		if (project == null){
			return Response.status(404).build();
		}
		
		if (project.isDeleted()){
			return Response.status(Status.GONE).build();
		}
		
		if (project.isPrivateProject()){
			if (!SecurityUtils.getSubject().isPermitted(ProjectPermissions.PROJECT_VIEW(projectId))){
				ProjectAuditService.Factory.getInstance().createNotAuthRecord("project:view", projectId);
				return Response.status(403).build();
			}
		}
		
		return Response.ok(ProjectRoleService.Factory.getInstance().getProjectMembers(projectId)).build();
	}

	@ApiOperation(value="Create a member role in a project")
	@ApiResponses(value = { 
			@ApiResponse(code = 201, message = "Project role successfully created.",					
					responseHeaders = @ResponseHeader(name = "Location", description = "The URI of the project role", response = URI.class)
					),
		    @ApiResponse(code = 400, message = "Invalid data supplied."),
		    @ApiResponse(code = 403, message = "Not authorized to create a role for this project."),
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
	@Path("/{id}/role")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addRole(
				UserRole role,
				@PathParam("id") final int projectId,
				@Context UriInfo uriInfo
			) throws Exception{
		
		uk.ac.ox.it.ords.api.project.model.Project project = ProjectService.Factory.getInstance().getProject(projectId);
		
		if (project == null) {
			return Response.status(404).build();
		}
		
		if (project.isDeleted()){
			return Response.status(Status.GONE).build();
		}
		
		if (!SecurityUtils.getSubject().isPermitted(ProjectPermissions.PROJECT_MODIFY(projectId))){
			ProjectAuditService.Factory.getInstance().createNotAuthRecord("project:modify", projectId);
			return Response.status(403).build();
		}
		
		role = ProjectRoleService.Factory.getInstance().addUserRoleToProject(projectId, role);
			
	    UriBuilder builder = uriInfo.getAbsolutePathBuilder();
	    builder.path(Integer.toString(role.getId()));
	    return Response.created(builder.build()).build();
	}
	
	@ApiOperation(value="Remove a member role from a project")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Project role successfully deleted."),
		    @ApiResponse(code = 400, message = "Invalid data supplied."),
		    @ApiResponse(code = 403, message = "Not authorized to delete this role."),
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
	@Path("/{projectId}/role/{roleId}")
	@DELETE
	public Response deleteRoleFromProject(
				@PathParam("projectId") final int projectId,
				@PathParam("roleId") final int roleId
			) throws Exception{
		
		uk.ac.ox.it.ords.api.project.model.Project project = ProjectService.Factory.getInstance().getProject(projectId);

		if (project == null) {
			return Response.status(404).build();
		}
		
		if (project.isDeleted()){
			return Response.status(Status.GONE).build();
		}
		
		if (!SecurityUtils.getSubject().isPermitted(ProjectPermissions.PROJECT_MODIFY(projectId))){
			ProjectAuditService.Factory.getInstance().createNotAuthRecord("project:modify", projectId);
			return Response.status(403).build();
		}
		
		UserRole userRole = ProjectRoleService.Factory.getInstance().getUserRole(roleId);
		
		if (userRole == null){
			return Response.status(404).build();
		}
		
		ProjectRoleService.Factory.getInstance().removeUserFromRoleInProject(projectId, roleId);
		return Response.ok().build();
	}

}
