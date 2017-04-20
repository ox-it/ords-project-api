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

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.it.ords.api.project.model.User;
import uk.ac.ox.it.ords.api.project.services.UserService;

/**
 * This is a helper service and needs refactoring out.
 * @author scottw
 *
 */
public class UserServiceImpl implements UserService {
	
	private static Logger log = LoggerFactory.getLogger(UserService.class);

	private SessionFactory sessionFactory;

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}


	public UserServiceImpl() {
		setSessionFactory (HibernateUtils.getSessionFactory());
	}

	public User getUserByPrincipalName(String principalname) throws Exception {		
		Session session = this.sessionFactory.getCurrentSession();
		try {
			session.beginTransaction();
			@SuppressWarnings("unchecked")
			List<User> users = (List<User>) session.createCriteria(User.class).add(Restrictions.eq("principalName", principalname)).list();
			session.getTransaction().commit();
			if (users.size() == 1){
				return users.get(0);
			} 
			return null;
		} catch (Exception e) {
			session.getTransaction().rollback();
			throw e;
		} finally {
			  HibernateUtils.closeSession();
		}
		
	}

	public User getUserByEmailAddress(String email) throws Exception {
		Session session = this.sessionFactory.getCurrentSession();
		try {
			session.beginTransaction();
			@SuppressWarnings("unchecked")
			List<User> users = (List<User>) session.createCriteria(User.class).add(Restrictions.eq("email", email)).list();
			session.getTransaction().commit();
			if (users.size() == 1){
				return users.get(0);
			} 
			return null;
		} catch (Exception e) {
			session.getTransaction().rollback();
			throw e;
		} finally {
			  HibernateUtils.closeSession();
		}
	}

}
