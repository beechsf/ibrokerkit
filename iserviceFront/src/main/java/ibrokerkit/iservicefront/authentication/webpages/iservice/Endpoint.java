package ibrokerkit.iservicefront.authentication.webpages.iservice;

import ibrokerkit.iservicefront.authentication.webapplication.AuthenticationApplication;
import ibrokerkit.iservicefront.authentication.webapplication.AuthenticationSession;
import ibrokerkit.iservicestore.store.Authentication;

import java.util.Enumeration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Application;
import org.apache.wicket.IRequestTarget;
import org.apache.wicket.Page;
import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.request.target.basic.RedirectRequestTarget;
import org.openid4java.message.AssociationRequest;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.DirectError;
import org.openid4java.message.Message;
import org.openid4java.message.Parameter;
import org.openid4java.message.ParameterList;
import org.openid4java.message.VerifyRequest;
import org.openid4java.server.ServerManager;
import org.openxri.IRIUtils;
import org.openxri.XRI;
import org.openxri.XRIAuthority;
import org.openxri.store.Authority;
import org.openxri.xml.AuthenticationService;

public class Endpoint implements IRequestTarget {

	private static Log log = LogFactory.getLog(Endpoint.class.getName());

	public Endpoint() {

	}

	@SuppressWarnings("unchecked")
	public void respond(RequestCycle requestCycle) {

		// handle the "Accept: uri-list" case

		Enumeration<String> e = ((WebRequest) requestCycle.getRequest()).getHttpServletRequest().getHeaders("Accept");

		while (e.hasMoreElements()) {

			String accept = e.nextElement();
			if (accept.equals("uri-list")) {

				log.info("Processing uri-list request: " + AuthenticationService.SERVICE_TYPE1 + " / " + AuthenticationService.SERVICE_TYPE2);

				requestCycle.getResponse().println(AuthenticationService.SERVICE_TYPE1);
				requestCycle.getResponse().println(AuthenticationService.SERVICE_TYPE2);
				requestCycle.getResponse().close();
				return;
			}
		}

		// process the request

		try {

			this.processRequest(requestCycle);
		} catch (Exception ex) {

			log.fatal("Failed: " + ex.getMessage(), ex);
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Process a request to our OpenID IdP
	 * This method first finds a matching Authentication i-server and then calls
	 * appropriate other methods for various steps in the authentication process.
	 * @param requestCycle
	 * @throws Exception
	 */
	public void processRequest(RequestCycle requestCycle) throws Exception {

		ibrokerkit.iservicestore.store.Store iserviceStore = ((AuthenticationApplication) Application.get()).getIserviceStore();
		org.openxri.store.Store openxriStore = ((AuthenticationApplication) Application.get()).getOpenxriStore();

		Request request = requestCycle.getRequest();
		Response response = requestCycle.getResponse();
		ParameterList parameters = new ParameterList(request.getParameterMap());

		// extract the parameters from the request

		String mode = request.getParameter("openid.mode");
		log.info("Processing Authentication request with mode " + mode);

		for (Object parameter : parameters.getParameters()) {

			log.info(parameter.toString());
		}

		// is it an association request? process it now

		if (AssociationRequest.MODE_ASSOC.equals(mode)) {

			this.processAssociate(requestCycle, parameters);
			return;
		}

		// is it a verification request? process it now

		if (VerifyRequest.MODE_CHKAUTH.equals(mode)) {

			this.processCheckAuthentication(requestCycle, parameters);
			return;
		}

		// determine identity

		String identity;
		XRI qxri;

		identity = parameters.hasParameter("openid.identity") ? parameters.getParameter("openid.identity").getValue() : null;
		if (identity == null) identity = parameters.hasParameter("openid.claimed_id") ? parameters.getParameter("openid.claimed_id").getValue() : null;
		log.info("identity = " + identity);

		if (mode == null || identity == null) {

			// error response

			Message message = DirectError.createDirectError("Invalid request (either mode or identity is null)");

			log.info("Sending message: " + message.keyValueFormEncoding());
			response.write(message.keyValueFormEncoding());
		}

		if (OpenIDUtil.isDirectedIdentity(identity)) {

			log.info("Using directed identity.");
			qxri = null;
		} else {

			identity = fixHXRI(identity);
			log.info("identity (HXRI fixed) = " + identity);

			qxri = new XRI(IRIUtils.IRItoXRI(IRIUtils.URItoIRI(identity)));
			identity = qxri.getAuthorityPath().toString();
			log.info("identity (XRI fixed) = " + identity);

			// check qxri

			log.info("Processing request: Qxri=" + qxri.toString());

			if (! (qxri.getAuthorityPath() instanceof XRIAuthority)) {

				log.error("Can only work with GCS and XRef XRIs");
				return;
			}
		}

		// find a matching authentication i-service

		Authentication authentication = null;

		if (qxri != null) {

			authentication = iserviceStore.findAuthentication(qxri.getAuthorityPath().toString());
			Authority authority = openxriStore.localLookup((XRIAuthority) qxri.getAuthorityPath());

			// if we found none for the qxri, look for one for the authority id

			if (authentication == null || authentication.getEnabled().equals(Boolean.FALSE)) {

				if (authority != null) {

					authentication = iserviceStore.findAuthentication(authority.getId().toString());
				}
			}

			// nothing found?

			if (authentication == null || authentication.getEnabled().equals(Boolean.FALSE)) {

				// display not-found page

				requestCycle.setResponsePage(new NotFoundPage(qxri));
				return;
			}
		}

		// what kind of OpenID request is this ?

		if (AuthRequest.MODE_IMMEDIATE.equals(mode)) {

			this.processImmediate(requestCycle, parameters, authentication, identity);
			return;
		} else if (AuthRequest.MODE_SETUP.equals(mode)) {

			this.processSetup(requestCycle, parameters, authentication, identity);
			return;
		} else if (AuthRequest.MODE_CANCEL.equals(mode)) {

			this.processCancel(requestCycle, parameters, authentication);
		} else {

			// error response

			Message message = DirectError.createDirectError("Unknown mode: " + mode);

			log.info("Sending message: " + message.keyValueFormEncoding());
			response.write(message.keyValueFormEncoding());
		}
	}

	public void processAssociate(RequestCycle requestCycle, ParameterList parameters) throws Exception {

		ServerManager serverManager = ((AuthenticationApplication) Application.get()).getServerManager();

		Response response = requestCycle.getResponse();

		// if there's no session_type parameter, add one for compatibility

		Parameter parameter = parameters.getParameter("openid.session_type");

		if (parameter == null) {

			parameter = new Parameter("openid.session_type", "");
			parameters.set(parameter);
		}

		// generate answer to association request and output it

		log.debug("Creating association response...");
		Message message = serverManager.associationResponse(parameters);

		log.info("Sending message: " + message.keyValueFormEncoding());
		response.write(message.keyValueFormEncoding());
	}

	public void processImmediate(RequestCycle requestCycle, ParameterList parameters, Authentication authentication, String identity) throws Exception {

		ServerManager serverManager = ((AuthenticationApplication) Application.get()).getServerManager();

		Response response = requestCycle.getResponse();

		// check if the user is logged in

		String userIdentifier = ((AuthenticationSession) Session.get()).getUserIdentifier();

		if (userIdentifier != null && (userIdentifier.equals(identity) || OpenIDUtil.isDirectedIdentity(identity))) {

			// check directed identity

			String outIdentity;

			if (OpenIDUtil.isDirectedIdentity(identity)) {

				// override identity

				outIdentity = userIdentifier;
			} else {

				outIdentity = identity;
			}

			Endpoint.log.debug("Using out identity: " + outIdentity);

			// create OpenID response

			String endpointUrl = ((AuthenticationApplication) Application.get()).getProperties().getProperty("endpoint-url");
			serverManager.setOPEndpointUrl(endpointUrl);

			Message message;

			if (OpenIDUtil.isDirectedIdentity(identity)) {

				Endpoint.log.debug("Creating positive directed identity authentication response...");
				message = serverManager.authResponse(
						parameters, 
						"xri://" + outIdentity, 
						"xri://" + outIdentity, 
						true);
			} else {

				Endpoint.log.debug("Creating positive authentication response...");
				message = serverManager.authResponse(
						parameters, 
						null, 
						null, 
						true);
			}

			if (message instanceof DirectError) {

				log.info("Sending message: " + message.keyValueFormEncoding());
				response.write(message.keyValueFormEncoding());
				return;
			}

			// sign the response

			Endpoint.log.debug("Signing authentication response...");

			if (message instanceof AuthSuccess) serverManager.sign((AuthSuccess) message);

			// option1: GET HTTP-redirect to the return_to URL

			Endpoint.log.debug("Sending authentication response via GET HTTP-redirect.");

			String redirectUrl = message.getDestinationUrl(true);
			log.info("Redirecting message: " + redirectUrl);
			RequestCycle.get().setRequestTarget(new RedirectRequestTarget(redirectUrl));

			// option2: HTML FORM Redirection
			//Endpoint.log.debug("Sending authentication response via HTML FORM redirect.");

			//Page page = new OpenIDRedirect(message);
			//requestCycle.setResponsePage(page);
			return;
		} else {

			// create OpenID response

			String endpointUrl = ((AuthenticationApplication) Application.get()).getProperties().getProperty("endpoint-url");
			serverManager.setOPEndpointUrl(endpointUrl);

			String userSetupUrl = endpointUrl;
			serverManager.setUserSetupUrl(userSetupUrl);

			log.debug("Creating immediate negative authentication response...");
			Message message = serverManager.authResponse(
					parameters, 
					"", 
					"", 
					false);

			if (message instanceof DirectError) {

				log.info("Sending message: " + message.keyValueFormEncoding());
				response.write(message.keyValueFormEncoding());
				return;
			}

			// sign the response

			Endpoint.log.debug("Signing authentication response...");

			if (message instanceof AuthSuccess) serverManager.sign((AuthSuccess) message);

			// option1: GET HTTP-redirect to the return_to URL

			Endpoint.log.debug("Sending authentication response via GET HTTP-redirect.");

			String redirectUrl = message.getDestinationUrl(true);
			log.info("Redirecting message: " + redirectUrl);
			RequestCycle.get().setRequestTarget(new RedirectRequestTarget(redirectUrl));

			// option2: HTML FORM Redirection
			//Endpoint.log.debug("Sending authentication response via HTML FORM redirect.");

			//Page page = new OpenIDRedirect(message);
			//requestCycle.setResponsePage(page);
			return;
		}
	}

	public void processSetup(RequestCycle requestCycle, ParameterList parameters, Authentication authentication, String identity) throws Exception {

		ServerManager serverManager = ((AuthenticationApplication) Application.get()).getServerManager();

		Response response = requestCycle.getResponse();

		// check if the user is logged in

		String userIdentifier = ((AuthenticationSession) Session.get()).getUserIdentifier();

		if (userIdentifier != null && (userIdentifier.equals(identity) || OpenIDUtil.isDirectedIdentity(identity))) {

			// check directed identity

			String outIdentity;

			if (OpenIDUtil.isDirectedIdentity(identity)) {

				// override identity

				outIdentity = userIdentifier;
			} else {

				outIdentity = identity;
			}

			Endpoint.log.debug("Using out identity: " + outIdentity);

			// create OpenID response

			String endpointUrl = ((AuthenticationApplication) Application.get()).getProperties().getProperty("endpoint-url");
			serverManager.setOPEndpointUrl(endpointUrl);

			Message message;

			if (OpenIDUtil.isDirectedIdentity(identity)) {

				Endpoint.log.debug("Creating positive directed identity authentication response...");
				message = serverManager.authResponse(
						parameters, 
						"xri://" + outIdentity, 
						"xri://" + outIdentity, 
						true);
			} else {

				Endpoint.log.debug("Creating positive authentication response...");
				message = serverManager.authResponse(
						parameters, 
						null, 
						null, 
						true);
			}

			if (message instanceof DirectError) {

				log.info("Sending message: " + message.keyValueFormEncoding());
				response.write(message.keyValueFormEncoding());
				return;
			}

			// sign the response

			Endpoint.log.debug("Signing authentication response...");

			if (message instanceof AuthSuccess) serverManager.sign((AuthSuccess) message);

			// option1: GET HTTP-redirect to the return_to URL

			Endpoint.log.debug("Sending authentication response via GET HTTP-redirect.");

			String redirectUrl = message.getDestinationUrl(true);
			log.info("Redirecting message: " + redirectUrl);
			RequestCycle.get().setRequestTarget(new RedirectRequestTarget(redirectUrl));

			// option2: HTML FORM Redirection
			//Endpoint.log.debug("Sending authentication response via HTML FORM redirect.");

			//Page page = new OpenIDRedirect(message);
			//requestCycle.setResponsePage(page);
			return;
		} else {

			// display the authentication page

			Page authenticationPage = new AuthenticationPage(authentication, parameters, identity);

			requestCycle.setResponsePage(authenticationPage);
		}
	}

	public void processCancel(RequestCycle requestCycle, ParameterList parameters, Authentication authentication) throws Exception {

		// log out user

		((AuthenticationSession) Session.get()).logoutUser();
	}

	public void processCheckAuthentication(RequestCycle requestCycle, ParameterList parameters) throws Exception {

		ServerManager serverManager = ((AuthenticationApplication) Application.get()).getServerManager();

		Response response = requestCycle.getResponse();

		// process a verification request

		log.debug("Creating verification response...");

		Message message = serverManager.verify(parameters);
		log.info("Sending message: " + message.keyValueFormEncoding());
		response.write(message.keyValueFormEncoding());
		return;
	}

	public void detach(RequestCycle requestCycle) {

	}

	/**
	 * This method is provided so that our Authentication i-service also works if the user enters an
	 * HXRI instead of just his simple i-name.
	 * @param hxri - maybe a HXRI
	 * @return
	 */
	private static String fixHXRI(String hxri) {

		String xri;

		if (hxri.contains("/=")) xri = hxri.substring(hxri.indexOf("/=") + 1);
		else if (hxri.contains("/@")) xri = hxri.substring(hxri.indexOf("/@") + 1);
		else return(hxri);

		log.debug("Fixing HXRI: " + hxri + " --> " + xri);

		return(xri);
	}
}
