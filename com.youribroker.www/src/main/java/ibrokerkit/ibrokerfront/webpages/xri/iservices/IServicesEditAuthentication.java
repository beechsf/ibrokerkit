package ibrokerkit.ibrokerfront.webpages.xri.iservices;

import ibrokerkit.ibrokerfront.webapplication.IbrokerApplication;
import ibrokerkit.iname4java.store.Xri;
import ibrokerkit.iservicestore.store.Authentication;
import ibrokerkit.iservicestore.store.Store;
import ibrokerkit.iservicestore.store.StoreException;

import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;


public class IServicesEditAuthentication extends IServices {

	private static final long serialVersionUID = 4335727852672594999L;

	public IServicesEditAuthentication(Xri xri, Authentication authentication) {

		this(xri, true, authentication);
	}

	protected IServicesEditAuthentication(Xri xri, boolean createPages, Authentication authentication) {

		super(xri, createPages);

		// create and add components

		this.add(new EditForm("editForm", new CompoundPropertyModel(authentication)));
	}

	private class EditForm extends Form {

		private static final long serialVersionUID = -286080184783434987L;
		private TextField nameTextField;
		private CheckBox enabledCheckBox;

		private EditForm(String id, IModel model) {

			super(id, model);

			// create components

			this.nameTextField = new TextField("name");
			this.enabledCheckBox = new CheckBox("enabled");

			// add components

			this.add(this.nameTextField);
			this.add(this.enabledCheckBox);
		}

		@Override
		protected void onSubmit() {

			Store store = ((IbrokerApplication) Application.get()).getIserviceStore();
			Authentication authentication = (Authentication) this.getModelObject();

			try {

				// update the authentication i-service

				store.updateObject(authentication);

				// and go back to the authentications list

				Page page = new IServicesListAuthentications(IServicesEditAuthentication.this.xri);
				page.info(IServicesEditAuthentication.this.getString("success"));
				this.setResponsePage(page);
			} catch (StoreException ex) {

				throw new RuntimeException("Problem while updating the Authentication i-service.", ex);
			}
		}
	}
}
