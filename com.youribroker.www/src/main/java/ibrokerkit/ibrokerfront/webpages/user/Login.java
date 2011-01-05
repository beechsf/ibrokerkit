package ibrokerkit.ibrokerfront.webpages.user;

import ibrokerkit.ibrokerfront.webapplication.IbrokerApplication;
import ibrokerkit.ibrokerfront.webapplication.IbrokerSession;
import ibrokerkit.ibrokerfront.webpages.BasePage;
import ibrokerkit.ibrokerfront.webpages.xri.YourXRIs;
import ibrokerkit.ibrokerstore.store.Store;
import ibrokerkit.ibrokerstore.store.StoreUtil;
import ibrokerkit.ibrokerstore.store.User;
import ibrokerkit.iname4java.store.XriStore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Application;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public class Login extends BasePage {

	private static final long serialVersionUID = 1585319524944745561L;

	protected static Log log = LogFactory.getLog(Login.class.getName());

	private String identifier;
	private String pass;

	public Login() {

		this.setTitle(this.getString("title"));

		// create and add components

		this.add(new MyForm("form", new CompoundPropertyModel(this)));
		this.add(new BookmarkablePageLink("forgotLink", ForgotPass.class));
	}

	private class MyForm extends Form {

		private static final long serialVersionUID = 1045735353194990548L;

		private TextField identifierTextField;
		private PasswordTextField passTextField;

		private MyForm(String id, IModel model) {

			super(id, model);

			// create components

			this.identifierTextField = new TextField("identifier");
			this.identifierTextField.setLabel(new Model("I-Name"));
			this.identifierTextField.setRequired(true);
			this.passTextField = new PasswordTextField("pass");
			this.passTextField.setLabel(new Model("Password"));
			this.passTextField.setRequired(true);

			// add components

			this.add(this.identifierTextField);
			this.add(this.passTextField);
		}

		@Override
		protected void onSubmit() {

			Login.log.debug("Beginning Login.");

			RequestCycle requestCycle = this.getRequestCycle();
			XriStore xriStore = ((IbrokerApplication) Application.get()).getXriStore();
			Store ibrokerStore = ((IbrokerApplication) this.getApplication()).getIbrokerStore();

			// find user

			User user;
			String pass;

			try {

				String userIdentifier = xriStore.findUserIdentifier(Login.this.identifier);
				user = ibrokerStore.findUser(userIdentifier);
				pass = user.getPass();
			} catch (Exception ex) {

				error(Login.this.getString("wrongpass"));
				return;
			}

			// check claimed password

			String claimedPass = StoreUtil.hashPass(Login.this.pass);

			Login.log.debug("Check password: " + pass + " == " + claimedPass);

			if (! (pass.equals(claimedPass))) {

				error(Login.this.getString("wrongpass"));
				return;
			}

			// login user in the session

			((IbrokerSession) Session.get()).loginUser(user);

			// send user to his i-names

			requestCycle.setResponsePage(YourXRIs.class);
			requestCycle.setRedirect(true);
			return;
		}
	}

	public String getPass() {
		return (this.pass);
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public String getIdentifier() {
		return (this.identifier);
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
}
