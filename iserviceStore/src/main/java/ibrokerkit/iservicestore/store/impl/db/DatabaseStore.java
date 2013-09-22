package ibrokerkit.iservicestore.store.impl.db;

import ibrokerkit.iservicestore.store.Authentication;
import ibrokerkit.iservicestore.store.Contact;
import ibrokerkit.iservicestore.store.Forwarding;
import ibrokerkit.iservicestore.store.Locator;
import ibrokerkit.iservicestore.store.Store;
import ibrokerkit.iservicestore.store.StoreException;

import java.util.List;
import java.util.Properties;

import org.hibernate.Hibernate;
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

			this.configuration.addClass(ibrokerkit.iservicestore.store.impl.db.DbAuthentication.class);
			this.configuration.addClass(ibrokerkit.iservicestore.store.impl.db.DbContact.class);
			this.configuration.addClass(ibrokerkit.iservicestore.store.impl.db.DbForwarding.class);
			this.configuration.addClass(ibrokerkit.iservicestore.store.impl.db.DbLocator.class);

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
			session.getTransaction().commit();
		} catch (Exception ex) {

			if (session.isOpen() && session.getTransaction().isActive()) session.getTransaction().rollback();
			log.error(ex.getMessage(), ex);
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
			session.getTransaction().commit();
		} catch (Exception ex) {

			if (session.isOpen() && session.getTransaction().isActive()) session.getTransaction().rollback();
			log.error(ex.getMessage(), ex);
			throw new StoreException("Cannot access database.", ex);
		}

		// done

		log.debug("Done.");
	}

	/*
	 * Common i-service methods
	 */

	public void deleteAllIServices(String qxri) throws StoreException {

		log.debug("deleteAllIServices(" + qxri + ")");

		Session session = this.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		// delete all i-services of the qxri

		try {

			for (DbAuthentication authentication : DbAuthentication.AllByQxri(session, qxri)) session.delete(authentication);
			for (DbContact contact : DbContact.AllByQxri(session, qxri)) session.delete(contact);
			for (DbForwarding forwarding : DbForwarding.AllByQxri(session, qxri)) session.delete(forwarding);
			for (DbLocator locator : DbLocator.AllByQxri(session, qxri)) session.delete(locator);

			session.flush();
			session.getTransaction().commit();
		} catch (Exception ex) {

			if (session.isOpen() && session.getTransaction().isActive()) session.getTransaction().rollback();
			log.error(ex.getMessage(), ex);
			throw new StoreException("Cannot access database.", ex);
		}

		// done

		log.debug("Done.");
	}

	/*
	 * Authentication methods
	 */

	public Authentication createAuthentication() throws StoreException {

		log.debug("createAuthentication()");

		DbAuthentication authentication = null;

		Session session = this.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		// create new Authentication

		try {

			authentication = new DbAuthentication();

			session.save(authentication);
			session.refresh(authentication);
			session.flush();
			session.getTransaction().commit();
		} catch (Exception ex) {

			if (session.isOpen() && session.getTransaction().isActive()) session.getTransaction().rollback();
			log.error(ex.getMessage(), ex);
			throw new StoreException("Cannot access database.", ex);
		}

		// done

		log.debug("Done.");
		return(authentication);
	}

	public Authentication getAuthentication(Long id) throws StoreException {

		log.debug("getAuthentication(" + id + ")");

		DbAuthentication authentication = null;

		Session session = this.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		// get Authentication

		try {

			authentication = (DbAuthentication) session.load(DbAuthentication.class, id);

			if (authentication != null) {

				Hibernate.initialize(authentication.getAttributes());
			}

			session.flush();
			session.getTransaction().commit();
		} catch (Exception ex) {

			if (session.isOpen() && session.getTransaction().isActive()) session.getTransaction().rollback();
			log.error(ex.getMessage(), ex);
			throw new StoreException("Cannot access database.", ex);
		}

		// done

		log.debug("Done.");
		return(authentication);
	}

	public Authentication[] listAuthentications() throws StoreException {

		log.debug("listAuthentications()");

		List<DbAuthentication> authentications = null;

		Session session = this.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		// get Authentications

		try {

			authentications = DbAuthentication.All(session);

			session.getTransaction().commit();
		} catch (Exception ex) {

			if (session.isOpen() && session.getTransaction().isActive()) session.getTransaction().rollback();
			log.error(ex.getMessage(), ex);
			throw new StoreException("Cannot access database.", ex);
		}

		// done

		log.debug("Done.");
		return(authentications.toArray(new Authentication[authentications.size()]));
	}

	public Authentication[] listAuthenticationsByIndex(String indx) throws StoreException {

		log.debug("listAuthenticationsByIndex(" + indx + ")");

		List<DbAuthentication> authentications = null;

		Session session = this.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		// get Authentications (look for index)

		try {

			authentications = DbAuthentication.AllByIndx(session, indx);

			session.getTransaction().commit();
		} catch (Exception ex) {

			if (session.isOpen() && session.getTransaction().isActive()) session.getTransaction().rollback();
			log.error(ex.getMessage(), ex);
			throw new StoreException("Cannot access database.", ex);
		}

		// done

		log.debug("Done.");
		return(authentications.toArray(new Authentication[authentications.size()]));
	}

	public Authentication findAuthentication(String qxri) throws StoreException {

		log.debug("findAuthentication(" + qxri + ")");

		DbAuthentication authentication = null;

		Session session = this.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		// get Authentication (look for qxri)

		try {

			authentication = DbAuthentication.EnabledByQxri(session, qxri);

			if (authentication != null) {

				Hibernate.initialize(authentication.getAttributes());
			}

			session.getTransaction().commit();
		} catch (Exception ex) {

			if (session.isOpen() && session.getTransaction().isActive()) session.getTransaction().rollback();
			log.error(ex.getMessage(), ex);
			throw new StoreException("Cannot access database.", ex);
		}

		// done

		log.debug("Done.");
		return(authentication);
	}

	public Authentication[] findAuthentications(String qxri) throws StoreException {

		log.debug("findAuthentications(" + qxri + ")");

		List<DbAuthentication> authentications = null;

		Session session = this.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		// get Authentications (look for qxri)

		try {

			authentications = DbAuthentication.AllByQxri(session, qxri);

			session.getTransaction().commit();
		} catch (Exception ex) {

			if (session.isOpen() && session.getTransaction().isActive()) session.getTransaction().rollback();
			log.error(ex.getMessage(), ex);
			throw new StoreException("Cannot access database.", ex);
		}

		// done

		log.debug("Done.");
		return(authentications.toArray(new Authentication[authentications.size()]));
	}

	/*
	 * Contact methods
	 */

	public Contact createContact() throws StoreException {

		log.debug("createContact()");

		DbContact contact = null;

		Session session = this.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		// create new Contact

		try {

			contact = new DbContact();

			session.save(contact);
			session.refresh(contact);
			session.flush();
			session.getTransaction().commit();
		} catch (Exception ex) {

			if (session.isOpen() && session.getTransaction().isActive()) session.getTransaction().rollback();
			log.error(ex.getMessage(), ex);
			throw new StoreException("Cannot access database.", ex);
		}

		// done

		log.debug("Done.");
		return(contact);
	}

	public Contact getContact(Long id) throws StoreException {

		log.debug("getContact(" + id + ")");

		DbContact contact = null;

		Session session = this.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		// get Contact

		try {

			contact = (DbContact) session.load(DbContact.class, id);

			if (contact != null) {

				Hibernate.initialize(contact.getAttributes());
			}

			session.flush();
			session.getTransaction().commit();
		} catch (Exception ex) {

			if (session.isOpen() && session.getTransaction().isActive()) session.getTransaction().rollback();
			log.error(ex.getMessage(), ex);
			throw new StoreException("Cannot access database.", ex);
		}

		// done

		log.debug("Done.");
		return(contact);
	}

	public Contact[] listContacts() throws StoreException {

		log.debug("listContacts()");

		List<DbContact> contacts = null;

		Session session = this.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		// get Contacts (look for index)

		try {

			contacts = DbContact.All(session);

			session.getTransaction().commit();
		} catch (Exception ex) {

			if (session.isOpen() && session.getTransaction().isActive()) session.getTransaction().rollback();
			log.error(ex.getMessage(), ex);
			throw new StoreException("Cannot access database.", ex);
		}

		// done

		log.debug("Done.");
		return(contacts.toArray(new Contact[contacts.size()]));
	}

	public Contact[] listContactsByIndex(String indx) throws StoreException {

		log.debug("listContactsByIndex(" + indx + ")");

		List<DbContact> contacts = null;

		Session session = this.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		// get Contacts (look for index)

		try {

			contacts = DbContact.AllByIndx(session, indx);

			session.getTransaction().commit();
		} catch (Exception ex) {

			if (session.isOpen() && session.getTransaction().isActive()) session.getTransaction().rollback();
			log.error(ex.getMessage(), ex);
			throw new StoreException("Cannot access database.", ex);
		}

		// done

		log.debug("Done.");
		return(contacts.toArray(new Contact[contacts.size()]));
	}

	public Contact findContact(String qxri) throws StoreException {

		log.debug("findContact()");

		DbContact contact = null;

		Session session = this.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		// get Contact (look for qxri)

		try {

			contact = DbContact.EnabledByQxri(session, qxri);

			if (contact != null) {

				Hibernate.initialize(contact.getAttributes());
			}

			session.getTransaction().commit();
		} catch (Exception ex) {

			if (session.isOpen() && session.getTransaction().isActive()) session.getTransaction().rollback();
			log.error(ex.getMessage(), ex);
			throw new StoreException("Cannot access database.", ex);
		}

		// done

		log.debug("Done.");
		return(contact);
	}

	public Contact[] findContacts(String qxri) throws StoreException {

		log.debug("findContacts(" + qxri + ")");

		List<DbContact> contacts = null;

		Session session = this.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		// get Contacts (look for qxri)

		try {

			contacts = DbContact.AllByQxri(session, qxri);

			session.getTransaction().commit();
		} catch (Exception ex) {

			if (session.isOpen() && session.getTransaction().isActive()) session.getTransaction().rollback();
			log.error(ex.getMessage(), ex);
			throw new StoreException("Cannot access database.", ex);
		}

		// done

		log.debug("Done.");
		return(contacts.toArray(new Contact[contacts.size()]));
	}

	/*
	 * Forwarding methods
	 */

	public Forwarding createForwarding() throws StoreException {

		log.debug("createForwarding()");

		DbForwarding forwarding = null;

		Session session = this.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		// create new Forwarding

		try {

			forwarding = new DbForwarding();

			session.save(forwarding);
			session.refresh(forwarding);
			session.flush();
			session.getTransaction().commit();
		} catch (Exception ex) {

			if (session.isOpen() && session.getTransaction().isActive()) session.getTransaction().rollback();
			log.error(ex.getMessage(), ex);
			throw new StoreException("Cannot access database.", ex);
		}

		// done

		log.debug("Done.");
		return(forwarding);
	}

	public Forwarding getForwarding(Long id) throws StoreException {

		log.debug("getForwarding(" + id + ")");

		DbForwarding forwarding = null;

		Session session = this.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		// get Authentication

		try {

			forwarding = (DbForwarding) session.load(DbForwarding.class, id);

			if (forwarding != null) {

				Hibernate.initialize(forwarding.getAttributes());
			}

			session.flush();
			session.getTransaction().commit();
		} catch (Exception ex) {

			if (session.isOpen() && session.getTransaction().isActive()) session.getTransaction().rollback();
			log.error(ex.getMessage(), ex);
			throw new StoreException("Cannot access database.", ex);
		}

		// done

		log.debug("Done.");
		return(forwarding);
	}

	public Forwarding[] listForwardings() throws StoreException {

		log.debug("findForwardings()");

		List<DbForwarding> forwardings = null;

		Session session = this.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		// get Forwardings

		try {

			forwardings = DbForwarding.All(session);

			session.getTransaction().commit();
		} catch (Exception ex) {

			if (session.isOpen() && session.getTransaction().isActive()) session.getTransaction().rollback();
			log.error(ex.getMessage(), ex);
			throw new StoreException("Cannot access database.", ex);
		}

		// done

		log.debug("Done.");
		return(forwardings.toArray(new Forwarding[forwardings.size()]));
	}

	public Forwarding[] listForwardingsByIndex(String indx) throws StoreException {

		log.debug("listForwardingsByIndex(" + indx + ")");

		List<DbForwarding> forwardings = null;

		Session session = this.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		// get Forwardings (look for index)

		try {

			forwardings = DbForwarding.AllByIndx(session, indx);

			session.getTransaction().commit();
		} catch (Exception ex) {

			if (session.isOpen() && session.getTransaction().isActive()) session.getTransaction().rollback();
			log.error(ex.getMessage(), ex);
			throw new StoreException("Cannot access database.", ex);
		}

		// done

		log.debug("Done.");
		return(forwardings.toArray(new Forwarding[forwardings.size()]));
	}

	public Forwarding findForwarding(String qxri) throws StoreException {

		DbForwarding forwarding = null;

		Session session = this.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		// get Forwarding (look for qxri)

		try {

			forwarding = DbForwarding.EnabledByQxri(session, qxri);

			if (forwarding != null) {

				Hibernate.initialize(forwarding.getAttributes());
			}

			session.getTransaction().commit();
		} catch (Exception ex) {

			if (session.isOpen() && session.getTransaction().isActive()) session.getTransaction().rollback();
			log.error(ex.getMessage(), ex);
			throw new StoreException("Cannot access database.", ex);
		}

		// done

		return(forwarding);
	}

	public Forwarding[] findForwardings(String qxri) throws StoreException {

		log.debug("findForwardings(" + qxri + ")");

		List<DbForwarding> forwardings = null;

		Session session = this.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		// get Forwardings (look for qxri)

		try {

			forwardings = DbForwarding.AllByQxri(session, qxri);

			session.getTransaction().commit();
		} catch (Exception ex) {

			if (session.isOpen() && session.getTransaction().isActive()) session.getTransaction().rollback();
			log.error(ex.getMessage(), ex);
			throw new StoreException("Cannot access database.", ex);
		}

		// done

		log.debug("Done.");
		return(forwardings.toArray(new Forwarding[forwardings.size()]));
	}

	/*
	 * Locator methods
	 */

	public Locator createLocator() throws StoreException {

		log.debug("createLocator()");

		DbLocator locator = null;

		Session session = this.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		// create new Locator

		try {

			locator = new DbLocator();

			session.save(locator);
			session.refresh(locator);
			session.flush();
			session.getTransaction().commit();
		} catch (Exception ex) {

			if (session.isOpen() && session.getTransaction().isActive()) session.getTransaction().rollback();
			log.error(ex.getMessage(), ex);
			throw new StoreException("Cannot access database.", ex);
		}

		// done

		log.debug("Done.");
		return(locator);
	}

	public Locator getLocator(Long id) throws StoreException {

		log.debug("getLocator(" + id + ")");

		DbLocator locator = null;

		Session session = this.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		// get Locator

		try {

			locator = (DbLocator) session.load(DbLocator.class, id);

			if (locator != null) {

				Hibernate.initialize(locator.getAttributes());
			}

			session.flush();
			session.getTransaction().commit();
		} catch (Exception ex) {

			if (session.isOpen() && session.getTransaction().isActive()) session.getTransaction().rollback();
			log.error(ex.getMessage(), ex);
			throw new StoreException("Cannot access database.", ex);
		}

		// done

		log.debug("Done.");
		return(locator);
	}

	public Locator[] listLocators() throws StoreException {

		log.debug("listLocators()");

		List<DbLocator> locators = null;

		Session session = this.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		// get Locators (look for index)

		try {

			locators = DbLocator.All(session);

			session.getTransaction().commit();
		} catch (Exception ex) {

			if (session.isOpen() && session.getTransaction().isActive()) session.getTransaction().rollback();
			log.error(ex.getMessage(), ex);
			throw new StoreException("Cannot access database.", ex);
		}

		// done

		log.debug("Done.");
		return(locators.toArray(new Locator[locators.size()]));
	}

	public Locator[] listLocatorsByIndex(String indx) throws StoreException {

		log.debug("listLocatorsByIndex(" + indx + ")");

		List<DbLocator> locators = null;

		Session session = this.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		// get Locators (look for index)

		try {

			locators = DbLocator.AllByIndx(session, indx);

			session.getTransaction().commit();
		} catch (Exception ex) {

			if (session.isOpen() && session.getTransaction().isActive()) session.getTransaction().rollback();
			log.error(ex.getMessage(), ex);
			throw new StoreException("Cannot access database.", ex);
		}

		// done

		log.debug("Done.");
		return(locators.toArray(new Locator[locators.size()]));
	}

	public Locator findLocator(String qxri) throws StoreException {

		log.debug("findLocator()");

		DbLocator locator = null;

		Session session = this.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		// get Locator (look for qxri)

		try {

			locator = DbLocator.EnabledByQxri(session, qxri);

			if (locator != null) {

				Hibernate.initialize(locator.getAttributes());
			}

			session.getTransaction().commit();
		} catch (Exception ex) {

			if (session.isOpen() && session.getTransaction().isActive()) session.getTransaction().rollback();
			log.error(ex.getMessage(), ex);
			throw new StoreException("Cannot access database.", ex);
		}

		// done

		log.debug("Done.");
		return(locator);
	}

	public Locator[] findLocators(String qxri) throws StoreException {

		log.debug("findLocators(" + qxri + ")");

		List<DbLocator> locators = null;

		Session session = this.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		// get Locators (look for qxri)

		try {

			locators = DbLocator.AllByQxri(session, qxri);

			session.getTransaction().commit();
		} catch (Exception ex) {

			if (session.isOpen() && session.getTransaction().isActive()) session.getTransaction().rollback();
			log.error(ex.getMessage(), ex);
			throw new StoreException("Cannot access database.", ex);
		}

		// done

		log.debug("Done.");
		return(locators.toArray(new Locator[locators.size()]));
	}

	/*
	 * Utility methods for accessing the database.
	 */

	private static void reattach(Session session, Object object) {

		if (session.contains(object)) return;

		session.buildLockRequest(LockOptions.READ).lock(object);
	}
}