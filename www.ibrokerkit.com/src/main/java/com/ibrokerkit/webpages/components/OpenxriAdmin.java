package com.ibrokerkit.webpages.components;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;

import com.ibrokerkit.webpages.BasePage;

public class OpenxriAdmin extends BasePage {

	private static final long serialVersionUID = -3288550596918043901L;

	public OpenxriAdmin() {

		this.setTitle(this.getString("title"));

		this.add(new BookmarkablePageLink("OpenxriLink", Openxri.class));
	}
}
