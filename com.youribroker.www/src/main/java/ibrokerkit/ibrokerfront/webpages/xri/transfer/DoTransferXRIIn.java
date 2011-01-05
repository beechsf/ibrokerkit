package ibrokerkit.ibrokerfront.webpages.xri.transfer;

import ibrokerkit.ibrokerfront.email.Email;
import ibrokerkit.ibrokerfront.webapplication.IbrokerApplication;
import ibrokerkit.ibrokerfront.webapplication.IbrokerSession;
import ibrokerkit.ibrokerfront.webpages.PaymentCallbackPage;
import ibrokerkit.ibrokerstore.store.Store;
import ibrokerkit.ibrokerstore.store.StoreUtil;
import ibrokerkit.ibrokerstore.store.User;
import ibrokerkit.iname4java.store.Xri;
import ibrokerkit.iname4java.store.XriConstants;
import ibrokerkit.iname4java.store.XriStore;
import ibrokerkit.iname4java.store.impl.grs.GrsXriData;

import java.io.StringWriter;
import java.net.URLDecoder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.wicket.Application;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.apache.wicket.validation.validator.StringValidator;

public class DoTransferXRIIn extends PaymentCallbackPage {

	private static final long serialVersionUID = 2786725384239368435L;

	private static Log log = LogFactory.getLog(DoTransferXRIIn.class.getName());

	private String iname;

	// form data

	private String email;
	private String pass;
	private String pass2;

	public DoTransferXRIIn(PageParameters pageParameters) {

		this(pageParameters, true);
	}

	public DoTransferXRIIn(PageParameters pageParameters, boolean verify) {

		super(pageParameters, TransferXRIIn.class, verify, 1);

		this.setTitle(this.getString("title"));

		// get passed-in parameters

		try {

			this.iname = URLDecoder.decode(pageParameters.getString("iname"), "UTF-8");
		} catch (Exception ex) {

			throw new RuntimeException(ex);
		}

		this.email = pageParameters.getString("email");

		// create and add components

		this.add(new Label("iname", this.iname));
		this.add(new MyForm("form", new CompoundPropertyModel(this)));
	}

	public class MyForm extends Form {

		private static final long serialVersionUID = -1976441444627103562L;

		// form components

		private Label inameLabel;
		private TextField emailTextField;
		private PasswordTextField passPasswordTextField;
		private PasswordTextField pass2PasswordTextField;

		public MyForm(String id, IModel model) {

			super(id, model);

			// create components

			this.inameLabel = new Label("iname", DoTransferXRIIn.this.iname);
			this.emailTextField = new TextField("email");
			this.emailTextField.setLabel(new Model("Email"));
			this.emailTextField.setRequired(true);
			this.emailTextField.add(EmailAddressValidator.getInstance());
			this.passPasswordTextField = new PasswordTextField("pass");
			this.passPasswordTextField.setLabel(new Model("Password"));
			this.passPasswordTextField.setRequired(true);
			this.passPasswordTextField.add(StringValidator.minimumLength(5));
			this.pass2PasswordTextField = new PasswordTextField("pass2");
			this.pass2PasswordTextField.setLabel(new Model("Password 2"));
			this.pass2PasswordTextField.setRequired(true);
			this.pass2PasswordTextField.add(StringValidator.minimumLength(5));

			// add components

			this.add(this.inameLabel);
			this.add(this.emailTextField);
			this.add(this.passPasswordTextField);
			this.add(this.pass2PasswordTextField);

			// add validators

			this.add(new EqualPasswordInputValidator(this.passPasswordTextField, this.pass2PasswordTextField));
		}

		@Override
		protected void onSubmit() {

			XriStore xriStore = ((IbrokerApplication) Application.get()).getXriStore();
			Store ibrokerStore = ((IbrokerApplication) this.getApplication()).getIbrokerStore();

			log.info("Transferring i-name " + DoTransferXRIIn.this.iname);

			// try to transfer the i-name

			Xri xri;
			User user;
			String transferToken;

			try {

				// log out user and create new one

				String userIdentifier = DoTransferXRIIn.this.iname;

				((IbrokerSession) this.getSession()).logoutUser();

				// create user

 				try {

					user = ibrokerStore.createOrUpdateUser(
							userIdentifier, 
							StoreUtil.hashPass(DoTransferXRIIn.this.pass), 
							null,
							userIdentifier, 
							DoTransferXRIIn.this.email,
							Boolean.FALSE);
				} catch (Exception ex) {

					throw new RuntimeException("Cannot create new user.", ex);
				}

				// login user

				((IbrokerSession) this.getSession()).loginUser(user);

				// transfer the xri

				GrsXriData xriData = new GrsXriData();
				xriData.setUserIdentifier(user.getIdentifier());

				xri = xriStore.transferAuthorityInRequest(DoTransferXRIIn.this.iname, xriData);

				// get the transfer token

				transferToken = xri.getAuthorityAttribute(XriConstants.ATTRIBUTE_GRS_TRANSFERTOKEN);
			} catch (Exception ex) {

				throw new RuntimeException("Problem while transferring an XRI: " + ex.getMessage(), ex);
			}

			// send e-mail

			String subject = "I-Name transfer request successful: " + DoTransferXRIIn.this.iname;
			String to = DoTransferXRIIn.this.email;

			try {

				Email email = new Email(subject, to);
				StringWriter writer = new StringWriter();
				StringBuffer buffer;

				VelocityEngine velocity = new VelocityEngine();
				velocity.init();
				VelocityContext context = new VelocityContext(((IbrokerApplication) this.getApplication()).getProperties());
				context.put("iname", DoTransferXRIIn.this.iname);
				context.put("transfertoken", transferToken);
				Template template = velocity.getTemplate(((IbrokerApplication) this.getApplication()).getWicketFilter().getFilterConfig().getServletContext().getRealPath("WEB-INF/dotransferin.vm"));
				template.merge(context, writer);
				buffer = writer.getBuffer();
				email.println(buffer.toString());
				email.send();
			} catch (Exception ex) {

				throw new RuntimeException("Problem while sending e-mail: " + ex.getMessage(), ex);
			}			

			// tell user what to do

			DoTransferXRIIn.this.info(DoTransferXRIIn.this.getString("success"));
		}
	}

	public String getEmail() {
		return (this.email);
	}
	public void setEmail(String email) {
		this.email = email;
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
