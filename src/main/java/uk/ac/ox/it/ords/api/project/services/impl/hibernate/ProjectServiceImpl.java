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

import java.util.ArrayList;
import java.util.List;
//import java.util.stream.Collectors;



import org.apache.shiro.SecurityUtils;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.it.ords.api.project.model.Project;
import uk.ac.ox.it.ords.api.project.server.ValidationException;
import uk.ac.ox.it.ords.api.project.services.ProjectAuditService;
import uk.ac.ox.it.ords.api.project.services.ProjectRoleService;
import uk.ac.ox.it.ords.api.project.services.ProjectService;
import uk.ac.ox.it.ords.api.project.services.impl.AbstractProjectServiceImpl;
import uk.ac.ox.it.ords.security.services.ServerConfigurationService;

/**
 * Hibernate implementation of the ProjectService
 */
public class ProjectServiceImpl extends AbstractProjectServiceImpl implements ProjectService{

	private static Logger log = LoggerFactory.getLogger(ProjectServiceImpl.class);
	
    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

	public ProjectServiceImpl() {
		setSessionFactory (HibernateUtils.getSessionFactory());
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.it.ords.api.project.services.ProjectService#getProject(int)
	 */
	public Project getProject(int id) {
		Session session = this.sessionFactory.getCurrentSession();
		Project project = null;
		try {
			session.beginTransaction();
			project = (Project) session.get(Project.class, id);
			session.getTransaction().commit();
		} catch (Exception e) {
			log.error("Error obtaining Project with id "+id, e);
			session.getTransaction().rollback();
			throw e;
		} finally {
			  HibernateUtils.closeSession();
		}
		return project;
	} 
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.it.ords.api.project.services.ProjectService#createProject(uk.ac.ox.it.ords.api.project.model.Project)
	 */
	public void createProject(Project project) throws ValidationException, Exception{
		
		if (project == null) throw new Exception("Cannot create project: null");
		
		//
		// Allocate to a server
		//
		String server = ServerConfigurationService.Factory.getInstance().getDatabaseServer().getHost();
		if (server == null){
			throw new Exception("No servers available");
		}
		
		//
		// TODO sort out ODBC info
		// project.setOdbcConnectionURL(ODBCUtils.createOdbcConnectionString(details.serverName));
		
		//
		// Save the project
		//
		Session session = this.sessionFactory.getCurrentSession();
		Transaction transaction = session.beginTransaction();
		try {
			configureProject(project);
			project.setDbServerAddress(server);
			validate(project);
			session.save(project);
			transaction.commit();
			ProjectAuditService.Factory.getInstance().createProject(project.getName(), project.getProjectId());
		} catch (HibernateException e) {
			log.error("Error creating Project", e);
			transaction.rollback();
			throw new Exception("Cannot create project",e);
		} finally {
			  HibernateUtils.closeSession();
		}
		//
		// Also create the owner role and permissions
		//
		ProjectRoleService.Factory.getInstance().createInitialPermissions(project.getProjectId());

	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.it.ords.api.project.services.ProjectService#deleteProject(int)
	 */
	public void deleteProject(int id) throws Exception {
		Session session = this.sessionFactory.getCurrentSession();
		try {

			session.beginTransaction();
			
			Project project = (Project) session.get(Project.class, id);
			project.setDeleted(true);
			session.update(project);
			session.getTransaction().commit();
			ProjectAuditService.Factory.getInstance().deleteProject(project.getName(), project.getProjectId());

			//
			// delete the owner role and permissions
			//
			ProjectRoleService.Factory.getInstance().deletePermissions(id);
			
		} catch (Exception e) {
			log.error("Error deleting Project", e);
			session.getTransaction().rollback();
			throw new Exception("Cannot delete project",e);
		} finally {
			  HibernateUtils.closeSession();
		}
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.it.ords.api.project.services.ProjectService#getProjects()
	 */
	@Override
	public List<Project> getProjects() {

		List<Project> projects = getProjectList();
		//
		// Filter for projects where the current user has a role
		//
		List<Project> myProjects = new ArrayList<Project>();
		for (Project project : projects){
			if (
					SecurityUtils.getSubject().hasRole("owner_"+project.getProjectId())
					||  SecurityUtils.getSubject().hasRole("contributor_"+project.getProjectId())
					||  SecurityUtils.getSubject().hasRole("viewer_"+project.getProjectId())		
				){
				myProjects.add(project);
			}
				
		}
		
		return myProjects;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.it.ords.api.project.services.ProjectService#getProjects()
	 */
	@Override
	public List<Project> getOwnProjects() {

		List<Project> projects = getProjectList();
		//
		// Filter for projects where the current user has a role
		//
		List<Project> myProjects = new ArrayList<Project>();
		for (Project project : projects){
			if (
					SecurityUtils.getSubject().hasRole("owner_"+project.getProjectId())		
				){
				myProjects.add(project);
			}
				
		}
		
		return myProjects;
	}
	
	@SuppressWarnings("unchecked")
	private List<Project> getProjectList(){
		Session session = this.sessionFactory.getCurrentSession();
		List<Project> projects = null;
		
		//
		// Obtain projects
		//
		try {
			session.beginTransaction();
			projects = session.createCriteria(Project.class)
					.add(Restrictions.eq("deleted", false)).list();
			session.getTransaction().commit();
		} catch (Exception e) {
			log.error("Error getting project list", e);
			session.getTransaction().rollback();
			throw e;
		} finally {
			  HibernateUtils.closeSession();
		}
		return projects;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.it.ords.api.project.services.ProjectService#getFullProjects()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Project> getAllProjects() {
		Session session = this.sessionFactory.getCurrentSession();
		List<Project> visibleProjects = null;
		try {
			session.beginTransaction();
			List<Project> projects = session.createCriteria(Project.class)
					.list();
			session.getTransaction().commit();
			
			//
			// We now need to filter out any projects that the
			// current user isn't allowed to see
			//	
			visibleProjects = filterProjectsForVisible(projects);
			
		} catch (Exception e) {
			log.error("Error getting project list", e);
			session.getTransaction().rollback();
			throw e;
		} finally {
			  HibernateUtils.closeSession();
		}
		return visibleProjects;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.it.ords.api.project.services.ProjectService#getOpenProjects()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Project> getOpenProjects() {
		Session session = this.sessionFactory.getCurrentSession();
		List<Project> projects = null;
		try {
			session.beginTransaction();
			projects = session.createCriteria(Project.class)
					.add(Restrictions.eq("deleted", Boolean.FALSE))
					.add(Restrictions.eq("trialProject", Boolean.FALSE))
					.add(Restrictions.eq("privateProject", Boolean.FALSE))
					.list();
			session.getTransaction().commit();
		} catch (Exception e) {
			log.error("Error getting project list", e);
			session.getTransaction().rollback();
			throw e;
		} finally {
			  HibernateUtils.closeSession();
		}
		return projects;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.it.ords.api.project.services.ProjectService#updateProject(uk.ac.ox.it.ords.api.project.model.Project)
	 */
	@Override
	public Project updateProject(Project project) throws Exception {
		Session session = this.sessionFactory.getCurrentSession();
		try {
			session.beginTransaction();
			validate(project);
			session.update(project);
			session.getTransaction().commit();
			ProjectAuditService.Factory.getInstance().updateProject(project.getName(), project.getProjectId());
			return project;
		} catch (Exception e) {
			log.error("Error updating project", e);
			session.getTransaction().rollback();
			throw e;
		} finally {
			HibernateUtils.closeSession();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Project> searchProjects(String searchTerms) {
		List<Project> projects;

		//
        // If the search has not specified any terms they will just get all open projects.
		//
		if ((searchTerms == null) || (searchTerms.trim().length() == 0)) {
			return getOpenProjects();
		}

		/*
	     * Since the user has specified search terms, we need to display all projects
	     * that match the search terms along with all datasets that match the search terms.
		 */
		
		//
		// Get matching projects
		//
		Session session = this.sessionFactory.getCurrentSession();
		projects = null;
		try {
			session.beginTransaction();
			
			Criteria searchCriteria = session.createCriteria(Project.class)
					.add(Restrictions.eq("trialProject", Boolean.FALSE))
					.add(Restrictions.eq("deleted", Boolean.FALSE))
					.add(Restrictions.eq("privateProject", Boolean.FALSE));
			String[] terms = searchTerms.split(",");
			
			for (String term : terms) {
				searchCriteria.add(
						Restrictions.and(
								Restrictions.or(
										Restrictions.ilike("name", "%"+term.trim()+"%"),
										Restrictions.ilike("description", "%"+term.trim()+"%")
										)
								)
						);		
			}
			
			projects = searchCriteria.list();
			session.getTransaction().commit();
			
		} catch (Exception e) {
			log.error("Error getting project list", e);
			session.getTransaction().rollback();
			throw e;
		} finally {
			  HibernateUtils.closeSession();
		}		
		
		//
		// TODO get matching datasets
		//

		//
		// TODO merge
		//

		//
		// We now need to filter out any projects that the
		// current user isn't allowed to see
		//	
		List<Project> visibleProjects = filterProjectsForVisible(projects);

		return visibleProjects;
	}

}
