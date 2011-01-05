package ibrokerkit.ibrokerfront.webapplication;

import ibrokerkit.ibrokerfront.webapplication.flags.LoggedInPage;

import org.apache.wicket.Session;
import org.apache.wicket.authorization.strategies.page.AbstractPageAuthorizationStrategy;


public class IbrokerAuthorizationStrategy extends AbstractPageAuthorizationStrategy {

	@SuppressWarnings("unchecked")
	@Override
	protected boolean isPageAuthorized(Class pageClass) {

		Object user = ((IbrokerSession) Session.get()).getUser();

		// is the page only for logged-in users

		if (instanceOf(pageClass, LoggedInPage.class)) {

			return(user != null);
		}

		// everything else can be accessed by everyone

		return(true);
	}
}
