package ibrokerkit.iserviceadmin.webapplication;

import org.apache.wicket.authorization.strategies.page.AbstractPageAuthorizationStrategy;

public class IServiceAdminAuthorizationStrategy extends AbstractPageAuthorizationStrategy {

	@SuppressWarnings("unchecked")
	@Override
	protected boolean isPageAuthorized(Class pageClass) {
		
		// everything can be accessed by everyone
		
		return(true);
	}
}
