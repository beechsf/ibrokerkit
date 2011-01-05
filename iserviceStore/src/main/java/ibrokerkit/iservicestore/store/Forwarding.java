package ibrokerkit.iservicestore.store;

import java.util.Map;

/**
 * <pre>
 * A Forwarding i-service.
 * Each Forwarding i-service holds a mapping from relative XRIs to URIs.
 * The keys in this mapping are paths, and the values are URIs.
 * E.g. (+blog) --> http://blog.mysite.com
 * A Forwarding i-service can also support an Index Page and an Error Page, if
 * the appropriate flags are turned on.
 * </pre>
 */
public interface Forwarding extends IService {

	/**
	 * Get a flag whether this Forwarding i-service should support
	 * an automatic index page at /(+index).
	 * @return Index Page flag.
	 */
	public Boolean getIndexPage();

	/**
	 * Set a flag whether this Forwarding i-service should support
	 * an automatic index page at /(+index).
	 * @param indexPage Index Page flag.
	 */
	public void setIndexPage(Boolean indexPage);

	/**
	 * Get a flag whether this Forwarding i-service should support
	 * an automatic error page at undefined mappings.
	 * @return errorPage Error Page flag.
	 */
	public Boolean getErrorPage();

	/**
	 * Set a flag whether this Forwarding i-service should support
	 * an automatic error page at undefined mappings.
	 * @param errorPage Error Page flag.
	 */
	public void setErrorPage(Boolean errorPage);

	/**
	 * Get the mappings associated with this Forwarding i-service.
	 * @return Mappings.
	 */
	public Map<String, String> getMappings();

	/**
	 * Set the mappings associated with this Forwarding i-service.
	 * @param mappings Mappings.
	 */
	public void setMappings(Map<String, String> mappings);
}
