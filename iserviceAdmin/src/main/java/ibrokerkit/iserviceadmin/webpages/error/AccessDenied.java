package ibrokerkit.iserviceadmin.webpages.error;

import ibrokerkit.iserviceadmin.webpages.BasePage;

public class AccessDenied extends BasePage {

	private static final long serialVersionUID = 8194374213290259595L;

	public AccessDenied() {

		this.setTitle(this.getString("title"));
	}

	@Override
	public boolean isErrorPage() {
		
		return(true);
	}
}
