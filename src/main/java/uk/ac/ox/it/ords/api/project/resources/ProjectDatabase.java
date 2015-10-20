package uk.ac.ox.it.ords.api.project.resources;

import java.util.List;

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
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.apache.shiro.SecurityUtils;

import uk.ac.ox.it.ords.api.project.services.ProjectDatabaseService;
import uk.ac.ox.it.ords.api.project.services.ProjectService;

public class ProjectDatabase {

	
	@Path("/project/{id}/database/{db}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDatabaseForProject(
			@PathParam("id") final int id,
			@PathParam("db") final int db
			){
		
		uk.ac.ox.it.ords.api.project.model.Project project = ProjectService.Factory.getInstance().getProject(id);
		
		if (project == null){
			throw new NotFoundException();
		}
		
		if (project.isDeleted()){
			return Response.status(Status.GONE).build();
		}
		
		if (project.isPrivateProject() && !SecurityUtils.getSubject().isPermitted("project:view:"+id)){
			throw new ForbiddenException();
		}
		
		uk.ac.ox.it.ords.api.project.model.ProjectDatabase database;
		
		try {
			database = ProjectDatabaseService.Factory.getInstance().getDatabaseForProject(db);
		} catch (Exception e) {
			throw new InternalServerErrorException();
		}	
		
		if (database == null){
			throw new NotFoundException();
		}
		
		return Response.ok(database).build();
	}
	
	@Path("/project/{id}/database")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDatabasesForProject(
			@PathParam("id") final int id
			){
		
		uk.ac.ox.it.ords.api.project.model.Project project = ProjectService.Factory.getInstance().getProject(id);
		
		if (project == null){
			throw new NotFoundException();
		}
		
		if (project.isDeleted()){
			return Response.status(Status.GONE).build();
		}
		
		if (project.isPrivateProject() && !SecurityUtils.getSubject().isPermitted("project:view:"+id)){
			throw new ForbiddenException();
		}
		
		try {
			List<uk.ac.ox.it.ords.api.project.model.ProjectDatabase> databases = ProjectDatabaseService.Factory.getInstance().getDatabasesForProject(id);
			return Response.ok(databases).build();
		} catch (Exception e) {
			throw new InternalServerErrorException();
		}		
	}
	
	@Path("/project/{id}/database")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addDatabaseToProject(
			@PathParam("id") final int id,
			uk.ac.ox.it.ords.api.project.model.ProjectDatabase database,
			@Context UriInfo uriInfo
			){
		
		uk.ac.ox.it.ords.api.project.model.Project project = ProjectService.Factory.getInstance().getProject(id);
		
		if (project == null){
			throw new NotFoundException();
		}
		
		if (project.isDeleted()){
			return Response.status(Status.GONE).build();
		}
		
		if (!SecurityUtils.getSubject().isPermitted("project:modify:"+id)){
			throw new ForbiddenException();
		}
		
		//
		// Prevent side-attack; ensure the database belongs to the project specified
		//
		if (database.getProjectId() != project.getProjectId()){
			throw new BadRequestException();
		}
		
		try {
			
			database = ProjectDatabaseService.Factory.getInstance().addDatabaseToProject(id, database);
			
	        UriBuilder builder = uriInfo.getAbsolutePathBuilder();
	        builder.path(Integer.toString(database.getProjectDatabaseId()));
	        return Response.created(builder.build()).build();
			
		} catch (Exception e) {
			throw new InternalServerErrorException();
		}
	}
	
	@Path("/project/{id}/database/{db}")
	@DELETE
	public Response removeDatabaseFromProject(
			@PathParam("id") final int id,
			@PathParam("db") final int db
			){
		
		uk.ac.ox.it.ords.api.project.model.Project project = ProjectService.Factory.getInstance().getProject(id);
		
		if (project == null){
			throw new NotFoundException();
		}
		
		if (project.isDeleted()){
			return Response.status(Status.GONE).build();
		}
		
		if (!SecurityUtils.getSubject().isPermitted("project:modify:"+id)){
			throw new ForbiddenException();
		}
		
		uk.ac.ox.it.ords.api.project.model.ProjectDatabase database = null; 
		
		try {
			database = ProjectDatabaseService.Factory.getInstance().getDatabaseForProject(db);
		} catch (Exception e1) {
			throw new InternalServerErrorException();
		}
		
		if (database == null){
			throw new NotFoundException();
		}
		
		//
		// Prevent side-attack; ensure the database belongs to the project specified
		//
		if (database.getProjectId() != id){
			throw new BadRequestException();
		}
		
		try {
			ProjectDatabaseService.Factory.getInstance().removeDatabaseFromProject(id, db);
			return Response.ok().build();
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalServerErrorException();
		}
		
	}

}
