package uk.ac.ox.it.ords.api.project.services.impl.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.it.ords.api.project.model.Project;
import uk.ac.ox.it.ords.api.project.services.ProjectRoleService;
import uk.ac.ox.it.ords.api.project.services.ProjectService;

public class ProjectServiceImpl extends AbstractProjectServiceImpl implements ProjectService{

	private static Logger log = LoggerFactory.getLogger(ProjectServiceImpl.class);
	
    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

	public ProjectServiceImpl() {
		setSessionFactory (HibernateUtils.getSessionFactory());
	}

	public Project getProject(int id) {
		Session session = this.sessionFactory.getCurrentSession();
		Project project = null;
		try {
			session.beginTransaction();
			project = (Project) session.get(Project.class, id);
			session.getTransaction().commit();
		} catch (HibernateException e) {
			log.error("Error obtaining Project with id "+id, e);
			session.getTransaction().rollback();
		}
		return project;
	} 
	
	public void createProject(Project project) throws Exception{
		Session session = this.sessionFactory.getCurrentSession();
		Transaction transaction = session.beginTransaction();
		try {
			project = configureProject(project);
			session.save(project);
			transaction.commit();
		} catch (HibernateException e) {
			log.error("Error creating Project", e);
			transaction.rollback();
			throw new Exception("Cannot create project",e);
		}
		transaction = null;
		//
		// Also create the owner role and permissions
		//
		ProjectRoleService.Factory.getInstance().createInitialPermissions(project.getProjectId());
	}

	public void deleteProject(int id) throws Exception {
		Session session = this.sessionFactory.getCurrentSession();
		try {

			session.beginTransaction();
			
			Project project = (Project) session.get(Project.class, id);
			project.setDeleted(true);
			session.save(project);
			session.getTransaction().commit();
			//
			// delete the owner role and permissions
			//
			ProjectRoleService.Factory.getInstance().deletePermissions(id);
			
		} catch (HibernateException e) {
			log.error("Error creating Project", e);
			session.getTransaction().rollback();
			throw new Exception("Cannot create project",e);
		}
	}
}
