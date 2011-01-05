package ibrokerkit.ibrokerfront.webpages.xri.config;

import ibrokerkit.ibrokerfront.models.XriRefsModel;
import ibrokerkit.iname4java.store.Xri;
import ibrokerkit.iname4java.store.XriStoreException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.openxri.xml.Ref;



public class ConfigXRIRefs extends ConfigXRI {

	private static final long serialVersionUID = 2315060133099664001L;

	private static Log log = LogFactory.getLog(ConfigXRIRefs.class.getName());

	private XriRefsModel authorityRefsModel;
	private ListView authorityRefsListView;

	public ConfigXRIRefs(Xri xri) {

		this(xri, true);
	}

	protected ConfigXRIRefs(Xri xri, boolean createPages) {

		super(xri, createPages);

		// create and add components

		this.authorityRefsModel = new XriRefsModel(this.xri);

		this.authorityRefsListView = new ListView("refs", this.authorityRefsModel) {

			private static final long serialVersionUID = -8849142156157353378L;

			@Override
			protected void populateItem(ListItem item) {

				final Ref ref = (Ref) item.getModelObject();
				String value = ref.getValue() != null ? ref.getValue() : "-";
				String priority = ref.getPriority() == null ? "" : "[PRIORITY=" + ref.getPriority().toString() + "]";

				item.add(new DeleteLink("deleteButton", ref));
				item.add(new Label("nr", Integer.toString(item.getIndex() + 1)));
				item.add(new Label("value", value));
				item.add(new Label("priority", priority));
			}
		};
		this.authorityRefsListView.setOutputMarkupId(true);
		this.add(this.authorityRefsListView);
	}

	@Override
	protected void onBeforeRender() {

		if (this.get("intro") != null) this.remove("intro");

		if (this.authorityRefsModel.getSize() > 0) {

			this.add(new Fragment("intro", "intro1Fragment", this));
		} else {

			this.add(new Fragment("intro", "intro2Fragment", this));
		}

		super.onBeforeRender();
	}
	
	private class DeleteLink extends Link {

		private static final long serialVersionUID = -5936784413210447590L;

		private Ref ref;

		private DeleteLink(String id, Ref ref) {

			super(id);

			this.ref = ref;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void onClick() {

			// delete ref
			
			try {

				ConfigXRIRefs.this.xri.deleteRef(this.ref);
			} catch (XriStoreException ex) {

				ConfigXRIRefs.log.error(ex);
				error(getString("deletefail") + ex.getLocalizedMessage());
				return;
			}

			this.info(getString("deletesuccess"));

			// update model

			ConfigXRIRefs.this.authorityRefsModel.detach();
		}
	}
}
