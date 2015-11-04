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

import java.io.IOException;
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

import org.apache.shiro.SecurityUtils;

import uk.ac.ox.it.ords.api.project.permissions.ProjectPermissions;
import uk.ac.ox.it.ords.api.project.services.AuditService;
import uk.ac.ox.it.ords.api.project.services.ProjectService;


public class Project {
	
	Logger log = LoggerFactory.getLogger(Project.class);

	public Project() {
	}
	
	
	@Path("/project")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getProjects(
			@QueryParam("open") final boolean open,
			@QueryParam("full") final boolean full,
			@QueryParam("q") final String q
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

	@Path("/project/{id}")
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
				AuditService.Factory.getInstance().createNotAuthRecord("project:view", id);
				return Response.status(403).build();
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
			) throws Exception{
		
		uk.ac.ox.it.ords.api.project.model.Project project = ProjectService.Factory.getInstance().getProject(id);
		
		if (project == null){
			return Response.status(404).build();
		} 
		
		if (project.isDeleted()){
			return Response.status(Status.GONE).build();
		}
		
		if (!SecurityUtils.getSubject().isPermitted(ProjectPermissions.PROJECT_DELETE(id))){
			AuditService.Factory.getInstance().createNotAuthRecord("project:delete", id);
			return Response.status(403).build();
		}

		ProjectService.Factory.getInstance().deleteProject(id);
		return Response.ok().build();
	}
	
	@Path("/project/{id}")
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
				AuditService.Factory.getInstance().createNotAuthRecord("project:upgrade", id);
				return Response.status(403).build();
			}
		}

		if (!SecurityUtils.getSubject().isPermitted(ProjectPermissions.PROJECT_MODIFY(id))){
			AuditService.Factory.getInstance().createNotAuthRecord("project:update", id);
			return Response.status(403).build();
		}
		
		//
		// Fulfill update request
		//
		uk.ac.ox.it.ords.api.project.model.Project updatedProject = ProjectService.Factory.getInstance().updateProject(project);
		return Response.ok(updatedProject).build();
	}

	@Path("/project/")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createProject(
			uk.ac.ox.it.ords.api.project.model.Project project,
			@Context UriInfo uriInfo
			) throws Exception {

		if (!SecurityUtils.getSubject().isPermitted(ProjectPermissions.PROJECT_CREATE) && !SecurityUtils.getSubject().isPermitted(ProjectPermissions.PROJECT_CREATE_FULL)){
			AuditService.Factory.getInstance().createNotAuthRecord("project:create", -1);
			return Response.status(403).build();
		}

		if (project == null){
			return Response.status(400).build();
		}
		
		//
		// Only users with the project:create-full permission can create a new project that has TrialProject set to False
		//
		if (project.isTrialProject() == false && !SecurityUtils.getSubject().isPermitted(ProjectPermissions.PROJECT_CREATE_FULL)){
			AuditService.Factory.getInstance().createNotAuthRecord("project:create-full", -1);
			return Response.status(403).build();
		}

		ProjectService.Factory.getInstance().createProject(project);		
	    UriBuilder builder = uriInfo.getAbsolutePathBuilder();
	    builder.path(Integer.toString(project.getProjectId()));
	    return Response.created(builder.build()).build();
	}


}
