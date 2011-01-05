package ibrokerkit.ibrokerfront.webpages.xri.wizard;

import ibrokerkit.ibrokerfront.components.CancelButton;
import ibrokerkit.ibrokerfront.validators.URIPatternValidator;
import ibrokerkit.ibrokerfront.webapplication.IbrokerApplication;
import ibrokerkit.ibrokerfront.webapplication.IbrokerSession;
import ibrokerkit.ibrokerfront.webpages.BasePage;
import ibrokerkit.ibrokerfront.webpages.xri.YourXRIs;
import ibrokerkit.ibrokerstore.store.User;
import ibrokerkit.iname4java.store.Xri;
import ibrokerkit.iservicestore.store.Authentication;
import ibrokerkit.iservicestore.store.Contact;
import ibrokerkit.iservicestore.store.Forwarding;

import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.discovery.DiscoveryInformation;
import org.openxri.xml.AuthenticationService;
import org.openxri.xml.ContactService;
import org.openxri.xml.ForwardingService;
import org.openxri.xml.Service;
import org.openxri.xml.XDIService;

public class WizardXRI extends BasePage {

	private static final long serialVersionUID = 9057884625095789615L;

	private static Log log = LogFactory.getLog(WizardXRI.class.getName());

	private static final String FORWARDING_MAPPING_PATH_DEFAULT = "";
	private static final String FORWARDING_MAPPING_PATH_EMAIL = "(+email)";
	private static final String FORWARDING_MAPPING_PATH_BLOG = "(+blog)";

	private Xri xri;

	private FormData formData;

	public WizardXRI(Xri xri) {

		this.xri = xri;

		this.setTitle(this.getString("title"));

		// init default settings in the form based on user profile

		User user = ((IbrokerSession) this.getSession()).getUser();

		this.formData = new FormData();

		if (user.getOpenid().equals(Boolean.TRUE)) {

			this.formData.setOpenIDChoice(this.formData.openIDChoiceDelegate);
			this.formData.setOpenIDDelegate(user.getIdentifier());
			this.formData.setOpenIDServer(doOpenIDAutoDetect(user.getIdentifier()));
		} else {

			this.formData.setOpenIDChoice(this.formData.openIDChoiceStandalone);
		}

		if (user.getEmail() != null && ! user.getEmail().trim().equals("")) {

			this.formData.setEnableEmail(Boolean.TRUE);
			this.formData.setUrlEmail(user.getEmail());
		}

		// create and add components

		this.add(new MyForm("form", new CompoundPropertyModel(this.formData)));
	}

	private static class FormData implements Serializable {

		private static final long serialVersionUID = -8981205177940578385L;

		private String defaultChoice = "contactpage";
		private String defaultChoiceNothing = "nothing";
		private String defaultChoiceContactPage = "contactpage";
		private String defaultChoiceRedirect = "redirect";
		private String urlRedirect;
		private Boolean enableEmail = Boolean.FALSE;
		private String urlEmail;
		private Boolean enableBlog = Boolean.FALSE;
		private String urlBlog;
		private String openIDChoice = "standalone";
		private String openIDChoiceStandalone = "standalone";
		private String openIDChoiceDelegate = "delegate";
		private String openIDChoiceNone = "none";
		private String openIDDelegate;
		private String openIDServer;

		public String getDefaultChoice() {
			return (this.defaultChoice);
		}
		public void setDefaultChoice(String defaultChoice) {
			this.defaultChoice = defaultChoice;
		}
		public String getDefaultChoiceContactPage() {
			return (this.defaultChoiceContactPage);
		}
		public void setDefaultChoiceContactPage(String defaultChoiceContactPage) {
			this.defaultChoiceContactPage = defaultChoiceContactPage;
		}
		public String getDefaultChoiceNothing() {
			return (this.defaultChoiceNothing);
		}
		public void setDefaultChoiceNothing(String defaultChoiceNothing) {
			this.defaultChoiceNothing = defaultChoiceNothing;
		}
		public String getDefaultChoiceRedirect() {
			return (this.defaultChoiceRedirect);
		}
		public void setDefaultChoiceRedirect(String defaultChoiceRedirect) {
			this.defaultChoiceRedirect = defaultChoiceRedirect;
		}
		public String getOpenIDChoiceDelegate() {
			return (this.openIDChoiceDelegate);
		}
		public void setOpenIDChoiceDelegate(String openIDChoiceDelegate) {
			this.openIDChoiceDelegate = openIDChoiceDelegate;
		}
		public String getOpenIDChoiceNone() {
			return (this.openIDChoiceNone);
		}
		public void setOpenIDChoiceNone(String openIDChoiceNone) {
			this.openIDChoiceNone = openIDChoiceNone;
		}
		public String getOpenIDChoiceStandalone() {
			return (this.openIDChoiceStandalone);
		}
		public void setOpenIDChoiceStandalone(String openIDChoiceStandalone) {
			this.openIDChoiceStandalone = openIDChoiceStandalone;
		}
		public Boolean getEnableBlog() {
			return (this.enableBlog);
		}
		public void setEnableBlog(Boolean enableBlog) {
			this.enableBlog = enableBlog;
		}
		public Boolean getEnableEmail() {
			return (this.enableEmail);
		}
		public void setEnableEmail(Boolean enableEmail) {
			this.enableEmail = enableEmail;
		}
		public String getOpenIDDelegate() {
			return (this.openIDDelegate);
		}
		public void setOpenIDDelegate(String openIDDelegate) {
			this.openIDDelegate = openIDDelegate;
			if (this.openIDDelegate != null) this.openIDDelegate = openIDDelegate.trim();
		}
		public String getOpenIDChoice() {
			return (this.openIDChoice);
		}
		public void setOpenIDChoice(String openIDChoice) {
			this.openIDChoice = openIDChoice.trim();
		}
		public String getOpenIDServer() {
			return (this.openIDServer);
		}
		public void setOpenIDServer(String openIDServer) {
			this.openIDServer = openIDServer;
			if (this.openIDServer != null) this.openIDServer = openIDServer.trim();
		}
		public String getUrlBlog() {
			return (this.urlBlog);
		}
		public void setUrlBlog(String urlBlog) {
			this.urlBlog = urlBlog;
			if (this.urlBlog != null) this.urlBlog = this.urlBlog.trim();
			if (this.urlBlog != null && ! this.urlBlog.startsWith("http://")) this.urlBlog = "http://" + urlBlog;
		}
		public String getUrlRedirect() {
			return (this.urlRedirect);
		}
		public void setUrlRedirect(String urlRedirect) {
			this.urlRedirect = urlRedirect;
			if (this.urlRedirect != null) this.urlRedirect = urlRedirect.trim();
			if (this.urlRedirect != null && ! this.urlRedirect.startsWith("http://")) this.urlRedirect = "http://" + urlRedirect;
		}
		public String getUrlEmail() {
			return (this.urlEmail);
		}
		public void setUrlEmail(String urlEmail) {
			this.urlEmail = urlEmail;
			if (this.urlEmail != null) this.urlEmail = urlEmail.trim();
			if (this.urlEmail != null && ! this.urlEmail.startsWith("mailto:")) this.urlEmail = "mailto:" + urlEmail;
		}
	}

	private class MyForm extends Form {

		private static final long serialVersionUID = 9222589433948029830L;

		private RadioGroup defaultChoice;
		private Radio defaultChoiceNothing;
		private Radio defaultChoiceContactPage;
		private Radio defaultChoiceRedirect;
		private TextField urlRedirect;
		private CheckBox enableEmail;
		private TextField urlEmail;
		private CheckBox enableBlog;
		private TextField urlBlog;
		private RadioGroup openIDChoice;
		private Radio openIDChoiceStandalone;
		private Radio openIDChoiceDelegate;
		private Radio openIDChoiceNone;
		private TextField openIDDelegate;
		private TextField openIDServer;
		private AutoDetectButton autoDetect;
		private MySubmitButton submitButton;
		private CancelButton cancelButton;

		private MyForm(String id, IModel model) {

			super(id, model);

			// create components

			this.defaultChoice = new RadioGroup("defaultChoice");
			this.defaultChoiceNothing = new Radio("defaultChoiceNothing");
			this.defaultChoiceContactPage = new Radio("defaultChoiceContactPage");
			this.defaultChoiceRedirect = new Radio("defaultChoiceRedirect");
			this.urlRedirect = new TextField("urlRedirect");
			this.urlRedirect.setLabel(new Model("Default Forwarding"));
			this.enableEmail = new CheckBox("enableEmail");
			this.urlEmail = new TextField("urlEmail");
			this.urlEmail.add(new URIPatternValidator(false));
			this.urlEmail.setLabel(new Model("Email Forwarding"));
			this.enableBlog = new CheckBox("enableBlog");
			this.urlBlog = new TextField("urlBlog");
			this.urlBlog.add(new URIPatternValidator(false));
			this.urlBlog.setLabel(new Model("Weblog Address"));
			this.openIDChoice = new RadioGroup("openIDChoice");
			this.openIDChoiceStandalone = new Radio("openIDChoiceStandalone");
			this.openIDChoiceDelegate = new Radio("openIDChoiceDelegate");
			this.openIDChoiceNone = new Radio("openIDChoiceNone");
			this.openIDDelegate = new TextField("openIDDelegate");
			this.openIDServer = new TextField("openIDServer");
			this.openIDServer.setOutputMarkupId(true);
			this.autoDetect = new AutoDetectButton("autoDetect", this);
			this.submitButton = new MySubmitButton("submitButton");
			this.cancelButton = new CancelButton("cancelButton", YourXRIs.class);

			// add components

			this.add(new Label("qxri", WizardXRI.this.xri.toString()));
			this.defaultChoice.add(this.defaultChoiceNothing);
			this.defaultChoice.add(this.defaultChoiceContactPage);
			this.defaultChoice.add(this.defaultChoiceRedirect);
			this.defaultChoice.add(new Label("qxriContact", WizardXRI.this.xri + "/" + ContactService.CONTACT_PATH));
			this.defaultChoice.add(this.urlRedirect);
			this.add(this.defaultChoice);
			this.add(this.enableEmail);
			this.add(new Label("qxriEmail", WizardXRI.this.xri + "/" + FORWARDING_MAPPING_PATH_EMAIL));
			this.add(this.urlEmail);
			this.add(this.enableBlog);
			this.add(new Label("qxriBlog", WizardXRI.this.xri + "/" + FORWARDING_MAPPING_PATH_BLOG));
			this.add(this.urlBlog);
			this.openIDChoice.add(this.openIDChoiceStandalone);
			this.openIDChoice.add(this.openIDChoiceDelegate);
			this.openIDChoice.add(this.openIDChoiceNone);
			this.openIDChoice.add(new Label("qxriAuthentication", WizardXRI.this.xri.toString()));
			this.openIDChoice.add(new Label("qxriAuthentication2", WizardXRI.this.xri.toString()));
			this.openIDChoice.add(this.openIDDelegate);
			this.openIDChoice.add(this.openIDServer);
			this.openIDChoice.add(this.autoDetect);
			this.openIDChoice.add(this.autoDetect);
			this.add(this.openIDChoice);
			this.add(this.submitButton);
			this.add(this.cancelButton);
		}

		private class MySubmitButton extends Button {

			private static final long serialVersionUID = -5285533932550100952L;

			private MySubmitButton(String id) {

				super(id);
			}

			@Override
			public void onSubmit() {

				Properties properties = ((IbrokerApplication) this.getApplication()).getProperties();
				ibrokerkit.iservicestore.store.Store iserviceStore = ((IbrokerApplication) this.getApplication()).getIserviceStore();
				User user = ((IbrokerSession) this.getSession()).getUser();

				try {

					// delete all service endpoints and i-services

					WizardXRI.this.xri.deleteStandardServices();
					iserviceStore.deleteAllIServices(WizardXRI.this.xri.getAuthorityId());

					List<Service> services = new ArrayList<Service> ();

					// set up authentication i-service (if no delegation) and SEP

					if (WizardXRI.this.formData.openIDChoice.equals(WizardXRI.this.formData.openIDChoiceStandalone)) {

						WizardXRI.log.debug("Creating Authentication i-service for an authority.");

						Authentication authentication = iserviceStore.createAuthentication();
						authentication.setQxri(WizardXRI.this.xri.getAuthorityId()); 
						authentication.setName(WizardXRI.this.getString("newAuthenticationName"));
						authentication.setEnabled(Boolean.TRUE); 
						authentication.setIndx(user.getIdentifier()); 
						authentication.setPass(user.getPass());
						iserviceStore.updateObject(authentication);

						WizardXRI.log.debug("Creating Authentication SEP for " + WizardXRI.this.xri.getAuthorityId());

						services.add(
								new AuthenticationService(
										new URI[] { new URI(properties.getProperty("authentication-service")), new URI(properties.getProperty("authentication-service-https")) },
										properties.getProperty("providerid"),
										null,
										true));
					} else if (WizardXRI.this.formData.openIDChoice.equals(WizardXRI.this.formData.openIDChoiceDelegate)) {

						WizardXRI.log.debug("Creating delegating Authentication SEP for " + WizardXRI.this.xri.getAuthorityId());

						services.add(
								new AuthenticationService(
										new URI(WizardXRI.this.formData.openIDServer),
										null,
										WizardXRI.this.formData.openIDDelegate,
										true));
					} else {

						// no Authentication i-service desired
					}

					// set up contact i-service and SEP

					String description = "Contact page for " + user.getName();
					String forward = user.getEmail();

					WizardXRI.log.debug("Creating Contact i-service for " + WizardXRI.this.xri.getAuthorityId());

					Contact contact = iserviceStore.createContact();
					contact.setQxri(WizardXRI.this.xri.getAuthorityId()); 
					contact.setName(WizardXRI.this.getString("newContactName"));
					contact.setEnabled(Boolean.TRUE);
					contact.setIndx(user.getIdentifier()); 
					contact.setDescription(description); 
					contact.setForward(forward);
					iserviceStore.updateObject(contact);

					WizardXRI.log.debug("Creating Contact SEP for an authority.");

					boolean contactServiceMakeDefault = WizardXRI.this.formData.getDefaultChoice().equals(WizardXRI.this.formData.defaultChoiceContactPage);

					services.add(
							new ContactService(
									new URI(properties.getProperty("contact-service")),
									properties.getProperty("providerid"),
									contactServiceMakeDefault));

					// set up forwarding i-service and SEP

					Map<String, String> mappings = new HashMap<String, String> ();

					if (WizardXRI.this.formData.urlRedirect != null && ! WizardXRI.this.formData.urlRedirect.equals(""))
						mappings.put(FORWARDING_MAPPING_PATH_DEFAULT, WizardXRI.this.formData.urlRedirect);

					if (WizardXRI.this.formData.enableEmail.equals(Boolean.TRUE))
						mappings.put(FORWARDING_MAPPING_PATH_EMAIL, WizardXRI.this.formData.urlEmail);

					if (WizardXRI.this.formData.enableBlog.equals(Boolean.TRUE))
						mappings.put(FORWARDING_MAPPING_PATH_BLOG, WizardXRI.this.formData.urlBlog);

					WizardXRI.log.debug("Creating Forwarding i-service for " + WizardXRI.this.xri.getAuthorityId());

					Forwarding forwarding = iserviceStore.createForwarding();
					forwarding.setQxri(WizardXRI.this.xri.getAuthorityId()); 
					forwarding.setName(WizardXRI.this.getString("newForwardingName"));
					forwarding.setEnabled(Boolean.TRUE); 
					forwarding.setIndx(user.getIdentifier()); 
					forwarding.setMappings(mappings); 
					forwarding.setIndexPage(Boolean.TRUE); 
					forwarding.setErrorPage(Boolean.TRUE);
					iserviceStore.updateObject(forwarding);

					WizardXRI.log.debug("Creating Forwarding SEP for " + WizardXRI.this.xri.getAuthorityId());

					boolean forwardingServiceMakeDefault = WizardXRI.this.formData.getDefaultChoice().equals(WizardXRI.this.formData.defaultChoiceRedirect);

					services.add(
							new ForwardingService(
									new URI(properties.getProperty("forwarding-service")),
									properties.getProperty("providerid"),
									forwardingServiceMakeDefault,
									true));

					// set up XDI SEP

					services.add(
							new XDIService(
									new URI(properties.getProperty("xdi-service") + WizardXRI.this.xri.getCanonicalID().getValue() + "/"),
									properties.getProperty("providerid")));

					// add service endpoints

					WizardXRI.this.xri.addServices(services.toArray(new Service[services.size()]));
				} catch (Exception ex) {

					throw new RuntimeException("Problem while setting up the XRI.", ex);
				}

				Page page = new YourXRIs();
				page.info(WizardXRI.this.getString("success"));
				this.setResponsePage(page);
			}
		}

		/**
		 * Ajax button for auto-detecting the OpenID server of a given identifier
		 * @author =peacekeeper
		 */
		private class AutoDetectButton extends AjaxSubmitLink {

			private static final long serialVersionUID = 1937766223131346865L;

			private AutoDetectButton(String id, Form form) {

				super(id, form);
			}

			@Override
			public void onSubmit(AjaxRequestTarget target, Form form) {

				String openIDServer = doOpenIDAutoDetect(WizardXRI.this.formData.getOpenIDDelegate());

				WizardXRI.this.formData.setOpenIDServer(openIDServer);

				if (target != null) target.addComponent(MyForm.this.openIDServer);
			}
		}
	}

	protected String doOpenIDAutoDetect(String delegate) {

		ConsumerManager consumerManager = ((IbrokerApplication) this.getApplication()).getConsumerManager();

		List<?> discoveries = null;

		try {

			discoveries = consumerManager.discover(delegate);

			if (discoveries == null || discoveries.size() < 1) {

				return(getString("openid-detect-notfound"));
			} else {

				URL server = ((DiscoveryInformation) discoveries.get(0)).getOPEndpoint();
				return(server.toString());
			}
		} catch (Exception ex) {

			return(getString("openid-detect-error") + " " + ex.getLocalizedMessage());
		}
	}
}
