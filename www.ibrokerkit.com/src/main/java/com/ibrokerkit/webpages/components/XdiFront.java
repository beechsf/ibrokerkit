package com.ibrokerkit.webpages.components;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;

import com.ibrokerkit.webpages.BasePage;

public class XdiFront extends BasePage {

	private static final long serialVersionUID = -3288550596918788741L;

	public XdiFront() {

		this.setTitle(this.getString("title"));

		// create and add components

		this.add(new BookmarkablePageLink("XdiFrontExamplesLink", XdiFrontExamples.class));
	}
}
