package ibrokerkit.iserviceadmin.webpages.contacts;

import ibrokerkit.iserviceadmin.models.AllContactsModel;
import ibrokerkit.iserviceadmin.webapplication.IServiceAdminApplication;
import ibrokerkit.iserviceadmin.webpages.BasePage;
import ibrokerkit.iservicestore.store.Contact;
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

public class ListContacts extends BasePage {

	private static final long serialVersionUID = -1037291139973235893L;

	private static final Log log = LogFactory.getLog(ListContacts.class);

	public ListContacts() {

		this.setTitle(this.getString("title"));

		// create and add components

		this.add(new ListView("contacts", new AllContactsModel()) {

			private static final long serialVersionUID = -3389132214239711659L;

			@Override
			protected void populateItem(ListItem item) {

				Contact contact = (Contact) item.getModelObject();

				PageParameters parameters = new PageParameters();
				parameters.put("id", contact.getId());

				item.add(new Label("id", contact.getId().toString()));
				item.add(new Label("indx", contact.getIndx()));
				item.add(new Label("name", contact.getName()));
				item.add(new BookmarkablePageLink("editLink", EditContact.class, parameters));
				item.add(new DeleteLink("deleteLink", contact));
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

			// create contact

			Contact contact;

			try {

				contact = iservice4javaStore.createContact();
			} catch (StoreException ex) {

				ListContacts.log.error(ex);
				ListContacts.this.error(ListContacts.this.getString("storefail") + ex.getLocalizedMessage());
				return;
			}

			// go to edit page

			PageParameters parameters = new PageParameters();
			parameters.put("id", contact.getId());
			
			this.setResponsePage(EditContact.class, parameters);
		}
	}

	private class DeleteLink extends Link {

		private static final long serialVersionUID = -5645609067816663587L;

		private Contact contact;

		public DeleteLink(String id, final Contact contact) {

			super(id);

			this.contact = contact;
		}

		@Override
		public void onClick() {

			final Store iservice4javaStore = ((IServiceAdminApplication) this.getApplication()).getIserviceStore();

			// delete contact

			try {

				iservice4javaStore.deleteObject(this.contact);
			} catch (StoreException ex) {

				ListContacts.log.error(ex);
				ListContacts.this.error(ListContacts.this.getString("storefail") + ex.getLocalizedMessage());
				return;
			}
		}
	}
}
