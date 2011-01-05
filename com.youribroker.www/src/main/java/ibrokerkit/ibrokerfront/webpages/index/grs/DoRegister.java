package ibrokerkit.ibrokerfront.webpages.index.grs;

import ibrokerkit.ibrokerfront.email.Email;
import ibrokerkit.ibrokerfront.models.CountryCodesModel;
import ibrokerkit.ibrokerfront.models.UserInamesModel;
import ibrokerkit.ibrokerfront.webapplication.IbrokerApplication;
import ibrokerkit.ibrokerfront.webapplication.IbrokerSession;
import ibrokerkit.ibrokerfront.webpages.PaymentCallbackPage;
import ibrokerkit.ibrokerfront.webpages.xri.wizard.WizardXRI;
import ibrokerkit.ibrokerstore.store.Store;
import ibrokerkit.ibrokerstore.store.StoreUtil;
import ibrokerkit.ibrokerstore.store.User;
import ibrokerkit.iname4java.store.Xri;
import ibrokerkit.iname4java.store.XriStore;
import ibrokerkit.iname4java.store.XriStoreException;
import ibrokerkit.iname4java.store.impl.grs.GrsXriData;

import java.io.StringWriter;
import java.net.URLDecoder;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.wicket.Application;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.apache.wicket.validation.validator.PatternValidator;

public class DoRegister extends PaymentCallbackPage {

	private static final long serialVersionUID = 2786725384239368435L;

	private static Log log = LogFactory.getLog(DoRegister.class.getName());

	private static final int YEARS = 1;

	private static final Pattern PATTERN_VOICE = Pattern.compile("(\\+[0-9]{1,3}[0-9 \\.\\-/:]{1,15})?");
	private static final Pattern PATTERN_FAX = Pattern.compile("(\\+[0-9]{1,3}[0-9 \\.\\-/:]{1,15})?");
	private static final Pattern PATTERN_PAGER = Pattern.compile("(\\+[0-9]{1,3}[0-9 \\.\\-/:]{1,15})?");

	private String iname;

	// form data

	private String name;
	private String organization;
	private String street1;
	private String street2;
	private String postalCode;
	private String city;
	private String state;
	private String countryCode;
	private String primaryVoice;
	private String secondaryVoice;
	private String fax;
	private String primaryEmail;
	private String secondaryEmail;
	private String pager;
	private Xri synonymXri;
	private String pass;
	private String pass2;

	public DoRegister(PageParameters pageParameters) {

		this(pageParameters, true);
	}

	public DoRegister(PageParameters pageParameters, boolean verify) {

		super(pageParameters, Index.class, verify, 1);

		this.setTitle(this.getString("title"));

		// get passed-in parameters

		try {

			this.iname = URLDecoder.decode(pageParameters.getString("iname"), "UTF-8");
		} catch (Exception ex) {

			throw new RuntimeException(ex);
		}

		this.name = pageParameters.getString("card_holder_name");
		this.street1 = pageParameters.getString("street_address");
		this.postalCode = pageParameters.getString("zip");
		this.city = pageParameters.getString("city");
		this.state = pageParameters.getString("state");
		this.countryCode = CountryCodesModel.code3ToCode2(pageParameters.getString("country"));
		this.primaryVoice = pageParameters.getString("phone");
		this.primaryEmail = pageParameters.getString("email");

		if (this.primaryVoice != null) this.primaryVoice = fixPhone(this.primaryVoice, pageParameters.getString("country"));

		// create and add components

		this.add(new Label("iname", this.iname));
		this.add(new MyForm("form", new CompoundPropertyModel(this)));
	}

	private static String fixPhone(String phone, String country) {

		String fixedPhone = phone.trim();

		if (fixedPhone.indexOf('+') != 0) fixedPhone = '+' + CountryCodesModel.code3ToPrefix(country) + ' ' + fixedPhone;

		if ((fixedPhone.indexOf(' ') < 0 || fixedPhone.indexOf(' ') > 4) &&
				(fixedPhone.indexOf('/') < 0 || fixedPhone.indexOf('/') > 4) &&
				(fixedPhone.indexOf('.') < 0 || fixedPhone.indexOf('.') > 4) &&
				(fixedPhone.indexOf(':') < 0 || fixedPhone.indexOf(':') > 4) &&
				(fixedPhone.indexOf('-') < 0 || fixedPhone.indexOf('-') > 4)) {

			fixedPhone = fixedPhone.substring(0, 3) + ' ' + fixedPhone.substring(3);
		}

		return(fixedPhone);
	}

	public class MyForm extends Form {

		private static final long serialVersionUID = -1976441444627103562L;

		// form components

		private Label inameLabel;
		private TextField nameTextField;
		private TextField organizationTextField;
		private TextField street1TextField;
		private TextField street2TextField;
		private TextField postalCodeTextField;
		private TextField cityTextField;
		private TextField stateTextField;
		private DropDownChoice countryCodeDropDownChoice;
		private TextField primaryVoiceTextField;
		private TextField secondaryVoiceTextField;
		private TextField faxTextField;
		private TextField primaryEmailTextField;
		private TextField secondaryEmailTextField;
		private TextField pagerTextField;
		private RadioGroup accountRadioGroup;
		private Radio accountCurrentRadio;
		private Radio accountSynonymRadio;
		private Radio accountNewRadio;
		private DropDownChoice synonymXriDropDownChoice;
		private PasswordTextField passPasswordTextField;
		private PasswordTextField pass2PasswordTextField;

		public MyForm(String id, IModel model) {

			super(id, model);

			// create components

			CountryCodesModel countryCodesModel = new CountryCodesModel();
			UserInamesModel userInamesModel = new UserInamesModel();

			this.inameLabel = new Label("iname", DoRegister.this.iname);
			this.nameTextField = new TextField("name");
			this.nameTextField.setLabel(new Model("Name"));
			this.nameTextField.setRequired(true);
			this.organizationTextField = new TextField("organization");
			this.organizationTextField.setLabel(new Model("Organization"));
			this.organizationTextField.setRequired(false);
			this.street1TextField = new TextField("street1");
			this.street1TextField.setLabel(new Model("Street"));
			this.street1TextField.setRequired(true);
			this.street2TextField = new TextField("street2");
			this.street2TextField.setLabel(new Model("Street"));
			this.street2TextField.setRequired(false);
			this.postalCodeTextField = new TextField("postalCode");
			this.postalCodeTextField.setLabel(new Model("Postal Code"));
			this.postalCodeTextField.setRequired(true);
			this.cityTextField = new TextField("city");
			this.cityTextField.setLabel(new Model("City"));
			this.cityTextField.setRequired(true);
			this.stateTextField = new TextField("state");
			this.stateTextField.setLabel(new Model("State/Province"));
			this.stateTextField.setRequired(false);
			this.countryCodeDropDownChoice = new DropDownChoice("countryCode", countryCodesModel, (IChoiceRenderer) countryCodesModel);
			this.countryCodeDropDownChoice.setLabel(new Model("Country Code"));
			this.countryCodeDropDownChoice.setRequired(true);
			this.primaryVoiceTextField = new TextField("primaryVoice");
			this.primaryVoiceTextField.setLabel(new Model("Primary Phone"));
			this.primaryVoiceTextField.setRequired(true);
			this.primaryVoiceTextField.add(new PatternValidator(PATTERN_VOICE));
			this.secondaryVoiceTextField = new TextField("secondaryVoice");
			this.secondaryVoiceTextField.setLabel(new Model("Secondary Phone"));
			this.secondaryVoiceTextField.setRequired(false);
			this.secondaryVoiceTextField.add(new PatternValidator(PATTERN_VOICE));
			this.faxTextField = new TextField("fax");
			this.faxTextField.setLabel(new Model("Fax"));
			this.faxTextField.setRequired(false);
			this.faxTextField.add(new PatternValidator(PATTERN_FAX));
			this.primaryEmailTextField = new TextField("primaryEmail");
			this.primaryEmailTextField.setLabel(new Model("Primary Email"));
			this.primaryEmailTextField.setRequired(true);
			this.primaryEmailTextField.add(EmailAddressValidator.getInstance());
			this.secondaryEmailTextField = new TextField("secondaryEmail");
			this.secondaryEmailTextField.setLabel(new Model("Secondary Email"));
			this.secondaryEmailTextField.setRequired(false);
			this.secondaryEmailTextField.add(EmailAddressValidator.getInstance());
			this.pagerTextField = new TextField("pager");
			this.pagerTextField.setLabel(new Model("Pager"));
			this.pagerTextField.setRequired(false);
			this.pagerTextField.add(new PatternValidator(PATTERN_PAGER));
			this.accountRadioGroup = new RadioGroup("account", new Model());
			this.accountRadioGroup.setRequired(true);
			this.accountCurrentRadio = new Radio("accountcurrent", new Model("current"));
			this.accountSynonymRadio = new Radio("accountsynonym", new Model("synonym"));
			this.accountNewRadio = new Radio("accountnew", new Model("new"));
			this.synonymXriDropDownChoice = new DropDownChoice("synonymXri", userInamesModel, (IChoiceRenderer) userInamesModel);
			this.synonymXriDropDownChoice.setLabel(new Model("XRI"));
			this.synonymXriDropDownChoice.setRequired(false);
			this.passPasswordTextField = new PasswordTextField("pass");
			this.passPasswordTextField.setLabel(new Model("Password"));
			this.passPasswordTextField.setRequired(false);
			this.pass2PasswordTextField = new PasswordTextField("pass2");
			this.pass2PasswordTextField.setLabel(new Model("Password 2"));
			this.pass2PasswordTextField.setRequired(false);

			// set default account behavior

			if (((IbrokerSession) this.getSession()).isLoggedIn()) {

				this.accountRadioGroup.setModelObject("current");
			} else {

				this.accountRadioGroup.setModelObject("new");
				this.accountRadioGroup.setVisible(false);
			}

			// add components

			this.add(this.inameLabel);
			this.add(this.nameTextField);
			this.add(this.organizationTextField);
			this.add(this.street1TextField);
			this.add(this.street2TextField);
			this.add(this.postalCodeTextField);
			this.add(this.cityTextField);
			this.add(this.stateTextField);
			this.add(this.countryCodeDropDownChoice);
			this.add(this.primaryVoiceTextField);
			this.add(this.secondaryVoiceTextField);
			this.add(this.faxTextField);
			this.add(this.primaryEmailTextField);
			this.add(this.secondaryEmailTextField);
			this.add(this.pagerTextField);
			this.add(this.accountRadioGroup);
			this.accountRadioGroup.add(this.accountCurrentRadio);
			this.accountRadioGroup.add(this.accountSynonymRadio);
			this.accountRadioGroup.add(this.accountNewRadio);
			this.accountRadioGroup.add(this.synonymXriDropDownChoice);
			this.add(this.passPasswordTextField);
			this.add(this.pass2PasswordTextField);

			// add validators

			this.add(new EqualPasswordInputValidator(this.passPasswordTextField, this.pass2PasswordTextField));
			this.add(new MyValidator());
		}

		private class MyValidator extends AbstractFormValidator {

			private static final long serialVersionUID = 4763487922865831322L;

			private FormComponent[] dependentFormComponents;

			private MyValidator() {

				this.dependentFormComponents = new FormComponent[0];			}

			public FormComponent[] getDependentFormComponents() {

				return(this.dependentFormComponents);
			}

			public void validate(Form form) {

				MyForm myForm = (MyForm) form;

				String account = (String) myForm.accountRadioGroup.getConvertedInput();
				Object synonymXri = myForm.synonymXriDropDownChoice.getConvertedInput();
				Object pass = myForm.passPasswordTextField.getConvertedInput();
				Object pass2 = myForm.pass2PasswordTextField.getConvertedInput();

				if (account != null && account.equals("current")) {

					return;
				} else if (account != null && account.equals("synonym")) {

					if (synonymXri == null) form.error(DoRegister.this.getString("nosynonym"));
				} else if (account == null || account.equals("new")) {

					if (pass == null || pass2 == null) form.error(DoRegister.this.getString("nopass"));
				}
			}
		}

		@Override
		protected void onSubmit() {

			// check account operation

			String account = this.accountRadioGroup.getModelObjectAsString();

			if (account.equals("current")) this.onSubmitCurrent();
			if (account.equals("synonym")) this.onSubmitSynonym();
			if (account.equals("new")) this.onSubmitNew();
		}

		private void onSubmitCurrent() {

			XriStore iname4javaStore = ((IbrokerApplication) Application.get()).getXriStore();

			log.info("Registering i-name " + DoRegister.this.iname + " on current account.");

			// try to create the i-name

			Xri xri;
			User user;

			try {

				// use logged in user

				user = ((IbrokerSession) this.getSession()).getUser();

				// create the xri

				GrsXriData xriData = new GrsXriData();
				xriData.setUserIdentifier(user.getIdentifier());
				xriData.setName(DoRegister.this.name);
				xriData.setOrganization(DoRegister.this.organization);
				xriData.setStreet(DoRegister.this.street2 != null ? new String[] { DoRegister.this.street1, DoRegister.this.street2 } : new String[] { DoRegister.this.street1 });
				xriData.setPostalCode(DoRegister.this.postalCode);
				xriData.setCity(DoRegister.this.city);
				xriData.setState(DoRegister.this.state);
				xriData.setCountryCode(DoRegister.this.countryCode);
				xriData.setPrimaryVoice(DoRegister.this.primaryVoice);
				xriData.setSecondaryVoice(DoRegister.this.secondaryVoice);
				xriData.setFax(DoRegister.this.fax);
				xriData.setPrimaryEmail(DoRegister.this.primaryEmail);
				xriData.setSecondaryEmail(DoRegister.this.secondaryEmail);
				xriData.setPager(DoRegister.this.pager);

				xri = iname4javaStore.registerXri(null, DoRegister.this.iname, xriData, YEARS);
			} catch (XriStoreException ex) {

				throw new RuntimeException("Problem while registering an XRI: " + ex.getMessage(), ex);
			}

			// send e-mail

			this.sendEmail();

			// take user to the wizard

			this.setResponsePage(new WizardXRI(xri));
			return;
		}

		private void onSubmitSynonym() {

			XriStore iname4javaStore = ((IbrokerApplication) Application.get()).getXriStore();

			log.info("Registering i-name " + DoRegister.this.iname + " as synonym.");

			// try to create the i-name

			Xri xri;
			User user;

			try {

				// use logged in user

				user = ((IbrokerSession) this.getSession()).getUser();

				// create the xri

				GrsXriData xriData = new GrsXriData();
				xriData.setUserIdentifier(user.getIdentifier());
				xriData.setName(DoRegister.this.name);
				xriData.setOrganization(DoRegister.this.organization);
				xriData.setStreet(DoRegister.this.street2 != null ? new String[] { DoRegister.this.street1, DoRegister.this.street2 } : new String[] { DoRegister.this.street1 });
				xriData.setPostalCode(DoRegister.this.postalCode);
				xriData.setCity(DoRegister.this.city);
				xriData.setState(DoRegister.this.state);
				xriData.setCountryCode(DoRegister.this.countryCode);
				xriData.setPrimaryVoice(DoRegister.this.primaryVoice);
				xriData.setSecondaryVoice(DoRegister.this.secondaryVoice);
				xriData.setFax(DoRegister.this.fax);
				xriData.setPrimaryEmail(DoRegister.this.primaryEmail);
				xriData.setSecondaryEmail(DoRegister.this.secondaryEmail);
				xriData.setPager(DoRegister.this.pager);

				xri = iname4javaStore.registerXriSynonym(null, DoRegister.this.iname, DoRegister.this.synonymXri, xriData, YEARS);
			} catch (XriStoreException ex) {

				throw new RuntimeException("Problem while registering an XRI: " + ex.getMessage(), ex);
			}

			// send e-mail

			this.sendEmail();

			// take user to the wizard

			this.setResponsePage(new WizardXRI(xri));
			return;
		}

		private void onSubmitNew() {

			XriStore iname4javaStore = ((IbrokerApplication) Application.get()).getXriStore();
			Store ibrokerStore = ((IbrokerApplication) this.getApplication()).getIbrokerStore();

			log.info("Registering i-name " + DoRegister.this.iname + " as new account.");

			// try to create the i-name

			Xri xri;
			User user;

			try {

				// log out user and create new one

				String userIdentifier = DoRegister.this.iname;

				((IbrokerSession) this.getSession()).logoutUser();

				// create user

				try {

					user = ibrokerStore.createOrUpdateUser(
							userIdentifier, 
							StoreUtil.hashPass(DoRegister.this.pass), 
							null,
							userIdentifier, 
							DoRegister.this.primaryEmail,
							Boolean.FALSE);
				} catch (Exception ex) {

					throw new RuntimeException("Cannot create new user.", ex);
				}

				// login user

				((IbrokerSession) this.getSession()).loginUser(user);

				// create the xri

				GrsXriData xriData = new GrsXriData();
				xriData.setUserIdentifier(user.getIdentifier());
				xriData.setName(DoRegister.this.name);
				xriData.setOrganization(DoRegister.this.organization);
				xriData.setStreet(DoRegister.this.street2 != null ? new String[] { DoRegister.this.street1, DoRegister.this.street2 } : new String[] { DoRegister.this.street1 });
				xriData.setPostalCode(DoRegister.this.postalCode);
				xriData.setCity(DoRegister.this.city);
				xriData.setState(DoRegister.this.state);
				xriData.setCountryCode(DoRegister.this.countryCode);
				xriData.setPrimaryVoice(DoRegister.this.primaryVoice);
				xriData.setSecondaryVoice(DoRegister.this.secondaryVoice);
				xriData.setFax(DoRegister.this.fax);
				xriData.setPrimaryEmail(DoRegister.this.primaryEmail);
				xriData.setSecondaryEmail(DoRegister.this.secondaryEmail);
				xriData.setPager(DoRegister.this.pager);

				xri = iname4javaStore.registerXri(null, DoRegister.this.iname, xriData, YEARS);
			} catch (XriStoreException ex) {

				throw new RuntimeException("Problem while registering an XRI: " + ex.getMessage(), ex);
			}

			// send e-mail

			this.sendEmail();

			// take user to the wizard

			this.setResponsePage(new WizardXRI(xri));
			return;
		}

		private void sendEmail() {

			// send e-mail

			String subject = "I-Name registration successful: " + DoRegister.this.iname;
			String to = DoRegister.this.primaryEmail;

			try {

				Email email = new Email(subject, to);
				StringWriter writer = new StringWriter();
				StringBuffer buffer;

				VelocityEngine velocity = new VelocityEngine();
				velocity.init();
				VelocityContext context = new VelocityContext(((IbrokerApplication) this.getApplication()).getProperties());
				context.put("iname", DoRegister.this.iname);
				Template template = velocity.getTemplate(((IbrokerApplication) this.getApplication()).getWicketFilter().getFilterConfig().getServletContext().getRealPath("WEB-INF/doregister.vm"));
				template.merge(context, writer);
				buffer = writer.getBuffer();
				email.println(buffer.toString());
				email.send();
			} catch (Exception ex) {

				throw new RuntimeException("Problem while sending e-mail: " + ex.getMessage(), ex);
			}			
		}
	}

	public String getName() {
		return (this.name);
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getOrganization() {
		return (this.organization);
	}
	public void setOrganization(String organization) {
		this.organization = organization;
	}
	public String getStreet1() {
		return (this.street1);
	}
	public void setStreet1(String street1) {
		this.street1 = street1;
	}
	public String getStreet2() {
		return (this.street2);
	}
	public void setStreet2(String street2) {
		this.street2 = street2;
	}
	public String getPostalCode() {
		return (this.postalCode);
	}
	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}
	public String getCity() {
		return (this.city);
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getState() {
		return (this.state);
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getCountrCode() {
		return (this.countryCode);
	}
	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}
	public String getPrimaryVoice() {
		return (this.primaryVoice);
	}
	public void setPrimaryVoice(String primaryVoice) {
		this.primaryVoice = primaryVoice;
	}
	public String getSecondaryVoice() {
		return (this.secondaryVoice);
	}
	public void setSecondaryVoice(String secondaryVoice) {
		this.secondaryVoice = secondaryVoice;
	}
	public String getFax() {
		return (this.fax);
	}
	public void setFax(String fax) {
		this.fax = fax;
	}
	public String getPrimaryEmail() {
		return (this.primaryEmail);
	}
	public void setPrimaryEmail(String primaryEmail) {
		this.primaryEmail = primaryEmail;
	}
	public String getSecondaryEmail() {
		return (this.secondaryEmail);
	}
	public void setSecondaryEmail(String secondaryEmail) {
		this.secondaryEmail = secondaryEmail;
	}
	public String getPager() {
		return (this.pager);
	}
	public void setPager(String pager) {
		this.pager = pager;
	}
	public Xri getSynonymXri() {
		return (this.synonymXri);
	}
	public void setSynonymXri(Xri synonymXri) {
		this.synonymXri = synonymXri;
	}
	public String getPass() {
		return (this.pass);
	}
	public void setPass(String pass) {
		this.pass = pass;
	}
	public String getPass2() {
		return (this.pass2);
	}
	public void setPass2(String pass2) {
		this.pass2 = pass2;
	}
}
