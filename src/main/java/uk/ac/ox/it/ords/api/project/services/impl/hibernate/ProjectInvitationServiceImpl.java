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
package uk.ac.ox.it.ords.api.project.services.impl.hibernate;

import java.util.List;
import java.util.UUID;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.it.ords.api.project.model.Invitation;
import uk.ac.ox.it.ords.api.project.services.ProjectInvitationService;
import uk.ac.ox.it.ords.api.project.services.impl.AbstractProjectInvitationService;

public class ProjectInvitationServiceImpl extends AbstractProjectInvitationService implements ProjectInvitationService {
	
	private static Logger log = LoggerFactory.getLogger(ProjectInvitationServiceImpl.class);
	
    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

	public ProjectInvitationServiceImpl() {
		setSessionFactory (HibernateUtils.getSessionFactory());
	}

	@Override
	public Invitation createInvitation(Invitation invitation)
			throws Exception {
		Session session = this.sessionFactory.getCurrentSession();
		Transaction transaction = session.beginTransaction();
		try {
			invitation.setUuid(UUID.randomUUID().toString());
			session.save(invitation);
			transaction.commit();
			return invitation;
		} catch (Exception e) {
			log.error("Error creating invitation", e);
			transaction.rollback();
			throw new Exception("Cannot create invitation",e);
		} finally {
			  HibernateUtils.closeSession();
		}
	}

	@Override
	public List<Invitation> getInvitations(int projectId) throws Exception {
		Session session = this.sessionFactory.getCurrentSession();
		Transaction transaction = session.beginTransaction();
		try {
			@SuppressWarnings("unchecked")
			List<Invitation> invitations = session.createCriteria(Invitation.class).add(Restrictions.eq("projectId", projectId)).list();
			transaction.commit();
			return invitations; 
		} catch (Exception e) {
			log.error("Error listing invitations", e);
			transaction.rollback();
			throw new Exception("Cannot list invitations",e);
		} finally {
			  HibernateUtils.closeSession();
		}
	}

	@Override
	public Invitation getInvitation(int invitationId) throws Exception {
		Session session = this.sessionFactory.getCurrentSession();
		Transaction transaction = session.beginTransaction();
		try {
			Invitation invitation = (Invitation) session.get(Invitation.class, invitationId);
			transaction.commit();
			return invitation; 
		} catch (Exception e) {
			log.error("Error getting invitation", e);
			transaction.rollback();
			throw new Exception("Cannot get invitation",e);
		} finally {
			  HibernateUtils.closeSession();
		}
	}

	@Override
	public void deleteInvitation(Invitation invitation) throws Exception {
		Session session = this.sessionFactory.getCurrentSession();
		Transaction transaction = session.beginTransaction();
		try {
			session.delete(invitation);
			transaction.commit();
		} catch (Exception e) {
			log.error("Error getting invitation", e);
			transaction.rollback();
			throw new Exception("Cannot get invitation",e);
		} finally {
			  HibernateUtils.closeSession();
		}
		
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.it.ords.api.project.services.ProjectInvitationService#getInvitationByInviteCode(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Invitation getInvitationByInviteCode(String code) throws Exception {
		Session session = this.sessionFactory.getCurrentSession();
		Transaction transaction = session.beginTransaction();
		List<Invitation> invitations = null;
		try {
			invitations = session.createCriteria(Invitation.class).add(Restrictions.eq("uuid", code)).list();
			transaction.commit();
		} catch (Exception e) {
			log.error("Error listing invitations", e);
			transaction.rollback();
			throw new Exception("Cannot list invitations",e);
		} finally {
			HibernateUtils.closeSession();
		}
		if (invitations != null && !invitations.isEmpty()){
			return invitations.get(0);
		} else {
			return null;
		}
	}

}
