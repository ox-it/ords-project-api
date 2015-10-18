package uk.ac.ox.it.ords.api.project.services;

import org.junit.Test;

import uk.ac.ox.it.ords.api.project.model.Project;
import uk.ac.ox.it.ords.api.project.services.impl.hibernate.HibernateUtils;

public class ProjectServiceTest {

	@Test (expected = Exception.class)
	public void testCreateNullProject() throws Exception{
		ProjectService.Factory.getInstance().createProject(null);
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

}
