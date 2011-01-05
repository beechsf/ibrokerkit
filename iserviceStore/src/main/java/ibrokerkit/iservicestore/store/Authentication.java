package ibrokerkit.iservicestore.store;

/**
 * <pre>
 * An Authentication (OpenID) i-service.
 * Each Authentication i-service holds a hashed password, which is used for
 * authenticating users at the i-service endpoint. The hashed password is not
 * automatically calculated by ibrokerStore but must be set by an application, e.g.
 * by using StoreUtil.
 * </pre>
 * @see ibrokerkit.iservicestore.store.StoreUtil
 */
public interface Authentication extends IService {

	/**
	 * Get the hashed password associated with this Authentication i-service.
	 * @return Hashed password.
	 */
	public String getPass();

	/**
	 * Set the hashed password associated with this Authentication i-service.
	 * @param pass Hashed password.
	 */
	public void setPass(String pass);
}
