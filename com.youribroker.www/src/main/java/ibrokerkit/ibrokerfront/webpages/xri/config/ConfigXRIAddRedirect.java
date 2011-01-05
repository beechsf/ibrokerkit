package ibrokerkit.ibrokerfront.webpages.xri.config;

import ibrokerkit.ibrokerfront.models.SEPUriAppendModel;
import ibrokerkit.iname4java.store.Xri;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.openxri.xml.Redirect;


public class ConfigXRIAddRedirect extends ConfigXRI {

	private static final long serialVersionUID = -508525213663460393L;

	private static Log log = LogFactory.getLog(ConfigXRIAddRedirect.class.getName());

	private Redirect redirect;

	private MyForm form;

	public ConfigXRIAddRedirect(Xri xri) {

		this(xri, true);
	}

	protected ConfigXRIAddRedirect(Xri xri, boolean createPages) {

		super(xri, createPages);

		this.redirect = new Redirect();
		this.redirect.setPriority(new Integer(10));

		// create components

		this.form = new MyForm("form", new CompoundPropertyModel(this.redirect));
		this.form.setOutputMarkupId(true);

		// add components

		this.add(this.form);
	}

	private class MyForm extends Form {

		private static final long serialVersionUID = -1234803911085909362L;

		private TextField valueTextField;
		private TextField priorityTextField;
		private DropDownChoice appendDropDownChoice;
		private MySubmitButton submitButton;

		private MyForm(String id, IModel model) {

			super(id, model);

			// create components

			this.valueTextField = new TextField("value");
			this.priorityTextField = new TextField("priority");
			this.appendDropDownChoice = new DropDownChoice("append", new SEPUriAppendModel());
			this.submitButton = new MySubmitButton("submitButton");

			// add components

			this.add(this.valueTextField);
			this.add(this.priorityTextField);
			this.add(this.appendDropDownChoice);
			this.add(this.submitButton);
			this.add(new FormComponentLabel("valueLabel", this.valueTextField));
			this.add(new FormComponentLabel("priorityLabel", this.priorityTextField));
			this.add(new FormComponentLabel("appendLabel", this.appendDropDownChoice));
		}

		private class MySubmitButton extends Button {

			private static final long serialVersionUID = -5684829951040190294L;

			private MySubmitButton(String id) {

				super(id);
			}

			@Override
			public void onSubmit() {

				try {

					// add the redirect

					ConfigXRIAddRedirect.this.xri.addRedirect(ConfigXRIAddRedirect.this.redirect);

					ConfigXRIAddRedirect.this.redirect = new Redirect();
					ConfigXRIAddRedirect.this.redirect.setPriority(new Integer(10));
					MyForm.this.setModelObject(ConfigXRIAddRedirect.this.redirect);
				} catch (Exception ex) {

					ConfigXRIAddRedirect.log.error(ex);
					error(getString("addfail") + ex.getLocalizedMessage());
					return;
				}

				info(ConfigXRIAddRedirect.this.getString("success"));
				return;
			}
		}
	}
}
