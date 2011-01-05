package ibrokerkit.iserviceadmin.webpages.locators;

import ibrokerkit.iserviceadmin.models.AllLocatorsModel;
import ibrokerkit.iserviceadmin.webapplication.IServiceAdminApplication;
import ibrokerkit.iserviceadmin.webpages.BasePage;
import ibrokerkit.iservicestore.store.Locator;
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

public class ListLocators extends BasePage {

	private static final long serialVersionUID = -1037291139973235893L;

	private static final Log log = LogFactory.getLog(ListLocators.class);

	public ListLocators() {

		this.setTitle(this.getString("title"));

		// create and add components

		this.add(new ListView("locators", new AllLocatorsModel()) {

			private static final long serialVersionUID = -3389132214239711659L;

			@Override
			protected void populateItem(ListItem item) {

				Locator locator = (Locator) item.getModelObject();

				PageParameters parameters = new PageParameters();
				parameters.put("id", locator.getId());

				item.add(new Label("id", locator.getId().toString()));
				item.add(new Label("indx", locator.getIndx()));
				item.add(new Label("name", locator.getName()));
				item.add(new BookmarkablePageLink("editLink", EditLocator.class, parameters));
				item.add(new DeleteLink("deleteLink", locator));
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

			// create locator

			Locator locator;

			try {

				locator = iservice4javaStore.createLocator();
			} catch (StoreException ex) {

				ListLocators.log.error(ex);
				ListLocators.this.error(ListLocators.this.getString("storefail") + ex.getLocalizedMessage());
				return;
			}

			// go to edit page

			PageParameters parameters = new PageParameters();
			parameters.put("id", locator.getId());
			
			this.setResponsePage(EditLocator.class, parameters);
		}
	}

	private class DeleteLink extends Link {

		private static final long serialVersionUID = -5645609067816663587L;

		private Locator locator;

		public DeleteLink(String id, final Locator locator) {

			super(id);

			this.locator = locator;
		}

		@Override
		public void onClick() {

			final Store iservice4javaStore = ((IServiceAdminApplication) this.getApplication()).getIserviceStore();

			// delete locator

			try {

				iservice4javaStore.deleteObject(this.locator);
			} catch (StoreException ex) {

				ListLocators.log.error(ex);
				ListLocators.this.error(ListLocators.this.getString("storefail") + ex.getLocalizedMessage());
				return;
			}
		}
	}
}
