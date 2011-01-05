package ibrokerkit.ibrokerfront.webpages.xri.iservices;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.link.IPageLink;
import org.apache.wicket.markup.html.link.PageLink;
import org.apache.wicket.markup.html.panel.Panel;

public class IServicesMenuPanel extends Panel {

	private static final long serialVersionUID = 252147328975257004L;

	public IServicesMenuPanel(String id, Page[] pages) {

		super(id);

		// create and add components

		for (int i=1; i<=pages.length; i++) {

			final Page page = pages[i-1];

			this.add(new PageLink("link" + i, new IPageLink() {

				private static final long serialVersionUID = -4365180300523221554L;

				public Page getPage() {

					return(page);
				}

				public Class<?> getPageIdentity() {

					return(page.getClass());
				}
			}));
		}
	}
}
