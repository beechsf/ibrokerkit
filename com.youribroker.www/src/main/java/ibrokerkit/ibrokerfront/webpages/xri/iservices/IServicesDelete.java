package ibrokerkit.ibrokerfront.webpages.xri.iservices;

import ibrokerkit.ibrokerfront.webapplication.IbrokerApplication;
import ibrokerkit.iname4java.store.Xri;
import ibrokerkit.iservicestore.store.IService;
import ibrokerkit.iservicestore.store.Store;
import ibrokerkit.iservicestore.store.StoreException;

import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;


public class IServicesDelete extends IServices {

	private static final long serialVersionUID = -3111843328690870063L;

	public IServicesDelete(Xri xri, IService iservice) {

		this(xri, true, iservice);
	}

	protected IServicesDelete(Xri xri, boolean createPages, IService iservice) {

		super(xri, createPages);

		// create and add components

		this.add(new DeleteForm("deleteForm", new Model(iservice)));
	}

	private class DeleteForm extends Form {

		private static final long serialVersionUID = 6858636014898320782L;

		private DeleteForm(String id, IModel model) {

			super(id, model);
		}

		@Override
		protected void onSubmit() {

			Store store = ((IbrokerApplication) Application.get()).getIserviceStore();
			IService iservice = (IService) this.getModelObject();

			try {

				// delete the i-service

				store.deleteObject(iservice);

				// and go back to the XRI list

				Page page = new IServicesIndex(IServicesDelete.this.xri);
				page.info(IServicesDelete.this.getString("success"));
				this.setResponsePage(page);
			} catch (StoreException ex) {

				throw new RuntimeException("Problem while deleting an i-service.", ex);
			}
		}
	}
}
