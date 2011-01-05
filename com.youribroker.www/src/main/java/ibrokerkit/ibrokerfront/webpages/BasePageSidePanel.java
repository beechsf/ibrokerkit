package ibrokerkit.ibrokerfront.webpages;

import ibrokerkit.ibrokerfront.webapplication.IbrokerApplication;
import ibrokerkit.ibrokerfront.webpages.index.Index;
import ibrokerkit.ibrokerfront.webpages.information.Policies;
import ibrokerkit.ibrokerfront.webpages.xri.YourXRIs;
import ibrokerkit.ibrokerfront.webpages.xri.renew.RenewXRI;
import ibrokerkit.ibrokerfront.webpages.xri.transfer.TransferXRI;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;


public class BasePageSidePanel extends Panel {

	private static final long serialVersionUID = -6517057009303244973L;

	public BasePageSidePanel(String id) {

		super(id);

		// create and add components
		
		this.add(new BookmarkablePageLink("PoliciesLink", Policies.class));
		this.add(new ExternalLink("ContactLink", ((IbrokerApplication) this.getApplication()).getProperties().getProperty("contact-link")));

		this.add(new BookmarkablePageLink("AddXRILink", Index.class));
		this.add(new BookmarkablePageLink("TransferXRILink", TransferXRI.class));
		this.add(new BookmarkablePageLink("RenewXRILink", RenewXRI.class));
		this.add(new BookmarkablePageLink("YourXRIsLink", YourXRIs.class));
	}
}
