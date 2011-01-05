package ibrokerkit.ibrokerfront.webpages.index;

import ibrokerkit.ibrokerfront.webpages.BasePage;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;

public class Index extends BasePage {

	private static final long serialVersionUID = 2786725384239368435L;

	public Index() {

		this.setTitle(this.getString("title"));

		// create and add components

		this.add(new BookmarkablePageLink("grsButton", ibrokerkit.ibrokerfront.webpages.index.grs.Index.class));
		this.add(new BookmarkablePageLink("communityButton", ibrokerkit.ibrokerfront.webpages.index.community.Index.class));
	}
}
