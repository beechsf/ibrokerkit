package ibrokerkit.iserviceadmin.webpages.error;

import ibrokerkit.iserviceadmin.webpages.BasePage;

public class PageExpired extends BasePage {

	private static final long serialVersionUID = -7413246788655059821L;

	public PageExpired() {
		
		this.setTitle(this.getString("title"));
	}

	@Override
	public boolean isErrorPage() {
		
		return(true);
	}
}
