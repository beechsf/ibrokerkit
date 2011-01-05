package ibrokerkit.iserviceadmin.webpages.error;

import ibrokerkit.iserviceadmin.webpages.BasePage;

public class InternalError extends BasePage {

	private static final long serialVersionUID = -104164585925887411L;

	public InternalError() {
		
		this.setTitle(this.getString("title"));
	}
	
	@Override
	public boolean isErrorPage() {
		
		return(true);
	}
}
