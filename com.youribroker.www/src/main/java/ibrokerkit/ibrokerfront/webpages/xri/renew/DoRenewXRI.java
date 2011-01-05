package ibrokerkit.ibrokerfront.webpages.xri.renew;

import ibrokerkit.ibrokerfront.email.Email;
import ibrokerkit.ibrokerfront.webapplication.IbrokerApplication;
import ibrokerkit.ibrokerfront.webpages.PaymentCallbackPage;
import ibrokerkit.iname4java.store.Xri;
import ibrokerkit.iname4java.store.XriStore;

import java.io.StringWriter;
import java.net.URLDecoder;
import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.wicket.Application;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.EmailAddressValidator;

public class DoRenewXRI extends PaymentCallbackPage {

	private static final long serialVersionUID = 5419595108571878552L;

	private static Log log = LogFactory.getLog(DoRenewXRI.class.getName());

	private String iname;

	// form data

	private String email;

	public DoRenewXRI(PageParameters pageParameters) {

		this(pageParameters, true);
	}

	public DoRenewXRI(PageParameters pageParameters, boolean verify) {

		super(pageParameters, RenewXRI.class, verify, 1);

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

		public MyForm(String id, IModel model) {

			super(id, model);

			// create components

			this.inameLabel = new Label("iname", DoRenewXRI.this.iname);
			this.emailTextField = new TextField("email");
			this.emailTextField.setLabel(new Model("Email"));
			this.emailTextField.setRequired(true);
			this.emailTextField.add(EmailAddressValidator.getInstance());

			// add components

			this.add(this.inameLabel);
			this.add(this.emailTextField);
		}

		@Override
		protected void onSubmit() {

			XriStore xriStore = ((IbrokerApplication) Application.get()).getXriStore();

			log.info("Renewing i-name " + DoRenewXRI.this.iname + " and i-number");

			// try to renew the i-name

			Calendar newExpDate;

			try {

				// renew the xri

				Xri xri = xriStore.findXri(DoRenewXRI.this.iname);

				newExpDate = xriStore.renewXri(xri, 1);
			} catch (Exception ex) {

				throw new RuntimeException("Problem while transferring an XRI: " + ex.getMessage(), ex);
			}

			// send e-mail

			String subject = "I-Name renewal successful: " + DoRenewXRI.this.iname;
			String to = DoRenewXRI.this.email;

			try {

				Email email = new Email(subject, to);
				StringWriter writer = new StringWriter();
				StringBuffer buffer;

				VelocityEngine velocity = new VelocityEngine();
				velocity.init();
				VelocityContext context = new VelocityContext(((IbrokerApplication) this.getApplication()).getProperties());
				context.put("iname", DoRenewXRI.this.iname);
				context.put("expdate", newExpDate.getTime().toString());
				Template template = velocity.getTemplate(((IbrokerApplication) this.getApplication()).getWicketFilter().getFilterConfig().getServletContext().getRealPath("WEB-INF/dorenew.vm"));
				template.merge(context, writer);
				buffer = writer.getBuffer();
				email.println(buffer.toString());
				email.send();
			} catch (Exception ex) {

				throw new RuntimeException("Problem while sending e-mail: " + ex.getMessage(), ex);
			}			

			// tell user what to do

			DoRenewXRI.this.info(DoRenewXRI.this.getString("success"));
		}
	}

	public String getEmail() {
		return (this.email);
	}
	public void setEmail(String email) {
		this.email = email;
	}
}
