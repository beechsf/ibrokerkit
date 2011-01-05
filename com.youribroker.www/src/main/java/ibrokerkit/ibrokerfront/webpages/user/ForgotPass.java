package ibrokerkit.ibrokerfront.webpages.user;

import ibrokerkit.ibrokerfront.email.Email;
import ibrokerkit.ibrokerfront.webapplication.IbrokerApplication;
import ibrokerkit.ibrokerfront.webpages.BasePage;
import ibrokerkit.ibrokerstore.store.Store;
import ibrokerkit.ibrokerstore.store.StoreUtil;
import ibrokerkit.ibrokerstore.store.User;
import ibrokerkit.iname4java.store.XriStore;

import java.io.StringWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.wicket.Application;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public class ForgotPass extends BasePage {

	private static final long serialVersionUID = 3059367552001110079L;

	private static Log log = LogFactory.getLog(ForgotPass.class.getName());

	private String input;

	private MyForm form;

	public ForgotPass() {

		this.setTitle(this.getString("title"));

		// create and add components

		this.form = new MyForm("form", new CompoundPropertyModel(this));

		add(this.form);
	}

	private class MyForm extends Form {

		private static final long serialVersionUID = 7819440312322226180L;

		private TextField inputTextField;

		private MyForm(String id, IModel model) {

			super(id, model);

			// create components

			this.inputTextField = new TextField("input");
			this.inputTextField.setLabel(new Model("i-name or e-mail"));
			this.inputTextField.setRequired(true);

			// add components

			this.add(this.inputTextField);
		}

		@Override
		protected void onSubmit() {

			XriStore xriStore = ((IbrokerApplication) Application.get()).getXriStore();
			Store ibrokerStore = ((IbrokerApplication)Application.get()).getIbrokerStore();

			// find user

			User user;

			try {

				String userIdentifier = xriStore.findUserIdentifier(ForgotPass.this.input);
				user = ibrokerStore.findUser(userIdentifier);
			} catch (Exception ex) {

				user = null;
			}

			// try to find user by e-mail address (only works if the e-mail exists on only 1 account)

			if (user == null) {

				User[] users;

				try {

					users = ibrokerStore.findUsersByEmail(ForgotPass.this.input);
				} catch (Exception ex) {

					throw new RuntimeException("Cannot read users from database.", ex);
				}

				if (users != null && users.length > 1) user = users[users.length - 1];
			}

			// nothing found?

			if (user == null) {

				error(ForgotPass.this.getString("notfound"));
				return;
			}

			// generate recovery

			String recovery = StoreUtil.randomPass();

			try {

				user.setRecovery(recovery);
				ibrokerStore.updateObject(user);
			} catch (Exception ex) {

				throw new RuntimeException("Cannot set recovery code.", ex);
			}

			// send e-mail

			String subject = "Password reset for " + user.getIdentifier();
			String to = user.getEmail();

			if (to == null || to.trim().equals("")) {

				this.error(ForgotPass.this.getString("noemail", null));
				return;
			}

			try {

				Email email = new Email(subject, to);
				StringWriter writer = new StringWriter();
				StringBuffer buffer;

				VelocityEngine velocity = new VelocityEngine();
				velocity.init();
				VelocityContext context = new VelocityContext(((IbrokerApplication) this.getApplication()).getProperties());
				context.put("$user", user.getIdentifier());
				context.put("$email", user.getEmail());
				context.put("$recovery", recovery);
				Template template = velocity.getTemplate(((IbrokerApplication) this.getApplication()).getWicketFilter().getFilterConfig().getServletContext().getRealPath("WEB-INF/forgotpass.vm"));
				template.merge(context, writer);
				buffer = writer.getBuffer();
				email.println(buffer.toString());
				email.send();
			} catch (Exception ex) {

				log.fatal(ex);
				this.error(ForgotPass.this.getString("sorry", null));
				return;
			}			

			// done

			info(ForgotPass.this.getString("success"));
		}
	}

	public String getInput() {
		return (this.input);
	}
	public void setInput(String input) {
		this.input = input;
	}
}
