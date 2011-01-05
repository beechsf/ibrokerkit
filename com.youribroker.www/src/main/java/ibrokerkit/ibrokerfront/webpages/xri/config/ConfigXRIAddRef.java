package ibrokerkit.ibrokerfront.webpages.xri.config;

import ibrokerkit.iname4java.store.Xri;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.openxri.xml.Ref;

public class ConfigXRIAddRef extends ConfigXRI {

	private static final long serialVersionUID = -6420786977619030326L;

	private static Log log = LogFactory.getLog(ConfigXRIAddRef.class.getName());

	private Ref ref;

	private MyForm form;

	public ConfigXRIAddRef(Xri xri) {

		this(xri, true);
	}

	protected ConfigXRIAddRef(Xri xri, boolean createPages) {

		super(xri, createPages);

		this.ref = new Ref();
		this.ref.setPriority(new Integer(10));

		// create components

		this.form = new MyForm("form", new CompoundPropertyModel(this.ref));
		this.form.setOutputMarkupId(true);

		// add components

		this.add(this.form);
	}

	private class MyForm extends Form {

		private static final long serialVersionUID = 199296660295637064L;

		private TextField valueTextField;
		private TextField priorityTextField;
		private MySubmitButton submitButton;

		private MyForm(String id, IModel model) {

			super(id, model);

			// create components

			this.valueTextField = new TextField("value");
			this.priorityTextField = new TextField("priority");
			this.submitButton = new MySubmitButton("submitButton");

			// add components

			this.add(this.valueTextField);
			this.add(this.priorityTextField);
			this.add(this.submitButton);
			this.add(new FormComponentLabel("valueLabel", this.valueTextField));
			this.add(new FormComponentLabel("priorityLabel", this.priorityTextField));
		}

		private class MySubmitButton extends Button {

			private static final long serialVersionUID = 5225692085480580044L;

			private MySubmitButton(String id) {

				super(id);
			}

			@Override
			public void onSubmit() {

				try {

					// add the ref

					ConfigXRIAddRef.this.xri.addRef(ConfigXRIAddRef.this.ref);
					
					ConfigXRIAddRef.this.ref = new Ref();
					ConfigXRIAddRef.this.ref.setPriority(new Integer(10));
					MyForm.this.setModelObject(ConfigXRIAddRef.this.ref);
				} catch (Exception ex) {

					ConfigXRIAddRef.log.error(ex);
					error(getString("addfail") + ex.getLocalizedMessage());
					return;
				}

				info(ConfigXRIAddRef.this.getString("success"));
				return;
			}
		}
	}
}
