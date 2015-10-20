package uk.ac.ox.it.ords.api.project.resources;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
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

import org.apache.shiro.SecurityUtils;

import uk.ac.ox.it.ords.api.project.services.ProjectService;


public class Project {

	public Project() {
	}
	
	
	@Path("/project")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getProjects(
			@QueryParam("open") final boolean open,
			@QueryParam("full") final boolean full
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
			
				//
				// The projects the user is involved in as a member.
				//
				projects = ProjectService.Factory.getInstance().getProjects();
			}
		}
		return Response.ok(projects).build();
		
		
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
		
		if (project.isDeleted()){
			return Response.status(Status.GONE).build();
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
	
	@Path("/project/{id}")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateProject(
			uk.ac.ox.it.ords.api.project.model.Project project,
			@PathParam("id") final int id 
			) throws IOException {
		
		uk.ac.ox.it.ords.api.project.model.Project oldProject = ProjectService.Factory.getInstance().getProject(id);
		
		if (project == null){
			throw new NotFoundException();
		}
		
		if (project.isDeleted()){
			return Response.status(Status.GONE).build();
		}
		
		//
		// Prevent side-modification hacks
		//
		if (id != project.getProjectId()){
			throw new BadRequestException();
		}
		
		//
		// Is this an upgrade request?
		//
		if (oldProject.isTrialProject() == true && project.isTrialProject() == false){
			if (!SecurityUtils.getSubject().isPermitted("project:upgrade")){
				throw new ForbiddenException();
			}
			try {
				ProjectService.Factory.getInstance().upgradeProject(id);
				uk.ac.ox.it.ords.api.project.model.Project upgradedProject = ProjectService.Factory.getInstance().getProject(id);
				System.out.println("returning project "+id);
				System.out.println(upgradedProject.isTrialProject());
				return Response.ok(upgradedProject).build();
			} catch (Exception e) {
				throw new InternalServerErrorException();
			}

		//
		// This is an update request
		//
		} else {
			if (!SecurityUtils.getSubject().isPermitted("project:update:"+id)){
				throw new ForbiddenException();
			}
			try {
				uk.ac.ox.it.ords.api.project.model.Project updatedProject = ProjectService.Factory.getInstance().updateProject(project);
				return Response.ok(updatedProject).build();
			} catch (Exception e) {
				throw new InternalServerErrorException();
			}
		}
		

		
		
	}

	@Path("/project/")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createProject(
			uk.ac.ox.it.ords.api.project.model.Project project,
			@Context UriInfo uriInfo
			) throws IOException {

		if (!SecurityUtils.getSubject().isPermitted("project:create") && !SecurityUtils.getSubject().isPermitted("project:create-full")){
			throw new ForbiddenException();
		}

		if (project == null){
			throw new BadRequestException();
		}
		
		if (project.isTrialProject() == false && !SecurityUtils.getSubject().isPermitted("project:create-full")){
			throw new ForbiddenException();
		}

		try {
			ProjectService.Factory.getInstance().createProject(project);
			
	        UriBuilder builder = uriInfo.getAbsolutePathBuilder();
	        builder.path(Integer.toString(project.getProjectId()));
	        return Response.created(builder.build()).build();
		} catch (Exception e) {
			e.printStackTrace();
			throw new BadRequestException();
		}
	}


}
