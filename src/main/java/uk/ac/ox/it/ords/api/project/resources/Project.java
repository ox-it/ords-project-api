package uk.ac.ox.it.ords.api.project.resources;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.apache.shiro.SecurityUtils;

import uk.ac.ox.it.ords.api.project.services.ProjectService;


public class Project {

	public Project() {
	}

	@Path("/project/{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getProject(
			@PathParam("id") final int id, 
			@Context HttpServletResponse response
			) throws IOException{
		
		
		uk.ac.ox.it.ords.api.project.model.Project project = ProjectService.Factory.getInstance().getProject(id);
		
		if (project == null){
			throw new NotFoundException();
		}
		
		if (project.isPrivateProject()){
			if (!SecurityUtils.getSubject().isPermitted("project:view:"+id)){
				throw new ForbiddenException();
			}
		}
		if (project.isDeleted()){
			return Response.status(Status.GONE).build();
		}
		
		return Response.ok(project).build();
	}

	@Path("/project/{id}")
	@DELETE
	public Response deleteProject(
			@PathParam("id") final int id, 
			@Context SecurityContext securityContext
			) throws IOException{
		
		
		uk.ac.ox.it.ords.api.project.model.Project project = ProjectService.Factory.getInstance().getProject(id);
		
		if (project == null){
			throw new NotFoundException();
		} 
		
		if (!SecurityUtils.getSubject().isPermitted("project:delete:"+id)){
			throw new ForbiddenException();
		}
		
		try {
			ProjectService.Factory.getInstance().deleteProject(id);
		} catch (Exception e) {
			throw new InternalServerErrorException();
		}
		return Response.ok().build();
	}

	@Path("/project/")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public uk.ac.ox.it.ords.api.project.model.Project createProject(
			uk.ac.ox.it.ords.api.project.model.Project project
			) throws IOException {

		if (!SecurityUtils.getSubject().isPermitted("project:create")){
			throw new ForbiddenException();
		}

		if (project == null){
			throw new BadRequestException();
		}

		try {
			ProjectService.Factory.getInstance().createProject(project);
			return project;
		} catch (Exception e) {
			e.printStackTrace();
			throw new BadRequestException();
		}
	}


}
