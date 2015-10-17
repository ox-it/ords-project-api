package uk.ac.ox.it.ords.api.project.services.impl.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

public class HibernateUtils
{
	private static SessionFactory sessionFactory;
	private static ServiceRegistry serviceRegistry;
	
	private static void init()
	{
		try
		{
			Configuration configuration = new Configuration().configure();

			serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry();
			sessionFactory = configuration.buildSessionFactory(serviceRegistry);
		}
		catch (HibernateException he)
		{
			System.err.println("Error creating Session: " + he);
			throw new ExceptionInInitializerError(he);
		}
	}

	public static SessionFactory getSessionFactory()
	{
		if (sessionFactory == null) init();
		return sessionFactory;
	} 
}
