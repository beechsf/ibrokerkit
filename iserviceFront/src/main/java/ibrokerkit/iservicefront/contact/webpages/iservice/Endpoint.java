package ibrokerkit.iservicefront.contact.webpages.iservice;

import ibrokerkit.iservicefront.IserviceApplication;
import ibrokerkit.iservicestore.store.Contact;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;

import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.IRequestHandler;
import org.openxri.IRIUtils;
import org.openxri.XRI;
import org.openxri.XRIAuthority;
import org.openxri.store.Authority;
import org.openxri.xml.ContactService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Endpoint implements IRequestHandler {

	private final static Logger log = LoggerFactory.getLogger(Endpoint.class.getName());

	public Endpoint() {

	}

	@Override
	public void detach(IRequestCycle requestCycle) {

	}

	@Override
	@SuppressWarnings("unchecked")
	public void respond(IRequestCycle requestCycle) {

		// handle the "Accept: uri-list" case

		Enumeration<String> e = ((ServletWebRequest) requestCycle.getRequest()).getContainerRequest().getHeaders("Accept");

		while (e.hasMoreElements()) {

			String accept = e.nextElement();
			if (accept.equals("uri-list")) {

				log.info("Processing uri-list request: " + ContactService.SERVICE_TYPE);

				requestCycle.getResponse().write(ContactService.SERVICE_TYPE + "\n");
				requestCycle.getResponse().close();
				return;
			}
		}

		// process the request

		XRI qxri = this.qxri(requestCycle);

		try {

			this.processRequest(requestCycle, qxri);
		} catch (Exception ex) {

			log.error("Failed.", ex);
			throw new RuntimeException(ex);
		}
	}

	private XRI qxri(IRequestCycle requestCycle) {

		String query = requestCycle.getRequest().getUrl().toString();
		query = query.substring(query.indexOf("/"));
		query = query.substring(query.indexOf("/"));
		query = query.substring(1);

		// construct an XRI from the query

		XRI qxri;

		try {

			qxri = new XRI(IRIUtils.IRItoXRI(IRIUtils.URItoIRI(query)));
		} catch (UnsupportedEncodingException ex) {

			throw new RuntimeException(ex);
		}

		log.info("Got request for XRI: " + qxri.toString());

		return qxri;
	}

	public void processRequest(IRequestCycle requestCycle, XRI qxri) throws Exception {

		ibrokerkit.iservicestore.store.Store iserviceStore = ((IserviceApplication) Application.get()).getIserviceStore();
		org.openxri.store.Store openxriStore = ((IserviceApplication) Application.get()).getOpenxriStore();

		// check qxri

		log.info("Processing request: Qxri=" + qxri.toString());

		if (! (qxri.getAuthorityPath() instanceof XRIAuthority)) {

			log.error("Can only work with GCS and XRef XRIs");
			return;
		}

		// find a matching contact i-service

		Contact contact = iserviceStore.findContact(qxri.getAuthorityPath().toString());
		Authority authority = openxriStore.localLookup((XRIAuthority) qxri.getAuthorityPath());

		// if we found none for the qxri, look for one for the authority id

		if (contact == null || contact.getEnabled().equals(Boolean.FALSE)) {

			if (authority != null) {

				contact = iserviceStore.findContact(authority.getId().toString());
			}
		}

		// nothing found?

		if (contact == null || contact.getEnabled().equals(Boolean.FALSE)) {

			// display not-found page

			Page page = new NotFoundPage(qxri);

			requestCycle.scheduleRequestHandlerAfterCurrent(new RenderPageRequestHandler(new PageProvider(page), RenderPageRequestHandler.RedirectPolicy.NEVER_REDIRECT));
			return;
		}

		// display contact page

		Page page = new ContactPage(contact, qxri, authority == null ? null : authority.getXrd());

		requestCycle.scheduleRequestHandlerAfterCurrent(new RenderPageRequestHandler(new PageProvider(page), RenderPageRequestHandler.RedirectPolicy.NEVER_REDIRECT));
	}
}
