package ibrokerkit.ibrokerfront.webpages.user;

import ibrokerkit.ibrokerfront.webapplication.IbrokerApplication;
import ibrokerkit.ibrokerfront.webpages.BasePage;
import ibrokerkit.ibrokerstore.store.Store;
import ibrokerkit.ibrokerstore.store.StoreUtil;
import ibrokerkit.ibrokerstore.store.User;
import ibrokerkit.iservicestore.store.Authentication;

import org.apache.wicket.Application;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.StringValidator;

public class RecoverPass extends BasePage {

	private static final long serialVersionUID = 3059367552001110079L;

	private String recovery;
	private String pass;
	private String pass2;

	private MyForm form;

	public RecoverPass() {

		this.setTitle(this.getString("title"));

		// create and add components

		this.form = new MyForm("form", new CompoundPropertyModel(this));

		add(this.form);
	}

	private class MyForm extends Form {

		private static final long serialVersionUID = 7819440312322226180L;

		private TextField recoveryTextField;
		private PasswordTextField passPasswordTextField;
		private PasswordTextField pass2PasswordTextField;

		private MyForm(String id, IModel model) {

			super(id, model);

			// create components

			this.recoveryTextField = new TextField("recovery");
			this.recoveryTextField.setLabel(new Model("Recovery code"));
			this.recoveryTextField.setRequired(true);
			this.passPasswordTextField = new PasswordTextField("pass");
			this.passPasswordTextField.setLabel(new Model("Password"));
			this.passPasswordTextField.setRequired(true);
			this.passPasswordTextField.add(StringValidator.minimumLength(5));
			this.pass2PasswordTextField = new PasswordTextField("pass2");
			this.pass2PasswordTextField.setLabel(new Model("Password 2"));
			this.pass2PasswordTextField.setRequired(true);
			this.pass2PasswordTextField.add(StringValidator.minimumLength(5));

			// add components

			this.add(this.recoveryTextField);
			this.add(this.passPasswordTextField);
			this.add(this.pass2PasswordTextField);

			// add validators

			this.add(new EqualPasswordInputValidator(this.passPasswordTextField, this.pass2PasswordTextField));
		}

		@Override
		protected void onSubmit() {

			Store ibrokerStore = ((IbrokerApplication)Application.get()).getIbrokerStore();
			ibrokerkit.iservicestore.store.Store iserviceStore = ((IbrokerApplication)Application.get()).getIserviceStore();

			// find user

			User user = null;

			try {

				user = ibrokerStore.findUserByRecovery(RecoverPass.this.recovery);
			} catch (Exception ex) {

				throw new RuntimeException("Cannot read user from database.", ex);
			}

			// nothing found?

			if (user == null) {

				error(RecoverPass.this.getString("notfound"));
				return;
			}

			// update user

			try {

				user.setPass(StoreUtil.hashPass(RecoverPass.this.pass));
				user.setRecovery(null);
				ibrokerStore.updateObject(user);
			} catch (Exception ex) {

				throw new RuntimeException("Cannot read user from database.", ex);
			}

			// update all Authentication i-service passwords of the user

			try {

				Authentication[] authentications = iserviceStore.listAuthenticationsByIndex(user.getIdentifier());

				for (Authentication authentication : authentications) {

					authentication.setPass(ibrokerkit.ibrokerstore.store.StoreUtil.hashPass(RecoverPass.this.pass));
					iserviceStore.updateObject(authentication);
				}
			} catch (Exception ex) {

				throw new RuntimeException("Cannot update Authentication i-services.", ex);
			}
			
			// done

			info(RecoverPass.this.getString("success"));
		}
	}

	public String getRecovery() {
		return (this.recovery);
	}
	public void setRecovery(String recovery) {
		this.recovery = recovery;
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
