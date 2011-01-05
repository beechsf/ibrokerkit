package ibrokerkit.ibrokerfront.webpages.xri.config;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.link.IPageLink;
import org.apache.wicket.markup.html.link.PageLink;
import org.apache.wicket.markup.html.panel.Panel;

public class ConfigXRIMenuPanel extends Panel {

	private static final long serialVersionUID = 994768771531273859L;

	public ConfigXRIMenuPanel(String id, final Page[] pages) {

		super(id);

		// create and add components

		for (int i=1; i<=pages.length; i++) {

			final Page page = pages[i-1];

			this.add(new PageLink("link" + i, new IPageLink() {

				private static final long serialVersionUID = -5671081883344810833L;

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
