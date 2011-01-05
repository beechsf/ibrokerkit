package ibrokerkit.iservicestore.sep;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.openxri.xml.SEPMediaType;
import org.openxri.xml.SEPPath;
import org.openxri.xml.SEPType;
import org.openxri.xml.SEPUri;
import org.openxri.xml.Service;

/**
 * This is a Locator Service as described by "Locator Service v1.0 Working Draft 01, 7 April 2007
 * All constructors produce a service endpoint that conforms to the specification.
 * The RECOMMENDED third-level DNS name for hosting a Locator Service is 'locator',
 * e.g. 'locator.my-i-broker.com'
 * 
 * @author =peacekeeper
 */
public class LocatorService extends Service {

	private static final long serialVersionUID = 8841486243416892L;

	public static final String SERVICE_TYPE = "xri://+i-service*(+locator)*($v*1.0)";
	public static final String LOCATOR_PATH = "(+locator)";

	/**
	 * Constructs a new Locator Service endpoint for use in an authority.
	 * @param locatorPages - The URI(s) where a locator page is implemented.
	 * @param providerID - The global i-number of the I-Broker providing this Locator Service.
	 * @param makeDefault - Whether to make the Locator Service the default service.
	 */
	public LocatorService(URI[] locatorPages, String providerID, boolean makeDefault) {

		super();

		/*
		 * According to the Locator Service specification, the ProviderID of the Locator Service
		 * SHOULD be set to the global i-number of the I-Broker.
		 */
		if (providerID != null) this.setProviderId(providerID);

		/*
		 * According to the Locator Service specification, these settings are REQUIRED to 
		 * establish the Locator Service.
		 */
		this.addType(new SEPType(SERVICE_TYPE, null, Boolean.TRUE));
		this.addPath(new SEPPath(LOCATOR_PATH, null, Boolean.TRUE));

		/*
		 * According to the Locator Service specification, setting a media type to default
		 * is not strictly necessary, since this setting is implied anyway, if no other
		 * media type is specified. On the other hand, it can't really harm.
		 */
		this.addMediaType(new SEPMediaType(null, SEPMediaType.MATCH_ATTR_DEFAULT, null));

		/*
		 * According to the Locator Service specification, it is RECOMMENDED to make the Locator
		 * Service the default service (i.e. the one to be chosen when no path or service type
		 * is used for service endpoint selection).
		 */
		if (makeDefault) {

			this.addType(new SEPType(null, SEPType.MATCH_ATTR_NULL, null));
			this.addPath(new SEPPath(null, SEPPath.MATCH_ATTR_NULL, null));
		}

		/*
		 * These are the URI where the Locator Service is implemented. The QXRI will be appended.
		 * It is currently not in the scope of OpenXRI to implement the actual locator page.
		 */
		for (int i=0; i<locatorPages.length; i++) {

			URI locatorPage = locatorPages[i];

			try {

				this.addURI(new SEPUri(locatorPage.toString(), null, SEPUri.APPEND_QXRI));
			} catch (URISyntaxException ex) {

				continue;
			}
		}
	}

	public LocatorService(URI locatorPage, String providerID, boolean makeDefault) {

		this(new URI[] { locatorPage }, providerID, makeDefault);
	}

	public LocatorService(URI[] locatorPages, String providerID) {

		this(locatorPages, providerID, true);
	}

	public LocatorService(URI locatorPage, String providerID) {

		this(new URI[] { locatorPage }, providerID, true);
	}

	public LocatorService(URI[] locatorPages) {

		this(locatorPages, null, true);
	}

	public LocatorService(URI locatorPage) {

		this(new URI[] { locatorPage }, null, true);
	}

	public static boolean isInstance(Service service) {

		if (service instanceof LocatorService) return(true);

		List<?> serviceTypes = service.getTypes();

		for (int i=0; i<serviceTypes.size(); i++) {

			SEPType serviceType = (SEPType) serviceTypes.get(i);

			if (SERVICE_TYPE.equals(serviceType.getValue())) return(true);
		}

		return(false);
	}
}
