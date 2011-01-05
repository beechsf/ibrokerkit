package ibrokerkit.ibrokerfront.webpages.xri;

import ibrokerkit.ibrokerfront.components.CancelButton;
import ibrokerkit.ibrokerfront.webapplication.IbrokerApplication;
import ibrokerkit.ibrokerfront.webpages.BasePage;
import ibrokerkit.iname4java.store.Xri;
import ibrokerkit.iname4java.store.XriStore;
import ibrokerkit.iname4java.store.XriStoreException;

import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;


public class DeleteXRI extends BasePage {

	private static final long serialVersionUID = 9036821222134104713L;

	public DeleteXRI(Xri xri) {

		this.setTitle(this.getString("title"));

		// create and add components

		this.add(new DeleteForm("deleteForm", new Model(xri)));
		this.add(new Label("qxri", xri.toString()));
	}

	private class DeleteForm extends Form {

		private static final long serialVersionUID = 2439697485099333638L;

		private DeleteForm(String id, IModel model) {

			super(id, model);

			// create and add components

			this.add(new CancelButton("cancelButton", YourXRIs.class));
		}

		@Override
		protected void onSubmit() {

			XriStore iname4javaStore = ((IbrokerApplication) Application.get()).getXriStore();
			Xri xri = (Xri) this.getModelObject();

			try {

				// delete the xri

				iname4javaStore.deleteXri(xri);

				// and go back to the XRI list

				Page page = new YourXRIs();
				page.info(DeleteXRI.this.getString("success"));
				this.setResponsePage(page);
			} catch (XriStoreException ex) {

				throw new RuntimeException("Problem while deleting an XRI: " + ex.getMessage(), ex);
			}
		}
	}

}
