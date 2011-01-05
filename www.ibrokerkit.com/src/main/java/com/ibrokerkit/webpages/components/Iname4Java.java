package com.ibrokerkit.webpages.components;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;

import com.ibrokerkit.webpages.BasePage;

public class Iname4Java extends BasePage {

	private static final long serialVersionUID = -3288550596918043901L;

	public Iname4Java() {

		this.setTitle(this.getString("title"));

		this.add(new BookmarkablePageLink("OpenxriLink", Openxri.class));
		this.add(new BookmarkablePageLink("Epptools4JavaLink", Epptools4Java.class));
	}
}
