package com.ibrokerkit.webpages.components;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;

import com.ibrokerkit.webpages.BasePage;

public class Openxri extends BasePage {

	private static final long serialVersionUID = -3288550596918043901L;

	public Openxri() {

		this.setTitle(this.getString("title"));

		this.add(new BookmarkablePageLink("IbrokerStoreLink", IbrokerStore.class));
	}
}
