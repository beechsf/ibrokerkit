package ibrokerkit.iservicestore.store;

/**
 * The Store is used for creating, retrieving and managing i-services.
 */
public interface Store {

	//
	// general store methods
	//

	/**
	 * Initializes the Store.
	 */
	public void init() throws StoreException;

	/**
	 * True, if the Store has been initialized.
	 */
	public boolean isInitialized();

	/**
	 * Closes the Store.
	 */
	public void close();

	/**
	 * True, if the Store has been closed.
	 */
	public boolean isClosed();

	//
	// common database methods
	//

	/**
	 * Updates an object in the database after modifications have been made.
	 */
	public void updateObject(Object object) throws StoreException;

	/**
	 * Deletes an object from the database.
	 */
	public void deleteObject(Object object) throws StoreException;

	//
	// common i-service methods
	//

	/**
	 * Deletes all i-services associated with a given QXRI field.
	 */
	public void deleteAllIServices(String qxri) throws StoreException;

	//
	// Authentication i-service methods
	//

	/**
	 * Create a new Authentication i-service.
	 */
	public Authentication createAuthentication() throws StoreException;

	/**
	 * Get an Authentication i-service with a given ID.
	 */
	public Authentication getAuthentication(Long id) throws StoreException;

	/**
	 * List all Authentication i-services in the Store.
	 */
	public Authentication[] listAuthentications() throws StoreException;

	/**
	 * List all Authentication i-services with a given INDX field.
	 */
	public Authentication[] listAuthenticationsByIndex(String indx) throws StoreException;

	/**
	 * Find a single, enabled Authentication i-service with a given QXRI field.
	 */
	public Authentication findAuthentication(String qxri) throws StoreException;

	/**
	 * Find all Authentication i-services with a given QXRI field.
	 */
	public Authentication[] findAuthentications(String qxri) throws StoreException;

	//
	// Contact i-service methods
	//

	/**
	 * Create a new Contact i-service.
	 */
	public Contact createContact() throws StoreException;

	/**
	 * Get a Contact i-service with a given ID.
	 */
	public Contact getContact(Long id) throws StoreException;

	/**
	 * List all Contact i-services in the Store.
	 */
	public Contact[] listContacts() throws StoreException;

	/**
	 * List all Contact i-services with a given INDX field.
	 */
	public Contact[] listContactsByIndex(String indx) throws StoreException;

	/**
	 * Find a single, enabled Contact i-service with a given QXRI field.
	 */
	public Contact findContact(String qxri) throws StoreException;
	
	/**
	 * Find all Contact i-services with a given QXRI field.
	 */
	public Contact[] findContacts(String qxri) throws StoreException;

	//
	// Forwarding i-service methods
	//

	/**
	 * Create a new Forwarding i-service.
	 */
	public Forwarding createForwarding() throws StoreException;

	/**
	 * Get an Forwarding i-service with a given ID.
	 */
	public Forwarding getForwarding(Long id) throws StoreException;

	/**
	 * List all Forwarding i-services in the Store.
	 */
	public Forwarding[] listForwardings() throws StoreException;

	/**
	 * List all Forwarding i-services with a given INDX field.
	 */
	public Forwarding[] listForwardingsByIndex(String indx) throws StoreException;

	/**
	 * Find a single, enabled Forwarding i-service with a given QXRI field.
	 */
	public Forwarding findForwarding(String qxri) throws StoreException;

	/**
	 * Find all Forwarding i-services with a given QXRI field.
	 */
	public Forwarding[] findForwardings(String qxri) throws StoreException;

	//
	// Locator i-service methods
	//

	/**
	 * Create a new Locator i-service.
	 */
	public Locator createLocator() throws StoreException;

	/**
	 * Get an Locator i-service with a given ID.
	 */
	public Locator getLocator(Long id) throws StoreException;

	/**
	 * List all Locator i-services in the Store.
	 */
	public Locator[] listLocators() throws StoreException;

	/**
	 * List all Locator i-services with a given INDX field.
	 */
	public Locator[] listLocatorsByIndex(String indx) throws StoreException;

	/**
	 * Find a single, enabled Locator i-service with a given QXRI field.
	 */
	public Locator findLocator(String qxri) throws StoreException;

	/**
	 * Find all Locator i-services with a given QXRI field.
	 */
	public Locator[] findLocators(String qxri) throws StoreException;
}
