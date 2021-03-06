package ibrokerkit.ibrokerstore.store.impl.db;

import ibrokerkit.ibrokerstore.store.Store;
import ibrokerkit.ibrokerstore.store.StoreException;
import ibrokerkit.ibrokerstore.store.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hibernate-based implementation of the Store interface.
 */
public class DatabaseStore implements Store {

	private static final Logger log = LoggerFactory.getLogger(DatabaseStore.class.getName());

	private Properties properties;
	private Configuration configuration;
	private ServiceRegistry serviceRegistry;
	private SessionFactory sessionFactory;

	public DatabaseStore(Properties properties) {

		this.properties = properties;
		this.configuration = null;
		this.serviceRegistry = null;
		this.sessionFactory = null;
	}

	public void init() throws StoreException {

		log.debug("init()");

		try {

			// prepare Hibernate configuration

			this.configuration = new Configuration();

			this.configuration.setProperties(this.properties);

			this.configuration.addClass(ibrokerkit.ibrokerstore.store.impl.db.DbUser.class);

			// create session factory

			this.initSessionFactory();
		} catch (Exception ex) {

			log.error(ex.getMessage(), ex);
			throw new StoreException("Cannot initialize Hibernate", ex);
		}

		log.debug("Done.");
	}

	private void initSessionFactory() {

		this.serviceRegistry = new ServiceRegistryBuilder().applySettings(this.configuration.getProperties()).buildServiceRegistry();        
		this.sessionFactory = this.configuration.buildSessionFactory(serviceRegistry);
	}

	public boolean isInitialized() {

		return(this.configuration != null && this.serviceRegistry != null && this.sessionFactory != null);
	}

	public void close() {

		log.debug("close()");

		this.serviceRegistry = null;
		this.sessionFactory.close();
		this.sessionFactory = null;
	}

	public boolean isClosed() {

		return(this.sessionFactory == null || this.sessionFactory.isClosed());
	}

	/**
	 * Checks if the database connection is still alive;
	 * if not, try to reconnect, then throw exception.
	 * @return The database connection.
	 */
	public SessionFactory getSessionFactory() throws StoreException {

		if (this.sessionFactory != null && ! this.sessionFactory.isClosed()) return(this.sessionFactory);

		this.initSessionFactory();

		if (this.sessionFactory != null && ! this.sessionFactory.isClosed()) return(this.sessionFactory);

		throw new StoreException("Database not available.");
	}

	/**
	 * Allow the connection to be changed externally.
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {

		this.sessionFactory = sessionFactory;
	}

	/*
	 * Common database methods
	 */

	public void updateObject(Object object) throws StoreException {

		log.debug("updateObject()");

		Session session = this.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		// check object

		if (! (object instanceof DbObject)) throw new StoreException("Object is not from this store.");

		// update object

		try {

			session.update(object);
			session.flush();
			session.refresh(object);
			commit(session);
		} catch (Exception ex) {

			log.error(ex.getMessage(), ex);
			rollback(session);
			throw new StoreException("Cannot access database.", ex);
		}

		// done

		log.debug("Done.");
	}

	public void deleteObject(Object object) throws StoreException {

		log.debug("deleteObject()");

		Session session = this.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		// check object

		if (! (object instanceof DbObject)) throw new StoreException("Object is not from this store.");

		// delete object

		try {

			reattach(session, object);
			session.delete(object);
			session.flush();
			commit(session);
		} catch (Exception ex) {

			log.error(ex.getMessage(), ex);
			rollback(session);
			throw new StoreException("Cannot access database.", ex);
		}
	}

	/*
	 * User methods
	 */

	public User createOrUpdateUser(String identifier, String pass, String recovery, String name, String email, Boolean openid) throws StoreException {

		log.debug("createOrUpdateUser()");

		DbUser user = null;

		Session session = this.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		// create user

		try {

			if (identifier != null) user = DbUser.ByIdentifier(session, identifier);
			if (user == null) user = new DbUser();

			user.setIdentifier(identifier);
			user.setPass(pass);
			user.setRecovery(recovery);
			user.setName(name);
			user.setEmail(email);
			user.setOpenid(openid);

			session.saveOrUpdate(user);
			commit(session);
		} catch (Exception ex) {

			log.error(ex.getMessage(), ex);
			rollback(session);
			throw new StoreException("Cannot access database.", ex);
		}

		// done

		log.debug("Done.");
		return(user);
	}

	public User[] listUsers() throws StoreException {

		log.debug("listUsers()");

		Session session = this.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		// get all users

		List<DbUser> users;

		try {

			users = DbUser.All(session);
			commit(session);
		} catch (Exception ex) {

			log.error(ex.getMessage(), ex);
			rollback(session);
			throw new StoreException("Cannot access database.", ex);
		}

		// build result list

		User[] result = new ArrayList<DbUser> (users).toArray(new User[users.size()]);

		// done

		log.debug("Done.");
		return(result);
	}

	public User findUser(String identifier) throws StoreException {

		log.debug("findUser()");

		DbUser user;

		Session session = this.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		// get user

		try {

			user = DbUser.ByIdentifier(session, identifier);
			commit(session);
		} catch (Exception ex) {

			log.error(ex.getMessage(), ex);
			rollback(session);
			throw new StoreException("Cannot access database.", ex);
		}

		// done

		log.debug("Done.");
		return(user);
	}

	public User[] findUsersByEmail(String email) throws StoreException {

		log.debug("findUsersByEmail()");

		Session session = this.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		// get user

		List<DbUser> users;

		try {

			users = DbUser.ByEmail(session, email);
			commit(session);
		} catch (Exception ex) {

			log.error(ex.getMessage(), ex);
			rollback(session);
			throw new StoreException("Cannot access database.", ex);
		}

		// build result list

		User[] result = new ArrayList<DbUser> (users).toArray(new User[users.size()]);

		// done

		log.debug("Done.");
		return(result);
	}

	public User findUserByRecovery(String recovery) throws StoreException {

		log.debug("findUserByRecovery()");

		DbUser user;

		Session session = this.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		// get user

		try {

			user = DbUser.ByRecovery(session, recovery);
			commit(session);
		} catch (Exception ex) {

			log.error(ex.getMessage(), ex);
			rollback(session);
			throw new StoreException("Cannot access database.", ex);
		}

		// done

		log.debug("Done.");
		return(user);
	}

	public boolean existsUser(String identifier, String name) throws StoreException {

		log.debug("existsUser()");

		boolean exists = false;

		Session session = this.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		// check user

		try {

			if (identifier != null && DbUser.ByIdentifier(session, identifier) != null) exists = true;
			if (name != null && DbUser.ByName(session, name) != null) exists = true;
			commit(session);
		} catch (Exception ex) {

			log.error(ex.getMessage(), ex);
			rollback(session);
			throw new StoreException("Cannot access database.", ex);
		}

		// done

		log.debug("Done.");
		return(exists);
	}

	public User checkUserPassword(String identifier, String pass) throws StoreException {

		log.debug("checkUserPassword()");

		DbUser user;

		Session session = this.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		// check user

		try {

			user = DbUser.ByIdentifierAndPass(session, identifier, pass);
			commit(session);
		} catch (Exception ex) {

			log.error(ex.getMessage(), ex);
			rollback(session);
			throw new StoreException("Cannot access database.", ex);
		}

		// done

		log.debug("Done.");
		return(user);
	}

	/*
	 * Utility methods for accessing the database.
	 */

	private static void reattach(Session session, Object object) {

		if (session.contains(object)) return;

		session.buildLockRequest(LockOptions.READ).lock(object);
	}

	private static void commit(Session session) {

		try {

			if (session.isOpen() && session.getTransaction().isActive()) session.getTransaction().commit();
		} catch (Exception ex) {
			
			log.error(ex.getMessage(), ex);
			rollback(session);
		}
	}

	private static void rollback(Session session) {

		try {

			if (session.isOpen() && session.getTransaction().isActive()) session.getTransaction().rollback();
		} catch (Exception ex) {

			log.error(ex.getMessage(), ex);
		}
	}
}