package ibrokerkit.ibrokerfront.webpages.index.community;

import ibrokerkit.ibrokerfront.email.Email;
import ibrokerkit.ibrokerfront.models.AvailableParentInamesModel;
import ibrokerkit.ibrokerfront.validators.SubSegmentNamePatternValidator;
import ibrokerkit.ibrokerfront.webapplication.IbrokerApplication;
import ibrokerkit.ibrokerfront.webapplication.IbrokerSession;
import ibrokerkit.ibrokerfront.webpages.BasePage;
import ibrokerkit.ibrokerfront.webpages.xri.wizard.WizardXRI;
import ibrokerkit.ibrokerstore.store.Store;
import ibrokerkit.ibrokerstore.store.StoreUtil;
import ibrokerkit.ibrokerstore.store.User;
import ibrokerkit.iname4java.store.Xri;
import ibrokerkit.iname4java.store.XriStore;
import ibrokerkit.iname4java.store.XriStoreException;
import ibrokerkit.iname4java.store.impl.openxri.OpenxriXriData;

import java.io.StringWriter;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.wicket.Application;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.apache.wicket.validation.validator.StringValidator;

public class Index extends BasePage {

	private static final long serialVersionUID = 2786725384239368435L;

	private static final int YEARS = 1;
	
	// form data

	private Xri parentXri;
	private String localName;
	private String pass;
	private String pass2;
	private String email;

	public Index() {

		this.setTitle(this.getString("title"));

		// create and add components

		this.add(new MyForm("form", new CompoundPropertyModel(this)));
	}

	public class MyForm extends Form {

		private static final long serialVersionUID = -1976441444627103562L;

		// form components

		private DropDownChoice parentXriDropDownChoice;
		private TextField localNameTextField;
		private WebMarkupContainer userContainer;
		private PasswordTextField passPasswordTextField;
		private PasswordTextField pass2PasswordTextField;
		private TextField emailTextField;

		public MyForm(String id, IModel model) {

			super(id, model);

			// create components

			AvailableParentInamesModel availableParentInamesModel = new AvailableParentInamesModel();

			this.parentXriDropDownChoice = new DropDownChoice("parentXri", availableParentInamesModel, (IChoiceRenderer) availableParentInamesModel);
			this.parentXriDropDownChoice.setLabel(new Model("Parent XRI"));
			this.parentXriDropDownChoice.setRequired(true);
			this.localNameTextField = new TextField("localName");
			this.localNameTextField.setLabel(new Model("i-name"));
			this.localNameTextField.setRequired(true);
			this.localNameTextField.add(new SubSegmentNamePatternValidator());
			this.userContainer = new WebMarkupContainer("userContainer", model);
			this.passPasswordTextField = new PasswordTextField("pass");
			this.passPasswordTextField.setLabel(new Model("Password"));
			this.passPasswordTextField.setRequired(true);
			this.passPasswordTextField.add(StringValidator.minimumLength(5));
			this.pass2PasswordTextField = new PasswordTextField("pass2");
			this.pass2PasswordTextField.setLabel(new Model("Password 2"));
			this.pass2PasswordTextField.setRequired(true);
			this.pass2PasswordTextField.add(StringValidator.minimumLength(5));
			this.emailTextField = new TextField("email");
			this.emailTextField.setLabel(new Model("Email"));
			this.emailTextField.setRequired(true);
			this.emailTextField.add(EmailAddressValidator.getInstance());

			// if user is logged in, dont need user fields

			if (((IbrokerSession) this.getSession()).isLoggedIn()) {

				this.userContainer.setVisible(false);
			}

			// add components

			this.userContainer.add(this.passPasswordTextField);
			this.userContainer.add(this.pass2PasswordTextField);
			this.userContainer.add(this.emailTextField);
			this.add(this.parentXriDropDownChoice);
			this.add(this.localNameTextField);
			this.add(this.userContainer);

			// add validators

			this.add(new EqualPasswordInputValidator(this.passPasswordTextField, this.pass2PasswordTextField));
		}

		@Override
		protected void onSubmit() {

			XriStore xriStore = ((IbrokerApplication) Application.get()).getXriStore();
			Store ibrokerStore = ((IbrokerApplication) this.getApplication()).getIbrokerStore();

			Index.this.localName = Index.this.localName.toLowerCase();

			// try to create the i-name

			Xri xri;
			User user;

			try {

				// check if the xri exists already

				boolean exists = xriStore.existsXri(Index.this.parentXri, '*' + Index.this.localName);

				if (exists) {

					this.error(Index.this.getString("exists"));
					return;
				}

				// if no user is logged in, create one and log him in

				user = ((IbrokerSession) this.getSession()).getUser();

				if (user == null) {

					// create user

					try {

						String userIdentifier =  Index.this.parentXri.getFullName() + '*' + Index.this.localName;

						user = ibrokerStore.createOrUpdateUser(
								userIdentifier, 
								StoreUtil.hashPass(Index.this.pass), 
								null,
								userIdentifier, 
								Index.this.email,
								Boolean.FALSE);
					} catch (Exception ex) {

						throw new RuntimeException("Cannot create new user.", ex);
					}

					// login user

					((IbrokerSession) this.getSession()).loginUser(user);
				}

				// create the xri

				OpenxriXriData xriData = new OpenxriXriData();
				xriData.setUserIdentifier(user.getIdentifier());

				xri = xriStore.registerXri(Index.this.parentXri, '*' + Index.this.localName, xriData, YEARS);
			} catch (XriStoreException ex) {

				throw new RuntimeException("Problem while registering an XRI: " + ex.getMessage(), ex);
			}

			// send e-mail

			String subject = "I-Name registration successful: " + Index.this.parentXri.getFullName() + '*' + Index.this.localName;
			String to = user.getEmail();

			try {

				Email email = new Email(subject, to);
				StringWriter writer = new StringWriter();
				StringBuffer buffer;

				VelocityEngine velocity = new VelocityEngine();
				velocity.init();
				VelocityContext context = new VelocityContext(((IbrokerApplication) this.getApplication()).getProperties());
				context.put("iname", Index.this.parentXri.getFullName() + '*' + Index.this.localName);
				Template template = velocity.getTemplate(((IbrokerApplication) this.getApplication()).getWicketFilter().getFilterConfig().getServletContext().getRealPath("WEB-INF/doregister.vm"));
				template.merge(context, writer);
				buffer = writer.getBuffer();
				email.println(buffer.toString());
				email.send();
			} catch (Exception ex) {

				throw new RuntimeException("Problem while sending e-mail: " + ex.getMessage(), ex);
			}			

			// take user to the wizard

			this.setResponsePage(new WizardXRI(xri));
			return;
		}
	}

	public Xri getParentXri() {
		return (this.parentXri);
	}
	public void setParentXri(Xri parentXri) {
		this.parentXri = parentXri;
	}
	public String getLocalName() {
		return (this.localName);
	}
	public void setLocalName(String localName) {
		this.localName = localName;
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
	public String getEmail() {
		return (this.email);
	}
	public void setEmail(String email) {
		this.email = email;
	}
}
