package ibrokerkit.ibrokerfront.webpages.error;

import ibrokerkit.ibrokerfront.webpages.BasePage;

public class PageExpired extends BasePage {

	private static final long serialVersionUID = -8742800773604795312L;

	public PageExpired() {
		
		this.setTitle(this.getString("title"));
	}

	@Override
	public boolean isErrorPage() {
		
		return(true);
	}
}
