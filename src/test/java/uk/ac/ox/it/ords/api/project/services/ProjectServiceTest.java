package uk.ac.ox.it.ords.api.project.services;

import static org.junit.Assert.*;

import org.hibernate.LockOptions;
import org.junit.Test;

import uk.ac.ox.it.ords.api.project.model.Project;
import uk.ac.ox.it.ords.api.project.services.impl.hibernate.HibernateUtils;

public class ProjectServiceTest {

	@Test (expected = Exception.class)
	public void testCreateNullProject() throws Exception{
		ProjectService.Factory.getInstance().createProject(null);
	}

	@Test
	public void testNullProjects() throws Exception{
		assertNull(ProjectService.Factory.getInstance().getProject(9999));
	}

	@Test (expected = Exception.class)
	public void testCreateInvalidProject() throws Exception{
		Project project = new Project();
		project.setDescription("test");
		ProjectService.Factory.getInstance().createProject(project);
	}

	@Test (expected = Exception.class)
	public void testCreateProjectException() throws Exception{
		try {
			Project project = new Project();
			project.setDescription("test");
			HibernateUtils.getSessionFactory().getCurrentSession().beginTransaction();
			ProjectService.Factory.getInstance().createProject(project);
		} catch (Exception e) {
			HibernateUtils.closeSession();			
			throw e;
		}
	}

	@Test (expected = Exception.class)
	public void testGetProjectException() throws Exception{
		try {
			HibernateUtils.getSessionFactory().getCurrentSession().beginTransaction();
			ProjectService.Factory.getInstance().getProject(9999);
		} catch (Exception e) {
			HibernateUtils.closeSession();			
			throw e;
		}
	}
	
	@Test (expected = Exception.class)
	public void testDeleteProjectException() throws Exception{
		try {
			HibernateUtils.getSessionFactory().getCurrentSession().beginTransaction();
			ProjectService.Factory.getInstance().deleteProject(9999);
		} catch (Exception e) {
			HibernateUtils.closeSession();			
			throw e;
		}
	}
	
	@Test (expected = Exception.class)
	public void testUpgradeProjectException() throws Exception{	
		try {
			ProjectService.Factory.getInstance().upgradeProject(9999);
		} catch (Exception e) {
			e.printStackTrace();
			HibernateUtils.closeSession();			
			throw e;
		}
	}
	
	@Test (expected = Exception.class)
	public void testUpdateProjectException() throws Exception{
		try {
			HibernateUtils.getSessionFactory().getCurrentSession().beginTransaction();
			ProjectService.Factory.getInstance().updateProject(null);
		} catch (Exception e) {
			HibernateUtils.closeSession();			
			throw e;
		}
	}

	@Test (expected = Exception.class)
	public void testListProjectsException() throws Exception{
		try {
			HibernateUtils.getSessionFactory().getCurrentSession().beginTransaction();
			ProjectService.Factory.getInstance().getProjects();
		} catch (Exception e) {
			HibernateUtils.closeSession();			
			throw e;
		}
	}
	
	@Test (expected = Exception.class)
	public void testListFullProjectException() throws Exception{
		try {
			HibernateUtils.getSessionFactory().getCurrentSession().beginTransaction();
			ProjectService.Factory.getInstance().getFullProjects();
		} catch (Exception e) {
			HibernateUtils.closeSession();			
			throw e;
		}
	}
	
	@Test (expected = Exception.class)
	public void testListOpenProjectException() throws Exception{
		try {
			HibernateUtils.getSessionFactory().getCurrentSession().beginTransaction();
			ProjectService.Factory.getInstance().getOpenProjects();
		} catch (Exception e) {
			HibernateUtils.closeSession();			
			throw e;
		}
	}

}
