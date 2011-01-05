package ibrokerkit.ibrokerfront.webpages.error;

import ibrokerkit.ibrokerfront.webpages.BasePage;
import ibrokerkit.ibrokerfront.webpages.user.Login;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;


public class AccessDenied extends BasePage {

	private static final long serialVersionUID = 2381288353730940553L;

	public AccessDenied() {

		this.setTitle(this.getString("title"));
		
		// create and add components
		
		this.add(new BookmarkablePageLink("LoginLink", Login.class));
	}

	@Override
	public boolean isErrorPage() {
		
		return(true);
	}
}
