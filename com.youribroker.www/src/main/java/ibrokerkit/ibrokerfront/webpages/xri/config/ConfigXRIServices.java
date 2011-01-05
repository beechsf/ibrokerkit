package ibrokerkit.ibrokerfront.webpages.xri.config;

import ibrokerkit.ibrokerfront.models.XriServicesModel;
import ibrokerkit.iname4java.store.Xri;
import ibrokerkit.iname4java.store.XriStoreException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.openxri.xml.SEPMediaType;
import org.openxri.xml.SEPPath;
import org.openxri.xml.SEPType;
import org.openxri.xml.SEPUri;
import org.openxri.xml.Service;


public class ConfigXRIServices extends ConfigXRI {

	private static final long serialVersionUID = 5156709826621850665L;

	private static Log log = LogFactory.getLog(ConfigXRIRefs.class.getName());

	private XriServicesModel authorityServicesModel;
	private ListView authorityServicesListView;

	public ConfigXRIServices(Xri xri) {

		this(xri, true);
	}

	protected ConfigXRIServices(Xri xri, boolean createPages) {

		super(xri, createPages);

		// create and add components

		this.authorityServicesModel = new XriServicesModel(this.xri);

		this.authorityServicesListView = new ListView("services", this.authorityServicesModel) {

			private static final long serialVersionUID = -8139387906374452137L;

			@Override
			protected void populateItem(ListItem item) {

				final Service service = (Service) item.getModelObject();

				item.add(new DeleteLink("deleteButton", service));
				item.add(new Label("nr", Integer.toString(item.getIndex() + 1)));
				item.add(new Label("priority", service.getPriority() == null ? "" : "[PRIORITY=" + service.getPriority().toString() + "]"));
				item.add(new ListView("URIs", service.getURIs()) {

					private static final long serialVersionUID = 7823663096946362829L;

					@Override
					protected void populateItem(ListItem item) {

						SEPUri uri = (SEPUri) item.getModelObject();
						String label = uri.getURI().toString();
						if (uri.getAppend() != null) label += " [APPEND=" + uri.getAppend() + "]";
						if (uri.getPriority() != null) label += " [PRIORITY=" + uri.getPriority() + "]";
						item.add(new Label("uri", label));
					}
				});
				item.add(new ListView("paths", service.getPaths()) {

					private static final long serialVersionUID = -6112031272698714331L;

					@Override
					protected void populateItem(ListItem item) {

						SEPPath path = (SEPPath) item.getModelObject();
						String label = path.getPath();
						if (path.getMatch() != null) label += " [MATCH=" + path.getMatch() + "]";
						if (path.getSelect()) label += " [SELECT]";
						item.add(new Label("path", label));
					}
				});
				item.add(new ListView("types", service.getTypes()) {

					private static final long serialVersionUID = 734914981632621432L;

					@Override
					protected void populateItem(ListItem item) {

						SEPType type = (SEPType) item.getModelObject();
						String label = type.getType();
						if (type.getMatch() != null) label += " [MATCH=" + type.getMatch() + "]";
						if (type.getSelect()) label += " [SELECT]";
						item.add(new Label("type", label));
					}
				});
				item.add(new ListView("mediaTypes", service.getMediaTypes()) {

					private static final long serialVersionUID = -3826083136194524259L;

					@Override
					protected void populateItem(ListItem item) {

						SEPMediaType mediaType = (SEPMediaType) item.getModelObject();
						String label = mediaType.getMediaType();
						if (mediaType.getMatch() != null) label += " [MATCH=" + mediaType.getMatch() + "]";
						if (mediaType.getSelect()) label += " [SELECT]";
						item.add(new Label("mediaType", label));
					}
				});
			}
		};
		this.authorityServicesListView.setOutputMarkupId(true);
		this.add(this.authorityServicesListView);
	}

	@Override
	protected void onBeforeRender() {

		if (this.get("intro") != null) this.remove("intro");

		if (this.authorityServicesModel.getSize() > 0) {

			this.add(new Fragment("intro", "intro1Fragment", this));
		} else {

			this.add(new Fragment("intro", "intro2Fragment", this));
		}

		super.onBeforeRender();
	}

	private class DeleteLink extends Link {

		private static final long serialVersionUID = 7201840282886589550L;

		private Service service;

		private DeleteLink(String id, Service service) {

			super(id);

			this.service = service;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void onClick() {

			// delete service
			
			try {

				ConfigXRIServices.this.xri.deleteService(this.service);
			} catch (XriStoreException ex) {

				ConfigXRIServices.log.error(ex);
				error(getString("deletefail") + ex.getLocalizedMessage());
				return;
			}

			this.info(getString("deletesuccess"));

			// update model

			ConfigXRIServices.this.authorityServicesModel.detach();
		}
	}
}
