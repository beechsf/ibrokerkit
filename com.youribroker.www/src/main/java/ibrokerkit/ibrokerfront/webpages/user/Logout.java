package ibrokerkit.ibrokerfront.webpages.user;

import ibrokerkit.ibrokerfront.webapplication.IbrokerSession;
import ibrokerkit.ibrokerfront.webpages.BasePage;
import ibrokerkit.ibrokerstore.store.User;

import org.apache.wicket.Application;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;

public class Logout extends BasePage {

	private static final long serialVersionUID = 8126698069469543780L;

	public Logout() {

		this.setTitle(this.getString("title"));

		// create and add components
		
		IbrokerSession session = (IbrokerSession) this.getSession();
		User user = session.getUser();
		String identifier = (user != null) ? user.getIdentifier() : "-";

		this.add(new Label("userIdentifier", identifier));
		this.add(new LogoutLink("logoutLink"));
	}

	private static class LogoutLink extends Link {
		
		private static final long serialVersionUID = 3416258156206380750L;

		public LogoutLink(String id) {
			
			super(id);
		}
		
		@Override
		public void onClick() {

			// logout user
			
			IbrokerSession session = (IbrokerSession) this.getSession();
			
			session.logoutUser();

			// send user to home page
			
			this.setResponsePage(Application.get().getHomePage());
		}
	}
}
