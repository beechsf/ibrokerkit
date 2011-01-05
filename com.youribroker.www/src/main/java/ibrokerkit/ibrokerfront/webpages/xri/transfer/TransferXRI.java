package ibrokerkit.ibrokerfront.webpages.xri.transfer;

import ibrokerkit.ibrokerfront.webpages.BasePage;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;

public class TransferXRI extends BasePage {

	private static final long serialVersionUID = -6144705899776948914L;

	public TransferXRI() {

		this.setTitle(this.getString("title"));

		// create and add components

		this.add(new BookmarkablePageLink("transferInLink", TransferXRIIn.class));
		this.add(new BookmarkablePageLink("transferOutLink", TransferXRIOut.class));
	}
}
