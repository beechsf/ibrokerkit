package ibrokerkit.ibrokerfront.webpages;

import ibrokerkit.ibrokerfront.webpages.user.EditUser;
import ibrokerkit.ibrokerfront.webpages.user.Logout;
import ibrokerkit.ibrokerfront.webpages.xri.YourXRIs;

import org.apache.wicket.Application;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;


public class BasePageLogoutPanel extends Panel {

	private static final long serialVersionUID = 2515285079079882691L;

	public BasePageLogoutPanel(String id) {

		super(id);

		// create and add components

		this.add(new BookmarkablePageLink("homePageLink", Application.get().getHomePage()));
		this.add(new BookmarkablePageLink("editLink", EditUser.class));
		this.add(new BookmarkablePageLink("inamesLink", YourXRIs.class));
		this.add(new BookmarkablePageLink("logoutLink", Logout.class));
	}
}
