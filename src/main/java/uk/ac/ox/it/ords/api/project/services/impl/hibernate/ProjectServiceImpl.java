package uk.ac.ox.it.ords.api.project.services.impl.hibernate;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.shiro.SecurityUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.it.ords.api.project.model.Project;
import uk.ac.ox.it.ords.api.project.services.ProjectRoleService;
import uk.ac.ox.it.ords.api.project.services.ProjectService;

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
	public void createProject(Project project) throws Exception{
		
		if (project == null) throw new Exception("Cannot create project: null");
		
		Session session = this.sessionFactory.getCurrentSession();
		Transaction transaction = session.beginTransaction();
		try {
			configureProject(project);
			validateProject(project);
			session.save(project);
			transaction.commit();
		} catch (Exception e) {
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
			//
			// delete the owner role and permissions
			//
			ProjectRoleService.Factory.getInstance().deletePermissions(id);
			
		} catch (Exception e) {
			log.error("Error creating Project", e);
			session.getTransaction().rollback();
			throw new Exception("Cannot create project",e);
		} finally {
			  HibernateUtils.closeSession();
		}
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.it.ords.api.project.services.ProjectService#getProjects()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Project> getProjects() {
		Session session = this.sessionFactory.getCurrentSession();
		List<Project> projects = null;
		List<Project> myProjects = null;
		try {
			session.beginTransaction();
			projects = session.createCriteria(Project.class)
					.add(Restrictions.eq("deleted", false)).list();
			session.getTransaction().commit();
			
			//
			// 
			//
			myProjects = projects.stream()
					.filter(p -> 
							SecurityUtils.getSubject().hasRole("owner_"+p.getProjectId())
						||  SecurityUtils.getSubject().hasRole("contributor_"+p.getProjectId())
						||  SecurityUtils.getSubject().hasRole("viewer_"+p.getProjectId())		
							)
					.collect(Collectors.toList());
			
		} catch (Exception e) {
			log.error("Error getting project list", e);
			session.getTransaction().rollback();
			throw e;
		} finally {
			  HibernateUtils.closeSession();
		}
		return myProjects;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.it.ords.api.project.services.ProjectService#getFullProjects()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Project> getFullProjects() {
		Session session = this.sessionFactory.getCurrentSession();
		List<Project> visibleProjects = null;
		try {
			session.beginTransaction();
			List<Project> projects = session.createCriteria(Project.class)
					.add(Restrictions.eq("deleted", false))
					.add(Restrictions.eq("trialProject", false))
					.list();
			session.getTransaction().commit();
			
			//
			// We now need to filter out any projects that the
			// current user isn't allowed to see
			//			
			visibleProjects = projects.stream()
					.filter(p -> 
							   !p.isPrivateProject() 
							|| 
								SecurityUtils.getSubject().isPermitted("project:view:"+p.getProjectId())
							)
					.collect(Collectors.toList());
			
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
			validateProject(project);
			session.update(project);
			session.getTransaction().commit();
			return project;
		} catch (Exception e) {
			log.error("Error updating project", e);
			session.getTransaction().rollback();
			throw e;
		} finally {
			HibernateUtils.closeSession();
		}
	}
}
