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
package uk.ac.ox.it.ords.api.project.services;

import static org.junit.Assert.*;

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
