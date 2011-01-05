package ibrokerkit.iserviceadmin.webpages.forwardings;

import ibrokerkit.iserviceadmin.models.AllForwardingsModel;
import ibrokerkit.iserviceadmin.webapplication.IServiceAdminApplication;
import ibrokerkit.iserviceadmin.webpages.BasePage;
import ibrokerkit.iservicestore.store.Forwarding;
import ibrokerkit.iservicestore.store.Store;
import ibrokerkit.iservicestore.store.StoreException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;

public class ListForwardings extends BasePage {

	private static final long serialVersionUID = -1037291139973235893L;

	private static final Log log = LogFactory.getLog(ListForwardings.class);

	public ListForwardings() {

		this.setTitle(this.getString("title"));

		// create and add components

		this.add(new ListView("forwardings", new AllForwardingsModel()) {

			private static final long serialVersionUID = -3389132214239711659L;

			@Override
			protected void populateItem(ListItem item) {

				Forwarding forwarding = (Forwarding) item.getModelObject();

				PageParameters parameters = new PageParameters();
				parameters.put("id", forwarding.getId());

				item.add(new Label("id", forwarding.getId().toString()));
				item.add(new Label("indx", forwarding.getIndx()));
				item.add(new Label("name", forwarding.getName()));
				item.add(new BookmarkablePageLink("editLink", EditForwarding.class, parameters));
				item.add(new DeleteLink("deleteLink", forwarding));
			}
		});

		this.add(new CreateForm("createForm"));
	}

	private class CreateForm extends Form {

		private static final long serialVersionUID = 2622570754273118524L;

		public CreateForm(String id) {

			super(id);
		}

		@Override
		protected void onSubmit() {

			final Store iservice4javaStore = ((IServiceAdminApplication) this.getApplication()).getIserviceStore();

			// create forwarding

			Forwarding forwarding;

			try {

				forwarding = iservice4javaStore.createForwarding();
			} catch (StoreException ex) {

				ListForwardings.log.error(ex);
				ListForwardings.this.error(ListForwardings.this.getString("storefail") + ex.getLocalizedMessage());
				return;
			}

			// go to edit page

			PageParameters parameters = new PageParameters();
			parameters.put("id", forwarding.getId());
			
			this.setResponsePage(EditForwarding.class, parameters);
		}
	}

	private class DeleteLink extends Link {

		private static final long serialVersionUID = -5645609067816663587L;

		private Forwarding forwarding;

		public DeleteLink(String id, final Forwarding forwarding) {

			super(id);

			this.forwarding = forwarding;
		}

		@Override
		public void onClick() {

			final Store iservice4javaStore = ((IServiceAdminApplication) this.getApplication()).getIserviceStore();

			// delete forwarding

			try {

				iservice4javaStore.deleteObject(this.forwarding);
			} catch (StoreException ex) {

				ListForwardings.log.error(ex);
				ListForwardings.this.error(ListForwardings.this.getString("storefail") + ex.getLocalizedMessage());
				return;
			}
		}
	}
}
