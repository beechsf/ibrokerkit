package ibrokerkit.iservicestore.store;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * <pre>
 * An i-service is a service on a web page which is associated with one or more XRIs.
 * 
 * The association between the i-service and XRIs is realized through the QXRI field. 
 * This can be one of the following:
 * 
 * 1) An actual XRI, e.g. =myname. In this case, the i-service will only 
 * be invoked when a request to exactly that XRI is made.
 * 
 * 2) An ID of an OpenXRI authority, e.g. 84. In this case, the i-service will
 * be invoked when a request is made to any XRI that uses this authority. This is
 * the preferred way of using ibrokerStore, since it properly supports XRI synonyms.
 * 
 * For example, a single i-service can be associated with the following synonym XRIs, 
 * assuming they all use the same OpenXRI authority:
 * - =myname
 * - =mysynonymname
 * - =!91F2.8153.F600.AE24
 * 
 * Another important concept is the INDX field. This value is not used internally by
 * iserviceStore or iserviceFront, but can be used as a key for looking up i-services.
 * In ibrokerKit, this value contains the identifier of a user in ibrokerStore and therefore
 * associates i-services with users.
 * </pre>
 */
public interface IService extends Serializable, Comparable<IService> {

	/**
	 * Get the internal ID of this i-service.
	 * @return ID.
	 */
	public Long getId();

	/**
	 * Get the internal timestamp of this i-service.
	 * @return Timestamp.
	 */
	public Date getTimestamp();

	/**
	 * Get the QXRI this i-service is bound to. 
	 * @return Qxri.
	 */
	public String getQxri();

	/**
	 * Set the QXRI this i-service is bound to. 
	 * @param qxri Qxri.
	 */
	public void setQxri(String qxri);
	
	/**
	 * Get the internal name of this i-service. This is only used for
	 * informational purposes and may be empty.
	 * @return Name.
	 */
	public String getName();

	/**
	 * Get the internal name of this i-service. This is only used for
	 * informational purposes and may be empty.
	 * @param name Name.
	 */
	public void setName(String name);

	/**
	 * Get a flag whether the i-service is enabled or not.
	 * An i-service that is not enabled cannot be invoked through ibrokerFront.
	 * @return Enabled flag.
	 */
	public Boolean getEnabled();

	/**
	 * Set a flag whether the i-service is enabled or not.
	 * An i-service that is not enabled cannot be invoked through ibrokerFront.
	 * @param enabled Enabled flag.
	 */
	public void setEnabled(Boolean enabled);

	/**
	 * Get arbitrary key/value pairs. These attributes are not used internally.
	 * @return Attributes.
	 */
	public Map<String, String> getAttributes();

	/**
	 * Set arbitrary key/value pairs. These attributes are not used internally.
	 * @param attributes Attributes.
	 */
	public void setAttributes(Map<String, String> attributes);

	/**
	 * Get the INDX value of this i-service.
	 * @return Indx.
	 */
	public String getIndx();

	/**
	 * Set the INDX value of this i-service.
	 * @param indx Indx.
	 */
	public void setIndx(String indx);
}
