package ibrokerkit.ibrokerfront.webpages.index.grs;

import ibrokerkit.ibrokerfront.webapplication.IbrokerApplication;
import ibrokerkit.ibrokerfront.webpages.BasePage;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.model.Model;


public class Register extends BasePage {

	private static final long serialVersionUID = 2786725384239368435L;

	private static Log log = LogFactory.getLog(Register.class.getName());

	private String iname;

	public Register(String iname) {

		this.setTitle(this.getString("title"));

		this.iname = iname;

		// create and add components

		this.add(new MyForm("form"));
	}

	public class MyForm extends Form {

		private static final long serialVersionUID = 3447974194419129870L;

		private Label typeLabel;
		private Label priceLabel;
		private CheckBox agreementCheckBox;

		public MyForm(String id) {

			super(id);

			// create and add components

			this.agreementCheckBox = new CheckBox("agreement", new Model());
			this.agreementCheckBox.setLabel(new Model("Agreement"));
			this.agreementCheckBox.setRequired(true);

			this.add(new Label("iname", Register.this.iname));

			if (Register.this.iname.charAt(0) == '=') {

				this.typeLabel = new Label("type", "individual");
				this.priceLabel = new Label("price", "$12");
			} else if (Register.this.iname.charAt(0) == '@') {

				this.typeLabel = new Label("type", "organizational");
				this.priceLabel = new Label("price", "$55");
			}

			this.add(this.typeLabel);
			this.add(this.priceLabel);
			this.add(this.agreementCheckBox);
		}

		@Override
		public void onSubmit() {

			Properties properties = ((IbrokerApplication) this.getApplication()).getProperties();

			// remember i-name in parameters

			String parameters;

			try {

				parameters = "iname=" + URLEncoder.encode(Register.this.iname, "UTF-8");
			} catch (UnsupportedEncodingException ex) {

				throw new RuntimeException(ex);
			}

			// take user to checkout page

			log.debug("Redirecting to registration payment page with parameters " + parameters);

			if (Register.this.iname.charAt(0) == '=') {

				this.setRedirect(true);
				this.setResponsePage(new RedirectPage(properties.getProperty("payment-equals-register") + '&' + parameters));
			} else if (Register.this.iname.charAt(0) == '@') {

				this.setRedirect(true);
				this.setResponsePage(new RedirectPage(properties.getProperty("payment-at-register") + '&' + parameters));
			}
		}
	}
}
