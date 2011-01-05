package ibrokerkit.iservicefront.forwarding.webpages.iservice;

import ibrokerkit.iservicefront.forwarding.webapplication.ForwardingApplication;
import ibrokerkit.iservicestore.store.Forwarding;

import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Application;
import org.apache.wicket.IRequestTarget;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.request.WebErrorCodeResponseTarget;
import org.openxri.XRI;
import org.openxri.XRIAuthority;
import org.openxri.XRIPath;
import org.openxri.store.Authority;
import org.openxri.xml.ForwardingService;

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

				log.info("Processing uri-list request: " + ForwardingService.SERVICE_TYPE);

				requestCycle.getResponse().println(ForwardingService.SERVICE_TYPE);
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

		ibrokerkit.iservicestore.store.Store iserviceStore = ((ForwardingApplication) Application.get()).getIserviceStore();
		org.openxri.store.Store openxriStore = ((ForwardingApplication) Application.get()).getOpenxriStore();

		// check qxri

		log.info("Processing request: Qxri=" + this.qxri.toString());

		if (! (this.qxri.getAuthorityPath() instanceof XRIAuthority)) {

			log.error("Can only work with GCS and XRef XRIs");
			return;
		}

		// find a matching forwarding i-service

		Forwarding forwarding = iserviceStore.findForwarding(this.qxri.getAuthorityPath().toString());
		Authority authority = openxriStore.localLookup((XRIAuthority) this.qxri.getAuthorityPath());

		// if we found none for the qxri, look for one for the authority id

		if (forwarding == null || forwarding.getEnabled().equals(Boolean.FALSE)) {

			if (authority != null) {

				forwarding = iserviceStore.findForwarding(authority.getId().toString());
			}
		}

		// nothing found?

		if (forwarding == null || forwarding.getEnabled().equals(Boolean.FALSE)) {

			// display not-found page

			requestCycle.setResponsePage(new NotFoundPage(this.qxri));
			return;
		}

		// extract the path from the request

		XRIPath xriPath = this.qxri.getXRIPath();
		String path;

		path = (xriPath != null) ? xriPath.toString() : "";
		if (path.startsWith("/")) path = path.substring(1);

		// handle the (+index) case

		if (path.equals(ForwardingService.INDEX_PATH) && forwarding.getIndexPage().equals(Boolean.TRUE)) {

			requestCycle.setResponsePage(new IndexPage(forwarding, this.qxri, authority == null ? null : authority.getXrd()));
			return;
		}

		// get mappings

		Map<String, String> mappings = forwarding.getMappings();

		String uri = mappings.get(path);

		// no mapping found ?

		if (uri == null) {

			if (forwarding.getErrorPage().equals(Boolean.TRUE)) {

				requestCycle.setResponsePage(new ErrorPage(forwarding, this.qxri, authority == null ? null : authority.getXrd(), path));
			} else {

				requestCycle.setRequestTarget(new WebErrorCodeResponseTarget(HttpServletResponse.SC_NOT_FOUND));
			}
			return;
		}

		// redirect to target uri

		requestCycle.getResponse().redirect(uri);
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
