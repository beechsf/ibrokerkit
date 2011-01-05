package ibrokerkit.ibrokerfront.webpages.xri.transfer;

import ibrokerkit.ibrokerfront.components.openid.OpenIDPanel;
import ibrokerkit.ibrokerfront.webapplication.IbrokerApplication;
import ibrokerkit.ibrokerfront.webpages.BasePage;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;

public class TransferXRIIn extends BasePage {

	private static final long serialVersionUID = -3451522127828390121L;

	private static Log log = LogFactory.getLog(TransferXRIIn.class.getName());

	// form data

	private String openid;

	public TransferXRIIn() {

		this.setTitle(this.getString("title"));

		// create and add components

		this.add(new MyForm("form", new CompoundPropertyModel(this)));
	}

	private class MyForm extends Form {

		private static final long serialVersionUID = -6435608976074820449L;

		// form components

		private OpenIDPanel openidPanel;
		private SubmitButton requestButton;

		private MyForm(String id, IModel model) {

			super(id, model);

			// create components

			this.openidPanel = new OpenIDPanel("openid");
			this.openidPanel.setRequired(true);
			this.requestButton = new SubmitButton("request");

			// add components

			this.add(this.openidPanel);
			this.add(this.requestButton);
		}

		private class SubmitButton extends Button {

			private static final long serialVersionUID = -5618465237448994700L;

			private SubmitButton(String id) {

				super(id);
			}

			@Override
			public void onSubmit() {

				Properties properties = ((IbrokerApplication) this.getApplication()).getProperties();

				// check if sender is authenticated

				if (! MyForm.this.openidPanel.isAuthenticated()) {

					TransferXRIIn.this.error(TransferXRIIn.this.getString("authenticate"));
					return;
				}

				// remember i-name in parameters

				String parameters;

				try {

					parameters = "iname=" + URLEncoder.encode(TransferXRIIn.this.openid, "UTF-8");
				} catch (UnsupportedEncodingException ex) {

					throw new RuntimeException(ex);
				}

				// take user to checkout page

				log.debug("Redirecting to transfer payment page with parameters " + parameters);

				if (TransferXRIIn.this.openid.charAt(0) == '=') {

					this.setRedirect(true);
					this.setResponsePage(new RedirectPage(properties.getProperty("payment-equals-transferxriin") + '&' + parameters));
				} else if (TransferXRIIn.this.openid.charAt(0) == '@') {

					this.setRedirect(true);
					this.setResponsePage(new RedirectPage(properties.getProperty("payment-at-transferxriin") + '&' + parameters));
				}
			}
		}
	}
	public String getOpenid() {
		return (this.openid);
	}
	public void setOpenid(String openid) {
		this.openid = openid;
	}
}
