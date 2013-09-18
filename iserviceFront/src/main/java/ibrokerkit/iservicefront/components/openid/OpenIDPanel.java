package ibrokerkit.iservicefront.components.openid;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.http.handler.RedirectRequestHandler;
import org.apache.wicket.request.mapper.mount.MountMapper;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.InMemoryConsumerAssociationStore;
import org.openid4java.consumer.InMemoryNonceVerifier;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.ParameterList;

public class OpenIDPanel extends Panel {

	private static final long serialVersionUID = 1L;

	private static Log log = LogFactory.getLog(OpenIDPanel.class.getName());

	private static final String OPENID_MOUNT_PATH = "OpenIDCallback";

	private TextField<String> openidTextField;
	private Button authenticateButton;
	private Label openidAuthenticatedLabel;

	private boolean authenticated;

	private DiscoveryInformation discoveryInformation;

	public OpenIDPanel(String id, IModel<String> model) {

		super(id, model);

		this.authenticated = false;

		// create components

		this.openidTextField = new TextField<String> ("openid", model);
		this.openidTextField.setLabel(new Model<String> ("OpenID"));
		this.authenticateButton = new AuthenticateButton("authenticateButton");
		this.authenticateButton.setDefaultFormProcessing(false);
		this.openidAuthenticatedLabel = new Label("openidAuthenticated", "");

		// add components

		this.add(this.openidTextField);
		this.add(this.authenticateButton);
		this.add(this.openidAuthenticatedLabel);
	}

	@SuppressWarnings("unchecked")
	public OpenIDPanel(String id) {

		super(id);

		this.authenticated = false;

		// create components

		this.openidTextField = new TextField<String> ("openid", (IModel<String>) this.getDefaultModel());
		this.openidTextField.setLabel(new Model<String> ("OpenID"));
		this.authenticateButton = new AuthenticateButton("authenticateButton");
		this.authenticateButton.setDefaultFormProcessing(false);
		this.openidAuthenticatedLabel = new Label("openidAuthenticated", "");

		// add components

		this.add(this.openidTextField);
		this.add(this.authenticateButton);
		this.add(this.openidAuthenticatedLabel);
	}

	@Override
	protected void onBeforeRender() {

		super.onBeforeRender();

		this.openidTextField.setVisible(! this.authenticated);
		this.authenticateButton.setVisible(! this.authenticated);
		this.openidAuthenticatedLabel.setDefaultModelObject(this.getDefaultModelObjectAsString() + " (Authenticated)");
		this.openidAuthenticatedLabel.setVisible(this.authenticated);
	}

	public void setRequired(boolean required) {

		this.openidTextField.setRequired(required);
	}

	public boolean isAuthenticated() {

		return(this.authenticated);
	}

	public void setAuthenticated(boolean authenticated) {

		this.authenticated = authenticated;
	}

	private class AuthenticateButton extends Button {

		private static final long serialVersionUID = 9823792834327L;

		public AuthenticateButton(String id) {

			super(id);
		}

		@Override
		public void onSubmit() {

			OpenIDPanel.log.debug("Beginning OpenID Login.");

			// determine realm and return_to URL where we will receive
			// the authentication responses from the OpenID provider

			String realm = ((ServletWebRequest) OpenIDPanel.this.getRequestCycle().getRequest()).getContainerRequest().getRequestURL().toString();
			if (realm.indexOf('/', 8) > 0) realm = realm.substring(0, realm.indexOf('/', 8));
			realm += ((WebApplication) OpenIDPanel.this.getApplication()).getWicketFilter().getFilterConfig().getServletContext().getContextPath();

			String returnToUrl = realm;
			if (! returnToUrl.endsWith("/")) returnToUrl += "/";
			returnToUrl += OPENID_MOUNT_PATH;

			// mount endpoint

			((WebApplication) Application.get()).unmount(OPENID_MOUNT_PATH);
			((WebApplication) Application.get()).mount(new MountMapper(OPENID_MOUNT_PATH, new OpenIDEndpoint()));

			// perform discovery on the user-supplied identifier

			String identifier = OpenIDPanel.this.openidTextField.getInput();
			OpenIDPanel.this.setDefaultModelObject(identifier);

			OpenIDPanel.log.debug("Performing discovery on " + identifier);

			List<?> discoveries;

			try {

				discoveries = MyConsumerManager.getInstance().discover(identifier);
			} catch (Exception ex) {

				OpenIDPanel.log.warn(ex);
				discoveries = null;
			}

			// check if we found anything

			if (discoveries == null || discoveries.size() < 1) {

				error(OpenIDPanel.this.getString("nodiscoveries"));
				OpenIDPanel.log.error("No discoveries.");
				return;
			}

			OpenIDPanel.log.debug(Integer.toString(discoveries.size()) + " discoveries detected.");

			// attempt to associate with an OpenID provider
			// and retrieve one service endpoint for authentication

			OpenIDPanel.log.debug("Trying to associate with an OpenID provider.");

			try {

				OpenIDPanel.this.discoveryInformation = MyConsumerManager.getInstance().associate(discoveries);
			} catch (Exception ex) {

				error(OpenIDPanel.this.getString("noassociation") + ex.getMessage());
				OpenIDPanel.log.error("Problem while associating with OpenID IdP.", ex);
				return;
			}

			OpenIDPanel.log.debug("Associated with " + OpenIDPanel.this.discoveryInformation.getOPEndpoint() + 
					", Claimed Identifier=" + (OpenIDPanel.this.discoveryInformation.getClaimedIdentifier() != null ? OpenIDPanel.this.discoveryInformation.getClaimedIdentifier().getIdentifier() : "(none)")  + 
					", Delegate Identifier=" + OpenIDPanel.this.discoveryInformation.getDelegateIdentifier());

			// obtain a AuthRequest message to be sent to the OpenID provider

			OpenIDPanel.log.debug("Trying to prepare authentication request.");

			AuthRequest authReq;

			try {

				authReq = MyConsumerManager.getInstance().authenticate(OpenIDPanel.this.discoveryInformation, returnToUrl, realm);
			} catch (Exception ex) {

				error(OpenIDPanel.this.getString("norequest") + ex.getMessage());
				OpenIDPanel.log.error("Problem while preparing authentication request.", ex);
				return;
			}

			// perform the authentication request by redirecting to the IdP endpoint

			String url = authReq.getDestinationUrl(true);
			this.getRequestCycle().scheduleRequestHandlerAfterCurrent(new RedirectRequestHandler(url));
			return;
		}
	}

	/*
	 * The OpenID endpoint
	 */


	public class OpenIDEndpoint implements IRequestHandler {

		@Override
		public void detach(IRequestCycle requestCycle) {

		}

		@Override
		public void respond(IRequestCycle requestCycle) {

			// extract the parameters from the authentication response
			// (which comes in as a HTTP request from the OpenID provider)

			ParameterList parameters = new ParameterList(((ServletWebRequest) requestCycle.getRequest()).getContainerRequest().getParameterMap());

			// retrieve the page our OpenID panel is on

			Page page = OpenIDPanel.this.getPage();

			// extract the receiving URL from the HTTP request

			HttpServletRequest httpReq = ((ServletWebRequest) requestCycle.getRequest()).getContainerRequest();

			StringBuffer receivingURL = httpReq.getRequestURL();
			String queryString = httpReq.getQueryString();

			if (queryString != null && queryString.length() > 0) receivingURL.append("?").append(httpReq.getQueryString());

			log.debug("Got OpenID response to our login request on " + receivingURL.toString() + ".");

			// verify the response; ConsumerManager needs to be the same
			// (static) instance used to place the authentication request

			VerificationResult verification;

			try {

				verification = MyConsumerManager.getInstance().verify(
						receivingURL.toString(), 
						parameters, 
						OpenIDPanel.this.discoveryInformation);
			} catch (Exception ex) {

				throw new RuntimeException("Problem while verifying OpenID response.", ex);
			}

			log.debug("Verified OpenID response: " + verification.getStatusMsg());

			// examine the verification result and extract the verified identifier

			Identifier identifier = verification.getVerifiedId();

			if (identifier == null) {

				log.debug("Not verified.");

				// send user back to page

				requestCycle.scheduleRequestHandlerAfterCurrent(new RenderPageRequestHandler(new PageProvider(page)));
				return;
			}

			log.debug("Verified: " + identifier.getIdentifier());

			// user is authenticated now

			OpenIDPanel.this.setAuthenticated(true);
			requestCycle.scheduleRequestHandlerAfterCurrent(new RenderPageRequestHandler(new PageProvider(page)));
			return;
		}
	}

	/*
	 * The ConsumerManager
	 */

	private static class MyConsumerManager extends ConsumerManager {

		private static transient MyConsumerManager instance = null;

		private MyConsumerManager() throws ConsumerException {

			super();

			this.setAssociations(new InMemoryConsumerAssociationStore());
			this.setNonceVerifier(new InMemoryNonceVerifier(120));
		}

		private static MyConsumerManager getInstance() {

			try {

				if (instance == null) instance = new MyConsumerManager();
			} catch (ConsumerException ex) {

				instance = null;
			}

			return(instance);
		}
	}
}
