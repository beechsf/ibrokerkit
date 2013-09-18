package ibrokerkit.iservicefront.authentication.webpages.iservice;

import ibrokerkit.iservicefront.authentication.webapplication.AuthenticationApplication;
import ibrokerkit.iservicefront.authentication.webapplication.AuthenticationSession;
import ibrokerkit.iservicefront.authentication.webpages.BasePage;
import ibrokerkit.iservicefront.behaviors.DefaultFocusBehavior;
import ibrokerkit.iservicefront.components.MyVelocityPanel;
import ibrokerkit.iservicestore.store.Authentication;
import ibrokerkit.iservicestore.store.StoreUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Application;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.target.basic.RedirectRequestTarget;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.DirectError;
import org.openid4java.message.Message;
import org.openid4java.message.ParameterList;
import org.openid4java.server.ServerManager;
import org.openxri.XRI;
import org.openxri.XRIAuthority;
import org.openxri.store.Authority;

public class AuthenticationPage extends BasePage {

	private static final long serialVersionUID = 6746092032675507969L;

	protected final static Log log = LogFactory.getLog(AuthenticationPage.class.getName());

	private Authentication authentication;
	private ParameterList parameters;
	private String identity;

	public AuthenticationPage(Authentication authentication, ParameterList parameters, String identity) {

		this.authentication = authentication;
		this.parameters = parameters;
		this.identity = identity;

		// extend velocity map

		this.velocityMap.put("authentication", this.authentication);
		this.velocityMap.put("parameters", this.parameters);
		this.velocityMap.put("identity", this.identity);

		this.addVelocity(new MyVelocityPanel("velocity", Model.valueOf(this.velocityMap)) {

			private static final long serialVersionUID = 2387469837463456L;

			@Override
			protected void addComponents() {

				this.add(new FeedbackPanel("feedbackPanel"));
				this.add(new AuthenticationForm("authenticationForm"));
			}

			@Override
			protected String getFilename() {

				return("velocity/authentication-authentication.vm");
			}
		});
	}

	private class AuthenticationForm extends Form {

		private static final long serialVersionUID = -1944754353721888791L;

		private String iname;
		private String pass;
		private Boolean staySignedIn = Boolean.FALSE;

		private WebMarkupContainer inameWebMarkupContainer;
		private TextField inameTextField;
		private PasswordTextField passTextField;
		private CheckBox staySignedInCheckBox;
		private AuthenticateButton authenticateButton;
		private CancelButton cancelButton;

		private AuthenticationForm(String id) {

			super(id);

			this.setModel(new CompoundPropertyModel(this));

			// create and add components

			this.inameWebMarkupContainer = new WebMarkupContainer("inameContainer");
			this.inameWebMarkupContainer.setVisible(OpenIDUtil.isDirectedIdentity(AuthenticationPage.this.identity));
			this.inameTextField = new TextField("iname");
			this.inameTextField.setLabel(new Model("I-Name"));
			this.inameTextField.setRequired(OpenIDUtil.isDirectedIdentity(AuthenticationPage.this.identity));
			this.passTextField = new PasswordTextField("pass");
			this.passTextField.setLabel(new Model("Password"));
			this.passTextField.setRequired(true);
			this.staySignedInCheckBox = new CheckBox("staySignedIn");
			this.authenticateButton = new AuthenticateButton("authenticate");
			this.cancelButton = new CancelButton("cancel");
			this.cancelButton.setDefaultFormProcessing(false);

			if (OpenIDUtil.isDirectedIdentity(AuthenticationPage.this.identity))
				this.inameTextField.add(new DefaultFocusBehavior());
			else
				this.passTextField.add(new DefaultFocusBehavior());

			this.inameWebMarkupContainer.add(this.inameTextField);
			this.add(this.inameWebMarkupContainer);
			this.add(this.passTextField);
			this.add(this.staySignedInCheckBox);
			this.add(this.authenticateButton);
			this.add(this.cancelButton);
		}

		private class AuthenticateButton extends Button {

			private static final long serialVersionUID = -5642434625788814775L;

			private AuthenticateButton(String id) {

				super(id);
			}

			@Override
			public void onSubmit() {

				ibrokerkit.iservicestore.store.Store iserviceStore = ((AuthenticationApplication) Application.get()).getIserviceStore();
				org.openxri.store.Store openxriStore = ((AuthenticationApplication) Application.get()).getOpenxriStore();

				ServerManager serverManager = ((AuthenticationApplication) Application.get()).getServerManager();

				// check directed identity

				Authentication authentication;
				String outIdentity;

				if (OpenIDUtil.isDirectedIdentity(AuthenticationPage.this.identity)) {

					try {

						XRI qxri = new XRI(AuthenticationForm.this.iname);

						authentication = iserviceStore.findAuthentication(qxri.getAuthorityPath().toString());
						Authority authority = openxriStore.localLookup((XRIAuthority) qxri.getAuthorityPath());

						// if we found none for the qxri, look for one for the authority id

						if (authentication == null || authentication.getEnabled().equals(Boolean.FALSE)) {

							if (authority != null) {

								authentication = iserviceStore.findAuthentication(authority.getId().toString());
							}
						}

						// override identity

						outIdentity = (authority == null) ? null : authority.getXrd().getCanonicalID().getValue();
					} catch (Exception ex) {

						log.error(ex);
						this.error(AuthenticationPage.this.getString("openidex") + ex.getMessage());
						return;
					}
				} else {

					authentication = AuthenticationPage.this.authentication;
					outIdentity = AuthenticationPage.this.identity;
				}

				if (authentication == null || outIdentity == null) {

					this.error(AuthenticationPage.this.getString("wrongpass"));
					return;
				}

				AuthenticationPage.log.debug("Using Authentication: " + authentication.getId());
				AuthenticationPage.log.debug("Using out identity: " + outIdentity);

				// check claimed password

				AuthenticationPage.log.debug("Check password: " + authentication.getPass());

				boolean authenticated = authentication != null && StoreUtil.checkPass(authentication.getPass(), AuthenticationForm.this.pass);

				if (! authenticated) {

					this.error(AuthenticationPage.this.getString("wrongpass"));
					return;
				}

				// login user in session

				if (AuthenticationForm.this.staySignedIn.booleanValue()) {

					AuthenticationPage.log.debug("Signing in user...");

					((AuthenticationSession) this.getSession()).loginUser(outIdentity);
				}

				// create OpenID response

				String endpointUrl = ((AuthenticationApplication) this.getApplication()).getProperties().getProperty("authentication-endpoint-url");
				serverManager.setOPEndpointUrl(endpointUrl);

				Message message;

				if (OpenIDUtil.isDirectedIdentity(AuthenticationPage.this.identity)) {

					AuthenticationPage.log.debug("Creating positive directed identity authentication response...");
					message = serverManager.authResponse(
							AuthenticationPage.this.parameters, 
							"xri://" + outIdentity, 
							"xri://" + outIdentity, 
							authenticated);
				} else {

					AuthenticationPage.log.debug("Creating positive authentication response...");
					message = serverManager.authResponse(
							AuthenticationPage.this.parameters, 
							null, 
							null, 
							authenticated);
				}

				if (message instanceof DirectError) {

					log.info("Sending message: " + message.keyValueFormEncoding());
					this.getResponse().write(message.keyValueFormEncoding());
					return;
				}

				// AX requested?

				/*
				if (AuthenticationPage.this.authRequest.hasExtension(AxMessage.OPENID_NS_AX)) {

				MessageExtension messageExtension = AuthenticationPage.this.authRequest.getExtension(AxMessage.OPENID_NS_AX);

				AuthenticationPage.log.debug("Processing AX message extension: " + messageExtension.getClass().getName());

				if (messageExtension instanceof FetchRequest) {

					FetchRequest fetchRequest = (FetchRequest) messageExtension;
					Map required = fetchRequest.getAttributes(true);
					Map optional = fetchRequest.getAttributes(false);

					Map userData = new HashMap();
					userData.put("email", userData.get(3));

					FetchResponse fetchResponse = FetchResponse.createFetchResponse(fetchRequest, userData);
					message.addExtension(fetchResponse);
				}
				}*/

				// sign the response

				AuthenticationPage.log.debug("Signing authentication response...");

				try {

					if (message instanceof AuthSuccess) serverManager.sign((AuthSuccess) message);
				} catch (Exception ex) {

					log.error(ex);
					this.error(AuthenticationPage.this.getString("openidex") + ex.getMessage());
					return;
				}

				// option1: GET HTTP-redirect to the return_to URL

				AuthenticationPage.log.debug("Sending authentication response via GET HTTP-redirect.");

				String redirectUrl = message.getDestinationUrl(true);
				log.info("Redirecting message: " + redirectUrl);
				RequestCycle.get().setRequestTarget(new RedirectRequestTarget(redirectUrl));

				// option2: HTML FORM redirect

				//AuthenticationPage.log.debug("Sending authentication response via HTML FORM redirect.");
				//Page page = new OpenIDRedirect(message);
				//this.setResponsePage(page);
				return;
			}
		}

		private class CancelButton extends Button {

			private static final long serialVersionUID = -8244392050142622285L;

			private CancelButton(String id) {

				super(id);
			}

			@Override
			public void onSubmit() {

				ServerManager serverManager = ((AuthenticationApplication) Application.get()).getServerManager();

				// create OpenID response

				String endpointUrl = ((AuthenticationApplication) this.getApplication()).getProperties().getProperty("authentication-endpoint-url");
				serverManager.setOPEndpointUrl(endpointUrl);

				AuthenticationPage.log.debug("Creating negative authentication response...");
				Message message = serverManager.authResponse(
						AuthenticationPage.this.parameters, 
						identity, 
						identity, 
						false);

				if (message instanceof DirectError) {

					log.info("Sending message: " + message.keyValueFormEncoding());
					this.getResponse().write(message.keyValueFormEncoding());
					return;
				}

				// option1: GET HTTP-redirect to the return_to URL

				AuthenticationPage.log.debug("Sending authentication response via GET HTTP-redirect.");

				String redirectUrl = message.getDestinationUrl(true);
				log.info("Redirecting message: " + redirectUrl);
				RequestCycle.get().setRequestTarget(new RedirectRequestTarget(redirectUrl));

				// option2: HTML FORM redirect

				//AuthenticationPage.log.debug("Sending authentication response via HTML FORM redirect.");
				//Page page = new OpenIDRedirect(message);
				//this.setResponsePage(page);
				return;
			}
		}

		public String getIname() {

			return (this.iname);
		}

		public void setIname(String iname) {

			this.iname = iname;
		}

		public void setPass(String pass) {

			this.pass = pass;
		}

		public String getPass() {

			return(this.pass);
		}

		public Boolean getStaySignedIn() {

			return(this.staySignedIn);
		}

		public void setStaySignedIn(Boolean staySignedIn) {

			this.staySignedIn = staySignedIn;
		}
	}
}
