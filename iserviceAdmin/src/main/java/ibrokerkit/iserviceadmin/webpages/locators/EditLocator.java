package ibrokerkit.iserviceadmin.webpages.locators;

import ibrokerkit.iserviceadmin.components.MapPanel;
import ibrokerkit.iserviceadmin.webapplication.IServiceAdminApplication;
import ibrokerkit.iserviceadmin.webpages.BasePage;
import ibrokerkit.iservicestore.store.Locator;
import ibrokerkit.iservicestore.store.Store;
import ibrokerkit.iservicestore.store.StoreException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Application;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.PageLink;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;

public class EditLocator extends BasePage {

	private static final long serialVersionUID = 1209589157235509150L;

	private static final Log log = LogFactory.getLog(EditLocator.class);

	private Locator locator;

	public EditLocator(PageParameters parameters) {

		this.setTitle(this.getString("title"));

		final Store iservice4javaStore = ((IServiceAdminApplication) Application.get()).getIserviceStore();

		try {

			this.locator = iservice4javaStore.getLocator(new Long(parameters.getLong("id")));
		} catch (Exception ex) {

			EditLocator.log.error(ex);
			EditLocator.this.error(EditLocator.this.getString("storefail") + ex.getLocalizedMessage());
			throw new RuntimeException(ex);
		}

		// create and add components

		this.add(new EditForm("editForm", new CompoundPropertyModel(this.locator)));
		this.add(new PageLink("backLink", ListLocators.class));
	}

	private class EditForm extends Form {

		private static final long serialVersionUID = 2622570754273118524L;

		public EditForm(String id, IModel model) {

			super(id, model);

			// create and add components
			
			this.add(new Label("id"));
			this.add(new TextField("qxri"));
			this.add(new TextField("name"));
			this.add(new CheckBox("enabled"));
			this.add(new MapPanel("attributes"));
			this.add(new TextField("indx"));
			this.add(new TextArea("description"));
			this.add(new TextArea("address"));
			this.add(new TextField("lat"));
			this.add(new TextField("lng"));
			this.add(new TextField("zoom"));
			this.add(new CheckBox("contactLink"));
			this.add(new MySubmitButton("submit"));
		}

		private class MySubmitButton extends Button {

			private static final long serialVersionUID = -7629912510152043987L;

			private MySubmitButton(String id) {

				super(id);
			}

			@Override
			public void onSubmit() {

				final Store iservice4javaStore = ((IServiceAdminApplication) this.getApplication()).getIserviceStore();

				// update locator

				try {

					iservice4javaStore.updateObject(EditLocator.this.locator);
				} catch (StoreException ex) {

					EditLocator.log.error(ex);
					EditLocator.this.error(EditLocator.this.getString("storefail") + ex.getLocalizedMessage());
					return;
				}

				// go to list page

				this.setResponsePage(ListLocators.class);
			}
		}
	}
}
