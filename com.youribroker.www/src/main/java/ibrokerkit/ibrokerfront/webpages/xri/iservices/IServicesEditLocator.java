package ibrokerkit.ibrokerfront.webpages.xri.iservices;

import ibrokerkit.ibrokerfront.webapplication.IbrokerApplication;
import ibrokerkit.ibrokerfront.webapplication.util.GoogleGeoCoder;
import ibrokerkit.ibrokerfront.webapplication.util.GoogleGeoCoder.GoogleGeoCoderResult;
import ibrokerkit.iname4java.store.Xri;
import ibrokerkit.iservicestore.store.Locator;
import ibrokerkit.iservicestore.store.Store;
import ibrokerkit.iservicestore.store.StoreException;

import java.io.IOException;

import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;

public class IServicesEditLocator extends IServices {

	private static final long serialVersionUID = -7284629991071350585L;

	public IServicesEditLocator(Xri xri, Locator locator) {

		this(xri, true, locator);
	}

	protected IServicesEditLocator(Xri xri, boolean createPages, Locator locator) {

		super(xri, createPages);

		// create and add components

		this.add(new EditForm("editForm", new CompoundPropertyModel(locator)));
	}

	private class EditForm extends Form {

		private static final long serialVersionUID = -8510554598285096507L;

		private TextField nameTextField;
		private CheckBox enabledCheckBox;
		private TextField addressTextField;
		private TextArea descriptionTextArea;
		private CheckBox contactLinkCheckBox;
		private TextField latTextField;
		private TextField lngTextField;
		private TextField zoomTextField;
		private AutoDetectButton autoDetectButton;
		private SubmitButton submitButton;

		private EditForm(String id, IModel model) {

			super(id, model);

			// create components

			this.nameTextField = new TextField("name");
			this.enabledCheckBox = new CheckBox("enabled");
			this.addressTextField = new TextField("address");
			this.descriptionTextArea = new TextArea("description");
			this.latTextField = new TextField("lat");
			this.latTextField.setOutputMarkupId(true);
			this.lngTextField = new TextField("lng");
			this.lngTextField.setOutputMarkupId(true);
			this.zoomTextField = new TextField("zoom");
			this.zoomTextField.setOutputMarkupId(true);
			this.autoDetectButton = new AutoDetectButton("autoDetectButton", this);
			this.contactLinkCheckBox = new CheckBox("contactLink");
			this.submitButton = new SubmitButton("submitButton");

			// add components

			this.add(this.nameTextField);
			this.add(this.enabledCheckBox);
			this.add(this.addressTextField);
			this.add(this.descriptionTextArea);
			this.add(this.latTextField);
			this.add(this.lngTextField);
			this.add(this.zoomTextField);
			this.add(this.autoDetectButton);
			this.add(this.contactLinkCheckBox);
			this.add(this.submitButton);
		}

		private class AutoDetectButton extends AjaxSubmitLink {

			private static final long serialVersionUID = -2068039185001523794L;

			public AutoDetectButton(String id, Form form) {

				super(id, form);
			}

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form form) {

				Locator locator = (Locator) form.getModelObject();

				try {

					GoogleGeoCoderResult result = GoogleGeoCoder.geoCode(locator.getAddress());

					locator.setLat(result.getLat());
					locator.setLng(result.getLng());
					locator.setZoom(result.getZoom());
				} catch (IOException ex) {

					locator.setLat(new Double(-1));
					locator.setLng(new Double(-1));
					locator.setZoom(new Double(-1));
				}

				if (target != null) {

					target.addComponent(EditForm.this.latTextField);
					target.addComponent(EditForm.this.lngTextField);
					target.addComponent(EditForm.this.zoomTextField);
				}
			}
		}

		private class SubmitButton extends Button {

			private static final long serialVersionUID = -7223696065897266860L;

			private SubmitButton(String id) {

				super(id);
			}

			@Override
			public void onSubmit() {

				Store store = ((IbrokerApplication) Application.get()).getIserviceStore();
				Locator locator = (Locator) EditForm.this.getModelObject();

				try {

					// update the locator i-service

					store.updateObject(locator);

					// and go back to the locators list

					Page page = new IServicesListLocators(IServicesEditLocator.this.xri);
					page.info(IServicesEditLocator.this.getString("success"));
					this.setResponsePage(page);
				} catch (StoreException ex) {

					throw new RuntimeException("Problem while updating the Locator i-service.", ex);
				}
			}
		}
	}
}
