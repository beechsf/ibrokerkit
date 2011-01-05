package ibrokerkit.ibrokerfront.webpages;

import ibrokerkit.ibrokerfront.webpages.user.Login;

import org.apache.wicket.Application;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;


public class BasePageLoginPanel extends Panel {

	private static final long serialVersionUID = 2236257806901201618L;

	public BasePageLoginPanel(String id) {

		super(id);

		// create and add components

		this.add(new BookmarkablePageLink("homePageLink", Application.get().getHomePage()));
		this.add(new BookmarkablePageLink("loginLink", Login.class));
	}
}
