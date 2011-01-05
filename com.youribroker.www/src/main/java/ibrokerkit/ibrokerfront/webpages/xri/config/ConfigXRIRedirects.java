package ibrokerkit.ibrokerfront.webpages.xri.config;

import ibrokerkit.ibrokerfront.models.XriRedirectsModel;
import ibrokerkit.iname4java.store.Xri;
import ibrokerkit.iname4java.store.XriStoreException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.openxri.xml.Redirect;


public class ConfigXRIRedirects extends ConfigXRI {

	private static final long serialVersionUID = 6830463839025880171L;

	private static Log log = LogFactory.getLog(ConfigXRIRedirects.class.getName());

	private XriRedirectsModel authorityRedirectsModel;
	private ListView authorityRedirectsListView;

	public ConfigXRIRedirects(Xri xri) {

		this(xri, true);
	}

	protected ConfigXRIRedirects(Xri xri, boolean createPages) {

		super(xri, createPages);

		// create and add components

		this.authorityRedirectsModel = new XriRedirectsModel(this.xri);

		this.authorityRedirectsListView = new ListView("redirects", this.authorityRedirectsModel) {

			private static final long serialVersionUID = -8849142156157353378L;

			@Override
			protected void populateItem(ListItem item) {

				final Redirect redirect = (Redirect) item.getModelObject();
				String value = redirect.getValue() != null ? redirect.getValue() : "-";
				String priority = redirect.getPriority() == null ? "" : "[PRIORITY=" + redirect.getPriority().toString() + "]";
				String append = redirect.getAppend() != null ? redirect.getAppend().toString() : "-";

				item.add(new DeleteLink("deleteButton", redirect));
				item.add(new Label("nr", Integer.toString(item.getIndex() + 1)));
				item.add(new Label("value", value));
				item.add(new Label("priority", priority));
				item.add(new Label("append", append));
			}
		};
		this.authorityRedirectsListView.setOutputMarkupId(true);
		this.add(this.authorityRedirectsListView);
	}

	@Override
	protected void onBeforeRender() {

		if (this.get("intro") != null) this.remove("intro");

		if (this.authorityRedirectsModel.getSize() > 0) {

			this.add(new Fragment("intro", "intro1Fragment", this));
		} else {

			this.add(new Fragment("intro", "intro2Fragment", this));
		}

		super.onBeforeRender();
	}
	
	private class DeleteLink extends Link {

		private static final long serialVersionUID = -5936784413210447590L;

		private Redirect redirect;

		private DeleteLink(String id, Redirect redirect) {

			super(id);

			this.redirect = redirect;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void onClick() {

			// delete redirect
			
			try {

				ConfigXRIRedirects.this.xri.deleteRedirect(this.redirect);
			} catch (XriStoreException ex) {

				ConfigXRIRedirects.log.error(ex);
				error(getString("deletefail") + ex.getLocalizedMessage());
				return;
			}

			this.info(getString("deletesuccess"));

			// update model

			ConfigXRIRedirects.this.authorityRedirectsModel.detach();
		}
	}
}
