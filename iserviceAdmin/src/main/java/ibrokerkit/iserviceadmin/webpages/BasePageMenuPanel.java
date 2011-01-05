package ibrokerkit.iserviceadmin.webpages;

import ibrokerkit.iserviceadmin.webpages.authentications.ListAuthentications;
import ibrokerkit.iserviceadmin.webpages.contacts.ListContacts;
import ibrokerkit.iserviceadmin.webpages.forwardings.ListForwardings;
import ibrokerkit.iserviceadmin.webpages.locators.ListLocators;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;

public class BasePageMenuPanel extends Panel {

	private static final long serialVersionUID = -3051759483762447878L;

	public BasePageMenuPanel(String id) {

		super(id);

		// create and add components

		this.add(new BookmarkablePageLink("homeLink", this.getApplication().getHomePage()));

		this.add(new BookmarkablePageLink("authenticationsLink", ListAuthentications.class));

		this.add(new BookmarkablePageLink("contactsLink", ListContacts.class));

		this.add(new BookmarkablePageLink("forwardingsLink", ListForwardings.class));

		this.add(new BookmarkablePageLink("locatorsLink", ListLocators.class));
	}
}
