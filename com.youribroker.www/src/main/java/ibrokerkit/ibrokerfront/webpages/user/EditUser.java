package ibrokerkit.ibrokerfront.webpages.user;

import ibrokerkit.ibrokerfront.webapplication.IbrokerApplication;
import ibrokerkit.ibrokerfront.webapplication.IbrokerSession;
import ibrokerkit.ibrokerfront.webapplication.flags.LoggedInPage;
import ibrokerkit.ibrokerfront.webpages.BasePage;
import ibrokerkit.ibrokerstore.store.User;
import ibrokerkit.iservicestore.store.Authentication;

import java.io.Serializable;

import org.apache.wicket.Application;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.apache.wicket.validation.validator.StringValidator;

public class EditUser extends BasePage implements LoggedInPage {

	private static final long serialVersionUID = 3059367552001110079L;

	private User user;

	private FormData formData;

	private MyForm form;

	public EditUser() {

		this(((IbrokerSession) Session.get()).getUser());
	}

	public EditUser(User user) {

		this.user = user;

		this.setTitle(this.getString("title"));

		this.formData = new FormData();
		this.formData.setEmail(user.getEmail());

		// create and add components

		this.form = new MyForm("form", new CompoundPropertyModel(this.formData));

		add(this.form);
	}

	private static class FormData implements Serializable {

		private static final long serialVersionUID = -8025503906522673305L;

		private String pass;
		private String pass2;
		private String email;

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

	private class MyForm extends Form {

		private static final long serialVersionUID = 7819440312322226180L;

		private PasswordTextField passPasswordTextField;
		private PasswordTextField pass2PasswordTextField;
		private TextField emailTextField;

		private MyForm(String id, IModel model) {

			super(id, model);

			// create components

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
			this.emailTextField.add(EmailAddressValidator.getInstance());

			this.add(new EqualPasswordInputValidator(this.passPasswordTextField, this.pass2PasswordTextField));

			// add components

			this.add(new Label("identifier", EditUser.this.user.getIdentifier()));
			this.add(this.passPasswordTextField);
			this.add(this.pass2PasswordTextField);
			this.add(this.emailTextField);
			this.add(new FormComponentLabel("passLabel", this.passPasswordTextField));
			this.add(new FormComponentLabel("pass2Label", this.pass2PasswordTextField));
			this.add(new FormComponentLabel("emailLabel", this.emailTextField));
		}

		@Override
		protected void onSubmit() {

			ibrokerkit.ibrokerstore.store.Store ibrokerStore = ((IbrokerApplication)Application.get()).getIbrokerStore();
			ibrokerkit.iservicestore.store.Store iserviceStore = ((IbrokerApplication)Application.get()).getIserviceStore();

			// update user

			try {

				EditUser.this.user.setPass(ibrokerkit.ibrokerstore.store.StoreUtil.hashPass(EditUser.this.formData.getPass()));
				EditUser.this.user.setEmail(EditUser.this.formData.getEmail());

				ibrokerStore.updateObject(EditUser.this.user);
			} catch (Exception ex) {

				throw new RuntimeException("Cannot update user.", ex);
			}

			// update all Authentication i-service passwords of the user

			try {

				Authentication[] authentications = iserviceStore.listAuthenticationsByIndex(EditUser.this.user.getIdentifier());

				for (Authentication authentication : authentications) {

					authentication.setPass(ibrokerkit.ibrokerstore.store.StoreUtil.hashPass(EditUser.this.formData.getPass()));
					iserviceStore.updateObject(authentication);
				}
			} catch (Exception ex) {

				throw new RuntimeException("Cannot update Authentication i-services.", ex);
			}

			// login user

			((IbrokerSession) this.getSession()).loginUser(EditUser.this.user);

			// done

			info(EditUser.this.getString("success"));
		}
	}
}
