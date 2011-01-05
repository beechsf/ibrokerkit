package ibrokerkit.epptools4java.store.impl.db;

import ibrokerkit.epptools4java.store.Action;
import ibrokerkit.epptools4java.store.Poll;
import ibrokerkit.epptools4java.store.Store;
import ibrokerkit.epptools4java.store.StoreException;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;


public class DatabaseStore implements Store {

	private static final Log log = LogFactory.getLog(DatabaseStore.class.getName());

	private Properties properties;
	private Configuration configuration;
	private SessionFactory sessionFactory;

	public DatabaseStore(Properties properties) {

		this.properties = properties;
		this.configuration = null;
		this.sessionFactory = null;
	}

	public void init() throws StoreException {

		log.trace("init()");

		try {

			// prepare Hibernate configuration

			this.configuration = new Configuration();

			this.configuration.setProperties(this.properties);

			this.configuration.addClass(ibrokerkit.epptools4java.store.impl.db.DbAction.class);
			this.configuration.addClass(ibrokerkit.epptools4java.store.impl.db.DbPoll.class);

			// create session factory

			this.initSessionFactory();
		} catch (Exception ex) {

			log.error(ex);
			throw new StoreException("Cannot initialize Hibernate", ex);
		}

		log.trace("Done.");
	}

	private void initSessionFactory() {

		this.sessionFactory = this.configuration.buildSessionFactory();
	}

	public boolean isInitialized() {

		return(this.configuration != null && this.sessionFactory != null);
	}

	public void close() {

		log.trace("close()");

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

		log.trace("updateObject()");

		Session session = this.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		// check object

		if (! (object instanceof DbObject)) throw new StoreException("Object is not from this store.");

		// update object

		try {

			session.update(object);
			session.flush();
			session.refresh(object);
			session.getTransaction().commit();
		} catch (Exception ex) {

			if (session.isOpen() && session.getTransaction().isActive()) session.getTransaction().rollback();
			log.error(ex);
			throw new StoreException("Cannot access database.", ex);
		}

		// done

		log.trace("Done.");
	}

	public void deleteObject(Object object) throws StoreException {

		log.trace("deleteObject()");

		Session session = this.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		// check object

		if (! (object instanceof DbObject)) throw new StoreException("Object is not from this store.");

		// delete object

		try {

			reattach(session, object);
			session.delete(object);
			session.flush();
			session.getTransaction().commit();
		} catch (Exception ex) {

			if (session.isOpen() && session.getTransaction().isActive()) session.getTransaction().rollback();
			log.error(ex);
			throw new StoreException("Cannot access database.", ex);
		}
	}

	/*
	 * Action methods
	 */

	public Action[] listActions() throws StoreException {

		log.trace("listActions()");

		Session session = this.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		// get all actions

		List<DbAction> actions;

		try {

			actions = DbAction.All(session);
			session.getTransaction().commit();
		} catch (Exception ex) {

			if (session.isOpen() && session.getTransaction().isActive()) session.getTransaction().rollback();
			log.error(ex);
			throw new StoreException("Cannot access database.", ex);
		}

		// build result list

		Action[] result = new ArrayList<DbAction> (actions).toArray(new Action[actions.size()]);

		// done

		log.trace("Done.");
		return(result);
	}

	public Action createAction(Character gcs, String transactionId, String request, String response) throws StoreException {

		log.trace("createAction()");

		DbAction action = null;

		Session session = this.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		// create action

		try {

			action = new DbAction();

			action.setGcs(gcs);
			action.setTransactionId(transactionId);
			action.setRequest(request);
			action.setResponse(response);

			session.save(action);
			session.getTransaction().commit();
		} catch (Exception ex) {

			if (session.isOpen() && session.getTransaction().isActive()) session.getTransaction().rollback();
			log.error(ex);
			throw new StoreException("Cannot access database.", ex);
		}

		// done

		log.trace("Done.");
		return(action);
	}

	/*
	 * Poll methods
	 */

	public Poll[] listPolls() throws StoreException {

		log.trace("listPolls()");

		Session session = this.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		// get all polls

		List<DbPoll> polls;

		try {

			polls = DbPoll.All(session);
			session.getTransaction().commit();
		} catch (Exception ex) {

			if (session.isOpen() && session.getTransaction().isActive()) session.getTransaction().rollback();
			log.error(ex);
			throw new StoreException("Cannot access database.", ex);
		}

		// build result list

		Poll[] result = new ArrayList<DbPoll> (polls).toArray(new Poll[polls.size()]);

		// done

		log.trace("Done.");
		return(result);
	}

	public Poll createPoll(Character gcs, String transactionId, String response) throws StoreException {

		log.trace("createPoll()");

		DbPoll poll = null;

		Session session = this.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		// create poll

		try {

			poll = new DbPoll();

			poll.setGcs(gcs);
			poll.setTransactionId(transactionId);
			poll.setResponse(response);

			session.save(poll);
			session.getTransaction().commit();
		} catch (Exception ex) {

			if (session.isOpen() && session.getTransaction().isActive()) session.getTransaction().rollback();
			log.error(ex);
			throw new StoreException("Cannot access database.", ex);
		}

		// done

		log.trace("Done.");
		return(poll);
	}

	/*
	 * Utility methods for accessing the database.
	 */

	private static void reattach(Session session, Object object) {

		if (session.contains(object)) return;

		session.lock(object, LockMode.NONE);
	}
}