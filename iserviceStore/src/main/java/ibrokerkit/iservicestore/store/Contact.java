package ibrokerkit.iservicestore.store;

/**
 * <pre>
 * A Contact i-service.
 * Each Contact i-service holds a description, as well as an e-mail address to which
 * contact requests will be forwarded.
 * </pre>
 */
public interface Contact extends IService {

	/**
	 * Get the description associated with this Contact i-service.
	 * @return Description.
	 */
	public String getDescription();

	/**
	 * Set the description associated with this Contact i-service.
	 * @param description Description.
	 */
	public void setDescription(String description);

	/**
	 * Get the forwarding e-mail address associated with this Contact i-service.
	 * @return Forwarding e-mail address.
	 */
	public String getForward();

	/**
	 * Set the forwarding e-mail address associated with this Contact i-service.
	 * @param forward Forwarding e-mail address.
	 */
	public void setForward(String forward);
}
