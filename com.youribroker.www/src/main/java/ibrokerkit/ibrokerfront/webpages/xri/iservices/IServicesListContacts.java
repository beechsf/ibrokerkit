package ibrokerkit.ibrokerfront.webpages.xri.iservices;

import ibrokerkit.ibrokerfront.models.InameContactsModel;
import ibrokerkit.ibrokerfront.webapplication.IbrokerApplication;
import ibrokerkit.iname4java.store.Xri;
import ibrokerkit.iservicestore.store.Contact;

import java.util.Properties;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.IPageLink;
import org.apache.wicket.markup.html.link.PageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;


public class IServicesListContacts extends IServices {

	private static final long serialVersionUID = -7018330559731483872L;

	public IServicesListContacts(Xri xri) {

		this(xri, true);
	}

	protected IServicesListContacts(final Xri xri, boolean createPages) {

		super(xri, createPages);

		// create and add components

		InameContactsModel userQxriContactsModel = new InameContactsModel(xri);

		ListView userContactsListView = new ListView("userQxriContacts", userQxriContactsModel) {

			private static final long serialVersionUID = 1145117064146283854L;

			@Override
			protected void populateItem(ListItem item) {

				final Contact contact = (Contact) item.getModelObject();

				Fragment qxriBound = new Fragment("qxriBound", "qxriBoundFragment", item);
				qxriBound.setVisible(! contact.getQxri().equals(xri.getAuthorityId()));
				item.add(qxriBound);

				item.add(new Label("name", contact.getName()));
				item.add(new Label("enabled", contact.getEnabled().toString()));
				item.add(new ExternalLink("viewButton", getServiceLink(xri)));
				
				item.add(new PageLink("editButton", new IPageLink() {

					private static final long serialVersionUID = 2556143547426646688L;

					public Page getPage() {
						
						return(new IServicesEditContact(xri, contact));
					}

					public Class<?> getPageIdentity() {
						
						return(IServicesEditContact.class);
					}
				}));
				
				item.add(new PageLink("deleteButton", new IPageLink() {

					private static final long serialVersionUID = -7255943539801132603L;

					public Page getPage() {
						
						return(new IServicesDelete(xri, contact));
					}

					public Class<?> getPageIdentity() {
						
						return(IServicesDelete.class);
					}
				}));
			}
		};

		this.add(userContactsListView);
		this.add(new Label("contactCount", Integer.toString(userQxriContactsModel.getSize())));
	}

	private String getServiceLink(Xri xri) {

		Properties properties = ((IbrokerApplication) this.getApplication()).getProperties();

		String link = properties.getProperty("contact-service");
		if (! xri.isStale()) link += xri.toString();

		return(link);
	}
}
