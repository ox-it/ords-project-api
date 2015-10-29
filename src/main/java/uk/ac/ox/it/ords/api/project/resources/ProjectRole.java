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

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
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

import org.apache.shiro.SecurityUtils;

import uk.ac.ox.it.ords.api.project.model.Member;
import uk.ac.ox.it.ords.api.project.permissions.ProjectPermissions;
import uk.ac.ox.it.ords.api.project.services.AuditService;
import uk.ac.ox.it.ords.api.project.services.ProjectRoleService;
import uk.ac.ox.it.ords.api.project.services.ProjectService;
import uk.ac.ox.it.ords.security.model.UserRole;

public class ProjectRole {

	
	@Path("/project/{projectId}/role/{roleId}")
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
			throw new NotFoundException();
		}
		
		if (project.isDeleted()){
			return Response.status(Status.GONE).build();
		}
		
		if (!SecurityUtils.getSubject().isPermitted(ProjectPermissions.PROJECT_MODIFY(projectId))){
			AuditService.Factory.getInstance().createNotAuthRecord("project:modify", projectId);
			throw new ForbiddenException();
		}

		
		UserRole userRole = ProjectRoleService.Factory.getInstance().getUserRole(roleId);
		
		if (userRole == null){
			throw new NotFoundException();		
		}
		
		//
		// Prevent cross-resource attack
		//
		if(!userRole.getRole().endsWith(String.valueOf(projectId))){
			throw new BadRequestException();
		}
		
		//
		// The only property we can update is the role required
		//
		userRole.setRole(member.getRole());
		
		ProjectRoleService.Factory.getInstance().updateProjectRole(userRole, projectId);
		
		return Response.ok().build();
	}
	
	@Path("/project/{projectId}/role/{roleId}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getProjectRole(
				@PathParam("projectId") final int projectId,
				@PathParam("roleId") final int roleId
			) throws Exception{
		
		uk.ac.ox.it.ords.api.project.model.Project project = ProjectService.Factory.getInstance().getProject(projectId);
		
		if (project == null){
			throw new NotFoundException();
		}
		
		if (project.isDeleted()){
			return Response.status(Status.GONE).build();
		}
		
		if (project.isPrivateProject()){
			if (!SecurityUtils.getSubject().isPermitted(ProjectPermissions.PROJECT_VIEW(projectId))){
				AuditService.Factory.getInstance().createNotAuthRecord("project:view", projectId);
				throw new ForbiddenException();
			}
		}
		
		UserRole userRole = ProjectRoleService.Factory.getInstance().getUserRole(roleId);
		
		if (userRole == null){
			throw new NotFoundException();		
		}
		
		//
		// Prevent cross-resource attack
		//
		if(!userRole.getRole().endsWith(String.valueOf(projectId))){
			throw new BadRequestException();
		}
		
		return Response.ok(new Member(userRole)).build();
		
	}
	
	@Path("/project/{id}/role")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRoles(
			@PathParam("id") final int projectId
			) throws Exception{
		
		uk.ac.ox.it.ords.api.project.model.Project project = ProjectService.Factory.getInstance().getProject(projectId);
		
		if (project == null){
			throw new NotFoundException();
		}
		
		if (project.isDeleted()){
			return Response.status(Status.GONE).build();
		}
		
		if (project.isPrivateProject()){
			if (!SecurityUtils.getSubject().isPermitted(ProjectPermissions.PROJECT_VIEW(projectId))){
				AuditService.Factory.getInstance().createNotAuthRecord("project:view", projectId);
				throw new ForbiddenException();
			}
		}
		
		return Response.ok(ProjectRoleService.Factory.getInstance().getProjectMembers(projectId)).build();
	}

	@Path("/project/{id}/role")
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
			throw new NotFoundException();
		}
		
		if (project.isDeleted()){
			return Response.status(Status.GONE).build();
		}
		
		if (!SecurityUtils.getSubject().isPermitted(ProjectPermissions.PROJECT_MODIFY(projectId))){
			AuditService.Factory.getInstance().createNotAuthRecord("project:modify", projectId);
			throw new ForbiddenException();
		}
		
		role = ProjectRoleService.Factory.getInstance().addUserRoleToProject(projectId, role);
			
	    UriBuilder builder = uriInfo.getAbsolutePathBuilder();
	    builder.path(Integer.toString(role.getId()));
	    return Response.created(builder.build()).build();
	}
	
	@Path("/project/{projectId}/role/{roleId}")
	@DELETE
	public Response deleteRoleFromProject(
				@PathParam("projectId") final int projectId,
				@PathParam("roleId") final int roleId
			) throws Exception{
		
		uk.ac.ox.it.ords.api.project.model.Project project = ProjectService.Factory.getInstance().getProject(projectId);

		if (project == null) {
			throw new NotFoundException();
		}
		
		if (project.isDeleted()){
			return Response.status(Status.GONE).build();
		}
		
		if (!SecurityUtils.getSubject().isPermitted(ProjectPermissions.PROJECT_MODIFY(projectId))){
			AuditService.Factory.getInstance().createNotAuthRecord("project:modify", projectId);
			throw new ForbiddenException();
		}
		
		UserRole userRole = ProjectRoleService.Factory.getInstance().getUserRole(roleId);
		
		if (userRole == null){
			throw new NotFoundException();				
		}
		
		ProjectRoleService.Factory.getInstance().removeUserFromRoleInProject(projectId, roleId);
		return Response.ok().build();
	}

}
