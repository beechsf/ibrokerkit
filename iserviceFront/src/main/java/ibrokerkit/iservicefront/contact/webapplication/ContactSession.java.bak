package ibrokerkit.iservicefront.contact.webapplication;

import ibrokerkit.iservicefront.contact.webpages.iservice.ContactPage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.IPageMap;
import org.apache.wicket.Page;
import org.apache.wicket.PageMap;
import org.apache.wicket.Request;
import org.apache.wicket.protocol.http.WebSession;
import org.openid4java.discovery.DiscoveryInformation;

public class ContactSession extends WebSession {

	private static final long serialVersionUID = -5669746479827302910L;

	protected static Log log = LogFactory.getLog(ContactSession.class.getName());

	private static final String SESSION_ATTRIBUTE_DISCOVERYINFORMATION = "__discoveryinformation__";
	private static final String SESSION_ATTRIBUTE_CONTACTPAGEMAPNAME = "__contactpagemapname__";
	private static final String SESSION_ATTRIBUTE_CONTACTPAGEID = "__contactpageid__";
	private static final String SESSION_ATTRIBUTE_CONTACTPAGEVERSION = "__contactpageversion__";

	public ContactSession(Request request) {

		super(request);
	}

	public DiscoveryInformation retrieveDiscoveryInformation() {

		log.debug("Retrieving OpenID discovery information from session.");

		return((DiscoveryInformation) this.getAttribute(SESSION_ATTRIBUTE_DISCOVERYINFORMATION));
	}

	public void storeDiscoveryInformation(DiscoveryInformation discoveryInformation) {

		log.debug("Storing OpenID discovery information in session.");

		this.setAttribute(SESSION_ATTRIBUTE_DISCOVERYINFORMATION, discoveryInformation);
	}

	public ContactPage retrieveContactPage() {

		log.debug("Retrieving Contact Page from session.");

		String pageMapName = (String) this.getAttribute(SESSION_ATTRIBUTE_CONTACTPAGEMAPNAME);
		int pageId = ((Integer) this.getAttribute(SESSION_ATTRIBUTE_CONTACTPAGEID)).intValue();
		int pageVersion = ((Integer) this.getAttribute(SESSION_ATTRIBUTE_CONTACTPAGEVERSION)).intValue();

		IPageMap pageMap = PageMap.forName(pageMapName);
		Page page = pageMap.get(pageId, pageVersion);

		return(((ContactPage) page));
	}

	public void storeContactPage(ContactPage contactPage) {

		log.debug("Storing Contact Page in session.");

		this.setAttribute(SESSION_ATTRIBUTE_CONTACTPAGEMAPNAME, contactPage.getPageMapName());
		this.setAttribute(SESSION_ATTRIBUTE_CONTACTPAGEID, new Integer(contactPage.getPageMapEntry().getNumericId()));
		this.setAttribute(SESSION_ATTRIBUTE_CONTACTPAGEVERSION, new Integer(contactPage.getCurrentVersionNumber()));
	}
}