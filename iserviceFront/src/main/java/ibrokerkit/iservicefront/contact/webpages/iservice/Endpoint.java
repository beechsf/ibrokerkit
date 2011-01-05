package ibrokerkit.iservicefront.contact.webpages.iservice;

import ibrokerkit.iservicefront.contact.webapplication.ContactApplication;
import ibrokerkit.iservicestore.store.Contact;

import java.util.Enumeration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Application;
import org.apache.wicket.IRequestTarget;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.protocol.http.WebRequest;
import org.openxri.XRI;
import org.openxri.XRIAuthority;
import org.openxri.store.Authority;
import org.openxri.xml.ContactService;

public class Endpoint implements IRequestTarget {

	private static Log log = LogFactory.getLog(Endpoint.class.getName());

	private XRI qxri;

	public Endpoint(XRI qxri) {

		this.qxri = qxri;
	}

	@SuppressWarnings("unchecked")
	public void respond(RequestCycle requestCycle) {

		// handle the "Accept: uri-list" case

		Enumeration<String> e = ((WebRequest) requestCycle.getRequest()).getHttpServletRequest().getHeaders("Accept");

		while (e.hasMoreElements()) {

			String accept = e.nextElement();
			if (accept.equals("uri-list")) {

				log.info("Processing uri-list request: " + ContactService.SERVICE_TYPE);

				requestCycle.getResponse().println(ContactService.SERVICE_TYPE);
				requestCycle.getResponse().close();
				return;
			}
		}

		// process the request

		try {

			this.processRequest(requestCycle);
		} catch (Exception ex) {

			log.fatal("Failed.", ex);
			throw new RuntimeException(ex);
		}
	}

	public void processRequest(RequestCycle requestCycle) throws Exception {

		ibrokerkit.iservicestore.store.Store iserviceStore = ((ContactApplication) Application.get()).getIserviceStore();
		org.openxri.store.Store openxriStore = ((ContactApplication) Application.get()).getOpenxriStore();

		// check qxri

		log.info("Processing request: Qxri=" + this.qxri.toString());

		if (! (this.qxri.getAuthorityPath() instanceof XRIAuthority)) {

			log.error("Can only work with GCS and XRef XRIs");
			return;
		}

		// find a matching contact i-service

		Contact contact = iserviceStore.findContact(this.qxri.getAuthorityPath().toString());
		Authority authority = openxriStore.localLookup((XRIAuthority) this.qxri.getAuthorityPath());

		// if we found none for the qxri, look for one for the authority id

		if (contact == null || contact.getEnabled().equals(Boolean.FALSE)) {

			if (authority != null) {

				contact = iserviceStore.findContact(authority.getId().toString());
			}
		}

		// nothing found?

		if (contact == null || contact.getEnabled().equals(Boolean.FALSE)) {

			// display not-found page

			requestCycle.setResponsePage(new NotFoundPage(this.qxri));
			return;
		}

		// display contact page

		requestCycle.setResponsePage(new ContactPage(contact, this.qxri, authority == null ? null : authority.getXrd()));
	}

	public XRI getQxri() {
		return (this.qxri);
	}

	public void setQxri(XRI qxri) {
		this.qxri = qxri;
	}

	public void detach(RequestCycle requestCycle) {

	}
}
