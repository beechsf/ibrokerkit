package com.ibrokerkit.webpages.error;

import com.ibrokerkit.webpages.BasePage;

public class InternalError extends BasePage {

	private static final long serialVersionUID = -2025103990399589635L;

	public InternalError() {
		
		this.setTitle(this.getString("title"));
	}
	
	@Override
	public boolean isErrorPage() {
		
		return(true);
	}
}
