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

import uk.ac.ox.it.ords.api.project.model.Invitation;
import uk.ac.ox.it.ords.api.project.permissions.ProjectPermissions;
import uk.ac.ox.it.ords.api.project.services.ProjectInvitationService;
import uk.ac.ox.it.ords.api.project.services.ProjectService;

public class ProjectInvitation {

	@Path("/project/{id}/invitation/{invitationid}")
	@DELETE
	public Response deleteInvitation(
			@PathParam("id") final int projectId,
			@PathParam("invitationid") final int invitationId
			) throws Exception{

		uk.ac.ox.it.ords.api.project.model.Project project = ProjectService.Factory.getInstance().getProject(projectId);
		uk.ac.ox.it.ords.api.project.model.Invitation invitation = ProjectInvitationService.Factory.getInstance().getInvitation(invitationId);

		if (project == null){
			throw new NotFoundException();
		}

		if (project.isDeleted()){
			return Response.status(Status.GONE).build();
		}
		
		if (invitation == null){
			throw new NotFoundException();
		}
		
		if (!SecurityUtils.getSubject().isPermitted(ProjectPermissions.PROJECT_MODIFY(projectId))){
			throw new ForbiddenException();
		}

		if (project.getProjectId() != invitation.getProjectId()){
			throw new BadRequestException();
		}

		ProjectInvitationService.Factory.getInstance().deleteInvitation(invitation);
		return Response.ok().build();
	}

	@Path("/project/{id}/invitation")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getInvitations(
			@PathParam("id") final int projectId) throws Exception{

		uk.ac.ox.it.ords.api.project.model.Project project = ProjectService.Factory.getInstance().getProject(projectId);

		if (project == null){
			throw new NotFoundException();
		}

		if (project.isDeleted()){
			return Response.status(Status.GONE).build();
		}

		if (!SecurityUtils.getSubject().isPermitted(ProjectPermissions.PROJECT_VIEW_INVITATIONS(projectId))){
			throw new ForbiddenException();
		}

		List<Invitation> invitations = ProjectInvitationService.Factory.getInstance().getInvitations(projectId);
		return Response.ok(invitations).build();
	}

	@Path("/project/{id}/invitation/{invitationid}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getInvitations(
			@PathParam("id") final int projectId,
			@PathParam("invitationid") final int invitationId
			) throws Exception{

		uk.ac.ox.it.ords.api.project.model.Project project = ProjectService.Factory.getInstance().getProject(projectId);
		Invitation invitation = ProjectInvitationService.Factory.getInstance().getInvitation(invitationId);
		
		if (project == null){
			throw new NotFoundException();
		}

		if (project.isDeleted()){
			return Response.status(Status.GONE).build();
		}
		
		if (invitation == null){
			throw new NotFoundException();
		}

		if (!SecurityUtils.getSubject().isPermitted(ProjectPermissions.PROJECT_VIEW_INVITATIONS(projectId))){
			throw new ForbiddenException();
		}

		//
		// Check for cross-resource attack
		//
		if (invitation.getProjectId() != projectId){
			throw new BadRequestException();
		}

		return Response.ok(invitation).build();

	}



	@Path("/project/{id}/invitation")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createInvitation(
			Invitation invitation,
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
			throw new ForbiddenException();
		}

		if (project.getProjectId() != invitation.getProjectId()){
			throw new BadRequestException();
		}

			invitation = ProjectInvitationService.Factory.getInstance().createInvitation(invitation);
			UriBuilder builder = uriInfo.getAbsolutePathBuilder();
			builder.path(Integer.toString(invitation.getId()));
			return Response.created(builder.build()).build();
	}

}
