package ibrokerkit.iserviceadmin.webpages.forwardings;

import ibrokerkit.iserviceadmin.components.MapPanel;
import ibrokerkit.iserviceadmin.webapplication.IServiceAdminApplication;
import ibrokerkit.iserviceadmin.webpages.BasePage;
import ibrokerkit.iservicestore.store.Forwarding;
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
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.PageLink;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;

public class EditForwarding extends BasePage {

	private static final long serialVersionUID = 1209589157235509150L;

	private static final Log log = LogFactory.getLog(EditForwarding.class);

	private Forwarding forwarding;

	public EditForwarding(PageParameters parameters) {

		this.setTitle(this.getString("title"));

		final Store iservice4javaStore = ((IServiceAdminApplication) Application.get()).getIserviceStore();

		try {

			this.forwarding = iservice4javaStore.getForwarding(new Long(parameters.getLong("id")));
		} catch (Exception ex) {

			EditForwarding.log.error(ex);
			EditForwarding.this.error(EditForwarding.this.getString("storefail") + ex.getLocalizedMessage());
			throw new RuntimeException(ex);
		}

		// create and add components

		this.add(new EditForm("editForm", new CompoundPropertyModel(this.forwarding)));
		this.add(new PageLink("backLink", ListForwardings.class));
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
			this.add(new CheckBox("indexPage"));
			this.add(new CheckBox("errorPage"));
			this.add(new MapPanel("mappings"));
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

				// update forwarding

				try {

					iservice4javaStore.updateObject(EditForwarding.this.forwarding);
				} catch (StoreException ex) {

					EditForwarding.log.error(ex);
					EditForwarding.this.error(EditForwarding.this.getString("storefail") + ex.getLocalizedMessage());
					return;
				}

				// go to list page

				this.setResponsePage(ListForwardings.class);
			}
		}
	}
}
