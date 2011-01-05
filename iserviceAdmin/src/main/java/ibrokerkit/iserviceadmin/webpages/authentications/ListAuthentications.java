package ibrokerkit.iserviceadmin.webpages.authentications;

import ibrokerkit.iserviceadmin.models.AllAuthenticationsModel;
import ibrokerkit.iserviceadmin.webapplication.IServiceAdminApplication;
import ibrokerkit.iserviceadmin.webpages.BasePage;
import ibrokerkit.iservicestore.store.Authentication;
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

public class ListAuthentications extends BasePage {

	private static final long serialVersionUID = -1037291139973235893L;

	private static final Log log = LogFactory.getLog(ListAuthentications.class);

	public ListAuthentications() {

		this.setTitle(this.getString("title"));

		// create and add components

		this.add(new ListView("authentications", new AllAuthenticationsModel()) {

			private static final long serialVersionUID = -3389132214239711659L;

			@Override
			protected void populateItem(ListItem item) {

				Authentication authentication = (Authentication) item.getModelObject();

				PageParameters parameters = new PageParameters();
				parameters.put("id", authentication.getId());

				item.add(new Label("id", authentication.getId().toString()));
				item.add(new Label("indx", authentication.getIndx()));
				item.add(new Label("name", authentication.getName()));
				item.add(new BookmarkablePageLink("editLink", EditAuthentication.class, parameters));
				item.add(new DeleteLink("deleteLink", authentication));
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

			// create authentication

			Authentication authentication;

			try {

				authentication = iservice4javaStore.createAuthentication();
			} catch (StoreException ex) {

				ListAuthentications.log.error(ex);
				ListAuthentications.this.error(ListAuthentications.this.getString("storefail") + ex.getLocalizedMessage());
				return;
			}

			// go to edit page

			PageParameters parameters = new PageParameters();
			parameters.put("id", authentication.getId());
			
			this.setResponsePage(EditAuthentication.class, parameters);
		}
	}

	private class DeleteLink extends Link {

		private static final long serialVersionUID = -5645609067816663587L;

		private Authentication authentication;

		public DeleteLink(String id, final Authentication authentication) {

			super(id);

			this.authentication = authentication;
		}

		@Override
		public void onClick() {

			final Store iservice4javaStore = ((IServiceAdminApplication) this.getApplication()).getIserviceStore();

			// delete authentication

			try {

				iservice4javaStore.deleteObject(this.authentication);
			} catch (StoreException ex) {

				ListAuthentications.log.error(ex);
				ListAuthentications.this.error(ListAuthentications.this.getString("storefail") + ex.getLocalizedMessage());
				return;
			}
		}
	}
}
