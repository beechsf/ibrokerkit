package ibrokerkit.oauthfront.webpages.oauth;

import ibrokerkit.ibrokerstore.store.StoreUtil;
import ibrokerkit.ibrokerstore.store.User;
import ibrokerkit.iname4java.store.Xri;
import ibrokerkit.iname4java.store.XriStoreException;
import ibrokerkit.oauthfront.components.MyVelocityPanel;
import ibrokerkit.oauthfront.oauth.OauthSupport;
import ibrokerkit.oauthfront.oauth.UserInputAddService;
import ibrokerkit.oauthfront.webpages.BasePage;

import java.util.ArrayList;
import java.util.List;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.target.basic.RedirectRequestTarget;
import org.openxri.xml.SEPPath;
import org.openxri.xml.SEPType;
import org.openxri.xml.Service;

public class OauthAddServicePage extends BasePage {

	private static Log log = LogFactory.getLog(OauthAddServicePage.class.getName());

	private OAuthAccessor oAuthAccessor;
	private OAuthMessage requestMessage;

	private String xriOperation;
	private String xriIname;
	private Service xriService;
	private Xri xri;
	private User user;

	private List<Service> conflictingServices;

	@SuppressWarnings("unchecked")
	public OauthAddServicePage(OAuthAccessor oAuthAccessor, OAuthMessage requestMessage) {

		this.oAuthAccessor = oAuthAccessor;
		this.requestMessage = requestMessage;

		this.xriOperation = (String) oAuthAccessor.getProperty(OauthSupport.PROPERTY_XRI_OPERATION);
		this.xriIname = (String) oAuthAccessor.getProperty(OauthSupport.PROPERTY_XRI_INAME);
		this.xriService = (Service) oAuthAccessor.getProperty(OauthSupport.PROPERTY_XRI_SERVICE);
		this.xri = (Xri) oAuthAccessor.getProperty(OauthSupport.PROPERTY_XRI);
		this.user = (User) oAuthAccessor.getProperty(OauthSupport.PROPERTY_USER);

		// check if a conflicting service exists already

		List<Service> existingServices;

		try {

			existingServices = this.xri.getServices();
		} catch (XriStoreException ex) {

			throw new RuntimeException(ex);
		}

		this.conflictingServices = new ArrayList<Service> ();

		existingServicesLoop:
			for (Service existingService : existingServices) {

				List<SEPPath> existingServicePaths = existingService.getPaths();
				List<SEPType> existingServiceTypes = existingService.getTypes();

				for (SEPPath existingServicePath : existingServicePaths) {

					for (SEPPath xriServicePath : (List<SEPPath>) this.xriService.getPaths()) {

						if (existingServicePath.getValue().toLowerCase().equals(xriServicePath.getValue().toLowerCase())) {

							conflictingServices.add(existingService);
							continue existingServicesLoop;
						}
					}
				}

				for (SEPType existingServiceType : existingServiceTypes) {

					for (SEPType xriServiceType : (List<SEPType>) this.xriService.getTypes()) {

						if (existingServiceType.getValue().toLowerCase().equals(xriServiceType.getValue().toLowerCase())) {

							conflictingServices.add(existingService);
							continue existingServicesLoop;
						}
					}
				}
			}

		// find aliases and i-number of the i-name

		List<String> aliases = this.xri.getAliases();

		// extend velocity map

		this.velocityMap.put("operation", this.xriOperation);
		this.velocityMap.put("iname", this.xriIname);
		this.velocityMap.put("aliases", aliases);
		this.velocityMap.put("service", this.xriService);
		this.velocityMap.put("conflictingServices", this.conflictingServices);

		this.addVelocity(new MyVelocityPanel("velocity", Model.valueOf(this.velocityMap)) {

			private static final long serialVersionUID = 1413571371351435417L;

			@Override
			protected void addComponents() {

				this.add(new FeedbackPanel("feedbackPanel"));
				this.add(new OauthForm("oauthForm"));
			}

			@Override
			protected String getFilename() {

				return("velocity/oauth-oauth-addservice.vm");
			}
		});
	}

	private class OauthForm extends Form {

		private static final long serialVersionUID = -2258362832418615124L;

		private Boolean removeConflicting = Boolean.TRUE;
		private String pass;

		private WebMarkupContainer removeConflictingContainer;
		private CheckBox removeConflictingCheckBox;
		private PasswordTextField passTextField;
		private AuthorizeButton authorizeButton;
		private CancelButton cancelButton;

		private OauthForm(String id) {

			super(id);

			this.setModel(new CompoundPropertyModel(this));

			// create components

			this.removeConflictingContainer = new WebMarkupContainer("removeConflictingContainer");
			this.removeConflictingContainer.setVisible(OauthAddServicePage.this.conflictingServices.size() > 0);
			this.removeConflictingCheckBox = new CheckBox("removeConflicting");
			this.passTextField = new PasswordTextField("pass");
			this.passTextField.setLabel(new Model("Password"));
			this.passTextField.setRequired(true);
			this.authorizeButton = new AuthorizeButton("authorize");
			this.cancelButton = new CancelButton("cancel");
			this.cancelButton.setDefaultFormProcessing(false);

			// add components

			this.removeConflictingContainer.add(this.removeConflictingCheckBox);
			this.add(this.removeConflictingContainer);
			this.add(this.passTextField);
			this.add(this.authorizeButton);
			this.add(this.cancelButton);
		}

		private class AuthorizeButton extends Button {

			private static final long serialVersionUID = -2389367204516648703L;

			public AuthorizeButton(String id) {

				super(id);
			}

			@Override
			public void onSubmit() {

				// check claimed password

				OauthAddServicePage.log.debug("Check password: " + user.getPass());

				boolean authenticated = StoreUtil.checkPass(OauthAddServicePage.this.user.getPass(), OauthForm.this.pass);

				if (! authenticated) {

					this.error(OauthAddServicePage.this.getString("wrongpass"));
					return;
				}

				// remember user input

				UserInputAddService userInput = new UserInputAddService();

				if (Boolean.TRUE.equals(OauthForm.this.removeConflicting)) {

					userInput.setRemoveConflictingServices(OauthAddServicePage.this.conflictingServices);
				}

				OauthAddServicePage.this.oAuthAccessor.setProperty(OauthSupport.PROPERTY_USERINPUT, userInput);

				// authorize the request token and redirect back to the consumer

				try {

					// authorize the request token

					OauthSupport.markAsAuthorized(OauthAddServicePage.this.oAuthAccessor);

					// redirect back to the consumer

					String redirectUrl = OauthAddServicePage.this.requestMessage.getParameter("oauth_callback");

					if (redirectUrl == null || redirectUrl.equals("none")) {

						redirectUrl = OauthAddServicePage.this.oAuthAccessor.consumer.callbackURL;
					}

					redirectUrl = OAuth.addParameters(redirectUrl, "oauth_token", OauthAddServicePage.this.oAuthAccessor.requestToken);

					log.debug("Directing oAuth response to " + redirectUrl);
					RequestCycle.get().setRequestTarget(new RedirectRequestTarget(redirectUrl));
					return;
				} catch (Exception ex) {

					log.error(ex.getMessage(), ex);
					this.error(OauthAddServicePage.this.getString("fail") + ex.getMessage());
					return;
				}
			}
		}

		private class CancelButton extends Button {

			private static final long serialVersionUID = -8244392050142622285L;

			private CancelButton(String id) {

				super(id);
			}

			@Override
			public void onSubmit() {

				// redirect back to the consumer

				try {

					String redirectUrl = OauthAddServicePage.this.requestMessage.getParameter("oauth_callback");

					if (redirectUrl == null || redirectUrl.equals("none")) {

						redirectUrl = OauthAddServicePage.this.oAuthAccessor.consumer.callbackURL;
					}

					redirectUrl = OAuth.addParameters(redirectUrl, "oauth_token", OauthAddServicePage.this.oAuthAccessor.requestToken);

					OauthAddServicePage.log.debug("Sending oAuth response to " + redirectUrl);
					RequestCycle.get().setRequestTarget(new RedirectRequestTarget(redirectUrl));
				} catch (Exception ex) {

					log.error(ex.getMessage(), ex);
					this.error(OauthAddServicePage.this.getString("fail") + ex.getMessage());
					return;
				}
			}
		}

		public Boolean getRemoveConflicting() {
			return (this.removeConflicting);
		}
		public void setRemoveConflicting(Boolean removeConflicting) {
			this.removeConflicting = removeConflicting;
		}
		public String getPass() {
			return (this.pass);
		}
		public void setPass(String pass) {
			this.pass = pass;
		}
	}
}
