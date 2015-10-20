package uk.ac.ox.it.ords.api.project.resources;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
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

import uk.ac.ox.it.ords.api.project.model.UserRole;
import uk.ac.ox.it.ords.api.project.services.ProjectRoleService;
import uk.ac.ox.it.ords.api.project.services.ProjectService;

public class ProjectRole {
	
	@Path("/project/{id}/role")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRoles(
			@PathParam("id") final int projectId
			){
		
		uk.ac.ox.it.ords.api.project.model.Project project = ProjectService.Factory.getInstance().getProject(projectId);
		
		if (project == null){
			throw new NotFoundException();
		}
		
		if (project.isDeleted()){
			return Response.status(Status.GONE).build();
		}
		
		if (project.isPrivateProject()){
			if (!SecurityUtils.getSubject().isPermitted("project:view:" + projectId)){
				throw new ForbiddenException();
			}
		}
		
		try {
			return Response.ok(ProjectRoleService.Factory.getInstance().getUserRolesForProject(projectId)).build();
		} catch (Exception e) {
			return Response.serverError().build();
		}
	}

	@Path("/project/{id}/role")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addRole(
				UserRole role,
				@PathParam("id") final int projectId,
				@Context UriInfo uriInfo
			){
		
		uk.ac.ox.it.ords.api.project.model.Project project = ProjectService.Factory.getInstance().getProject(projectId);
		
		if (!SecurityUtils.getSubject().isPermitted("project:modify:"+projectId)){
			throw new ForbiddenException();
		}
		
		if (project == null) {
			throw new NotFoundException();
		}
		
		if (project.isDeleted()){
			return Response.status(Status.GONE).build();
		}
		
		try {
			role = ProjectRoleService.Factory.getInstance().addUserRoleToProject(projectId, role);
			
	        UriBuilder builder = uriInfo.getAbsolutePathBuilder();
	        builder.path(Integer.toString(role.getId()));
	        return Response.created(builder.build()).build();
		} catch (Exception e) {
			throw new BadRequestException();
		}
	}
	
	@Path("/project/{projectId}/role/{roleId}")
	@DELETE
	public Response deleteRoleFromProject(
				@PathParam("projectId") final int projectId,
				@PathParam("roleId") final int roleId
			){
		
		uk.ac.ox.it.ords.api.project.model.Project project = ProjectService.Factory.getInstance().getProject(projectId);

		if (project == null) {
			throw new NotFoundException();
		}
		
		if (project.isDeleted()){
			return Response.status(Status.GONE).build();
		}
		
		if (!SecurityUtils.getSubject().isPermitted("project:modify:"+projectId)){
			throw new ForbiddenException();
		}
		
		try {
			UserRole userRole = ProjectRoleService.Factory.getInstance().getUserRole(roleId);
			if (userRole == null){
				throw new NotFoundException();				
			}
		} catch (Exception e1) {
			throw new NotFoundException();
		}
		try {
			ProjectRoleService.Factory.getInstance().removeUserFromRoleInProject(projectId, roleId);
			return Response.ok().build();
		} catch (Exception e) {
			throw new BadRequestException();
		}
		
		
		
	}

}
