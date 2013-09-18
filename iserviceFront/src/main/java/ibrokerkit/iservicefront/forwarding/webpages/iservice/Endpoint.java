package ibrokerkit.iservicefront.forwarding.webpages.iservice;

import ibrokerkit.iservicefront.IserviceApplication;
import ibrokerkit.iservicestore.store.Forwarding;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.http.handler.ErrorCodeRequestHandler;
import org.apache.wicket.request.http.handler.RedirectRequestHandler;
import org.openxri.IRIUtils;
import org.openxri.XRI;
import org.openxri.XRIAuthority;
import org.openxri.XRIPath;
import org.openxri.store.Authority;
import org.openxri.xml.ForwardingService;

public class Endpoint implements IRequestHandler {

	private static Log log = LogFactory.getLog(Endpoint.class.getName());

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

				log.info("Processing uri-list request: " + ForwardingService.SERVICE_TYPE);

				requestCycle.getResponse().write(ForwardingService.SERVICE_TYPE + "\n");
				requestCycle.getResponse().close();
				return;
			}
		}

		// process the request

		XRI qxri = qxri(requestCycle);

		try {

			this.processRequest(requestCycle, qxri);
		} catch (Exception ex) { 

			log.fatal("Failed.", ex);
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

		// find a matching forwarding i-service

		Forwarding forwarding = iserviceStore.findForwarding(qxri.getAuthorityPath().toString());
		Authority authority = openxriStore.localLookup((XRIAuthority) qxri.getAuthorityPath());

		// if we found none for the qxri, look for one for the authority id

		if (forwarding == null || forwarding.getEnabled().equals(Boolean.FALSE)) {

			if (authority != null) {

				forwarding = iserviceStore.findForwarding(authority.getId().toString());
			}
		}

		// nothing found?

		if (forwarding == null || forwarding.getEnabled().equals(Boolean.FALSE)) {

			// display not-found page

			Page page = new NotFoundPage(qxri);

			requestCycle.scheduleRequestHandlerAfterCurrent(new RenderPageRequestHandler(new PageProvider(page), RenderPageRequestHandler.RedirectPolicy.NEVER_REDIRECT));
			return;
		}

		// extract the path from the request

		XRIPath xriPath = qxri.getXRIPath();
		String path;

		path = (xriPath != null) ? xriPath.toString() : "";
		if (path.startsWith("/")) path = path.substring(1);

		// handle the (+index) case

		if (path.equals(ForwardingService.INDEX_PATH) && forwarding.getIndexPage().equals(Boolean.TRUE)) {

			Page page = new IndexPage(forwarding, qxri, authority == null ? null : authority.getXrd());

			requestCycle.scheduleRequestHandlerAfterCurrent(new RenderPageRequestHandler(new PageProvider(page), RenderPageRequestHandler.RedirectPolicy.NEVER_REDIRECT));
			return;
		}

		// get mappings

		Map<String, String> mappings = forwarding.getMappings();

		String uri = mappings.get(path);

		// no mapping found ?

		if (uri == null) {

			if (forwarding.getErrorPage().equals(Boolean.TRUE)) {

				Page page = new ErrorPage(forwarding, qxri, authority == null ? null : authority.getXrd(), path);

				requestCycle.scheduleRequestHandlerAfterCurrent(new RenderPageRequestHandler(new PageProvider(page), RenderPageRequestHandler.RedirectPolicy.NEVER_REDIRECT));
			} else {

				requestCycle.scheduleRequestHandlerAfterCurrent(new ErrorCodeRequestHandler(HttpServletResponse.SC_NOT_FOUND));
			}
			return;
		}

		// redirect to target uri

		requestCycle.scheduleRequestHandlerAfterCurrent(new RedirectRequestHandler(uri));
	}
}
