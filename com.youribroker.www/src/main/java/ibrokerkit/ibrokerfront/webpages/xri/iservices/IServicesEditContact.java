package ibrokerkit.ibrokerfront.webpages.xri.iservices;

import ibrokerkit.ibrokerfront.webapplication.IbrokerApplication;
import ibrokerkit.iname4java.store.Xri;
import ibrokerkit.iservicestore.store.Contact;
import ibrokerkit.iservicestore.store.Store;
import ibrokerkit.iservicestore.store.StoreException;

import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;


public class IServicesEditContact extends IServices {

	private static final long serialVersionUID = 1988616343831891134L;

	public IServicesEditContact(Xri xri, Contact contact) {

		this(xri, true, contact);
	}

	protected IServicesEditContact(Xri xri, boolean createPages, Contact contact) {

		super(xri, createPages);

		// create and add components

		this.add(new EditForm("editForm", new CompoundPropertyModel(contact)));
	}

	private class EditForm extends Form {

		private static final long serialVersionUID = 3862808475280756256L;
		private TextField nameTextField;
		private CheckBox enabledCheckBox;
		private TextField forwardTextField;
		private TextArea descriptionTextArea;

		private EditForm(String id, IModel model) {

			super(id, model);

			// create components

			this.nameTextField = new TextField("name");
			this.enabledCheckBox = new CheckBox("enabled");
			this.forwardTextField = new TextField("forward");
			this.descriptionTextArea = new TextArea("description");

			// add components

			this.add(this.nameTextField);
			this.add(this.enabledCheckBox);
			this.add(this.forwardTextField);
			this.add(this.descriptionTextArea);
		}

		@Override
		protected void onSubmit() {

			Store store = ((IbrokerApplication) Application.get()).getIserviceStore();
			Contact contact = (Contact) this.getModelObject();

			try {

				// update the contact i-service

				store.updateObject(contact);

				// and go back to the contacts list

				Page page = new IServicesListContacts(IServicesEditContact.this.xri);
				page.info(IServicesEditContact.this.getString("success"));
				this.setResponsePage(page);
			} catch (StoreException ex) {

				throw new RuntimeException("Problem while updating the Contact i-service.", ex);
			}
		}
	}
}
