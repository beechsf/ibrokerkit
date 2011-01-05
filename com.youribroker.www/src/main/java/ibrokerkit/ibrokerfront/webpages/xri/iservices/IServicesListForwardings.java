package ibrokerkit.ibrokerfront.webpages.xri.iservices;

import ibrokerkit.ibrokerfront.models.InameForwardingsModel;
import ibrokerkit.ibrokerfront.webapplication.IbrokerApplication;
import ibrokerkit.iname4java.store.Xri;
import ibrokerkit.iservicestore.store.Forwarding;

import java.util.Properties;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.IPageLink;
import org.apache.wicket.markup.html.link.PageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.openxri.xml.ForwardingService;


public class IServicesListForwardings extends IServices {

	private static final long serialVersionUID = 5296973037387314385L;

	public IServicesListForwardings(Xri xri) {

		this(xri, true);
	}

	protected IServicesListForwardings(final Xri xri, boolean createPages) {

		super(xri, createPages);

		// create and add components
		
		InameForwardingsModel userQxriForwardingsModel = new InameForwardingsModel(xri);

		ListView userForwardingsListView = new ListView("userQxriForwardings", userQxriForwardingsModel) {

			private static final long serialVersionUID = -4788864573626154154L;

			@Override
			protected void populateItem(ListItem item) {

				final Forwarding forwarding = (Forwarding) item.getModelObject();

				Fragment qxriBound = new Fragment("qxriBound", "qxriBoundFragment", item);
				qxriBound.setVisible(! forwarding.getQxri().equals(xri.getAuthorityId()));
				item.add(qxriBound);

				item.add(new Label("name", forwarding.getName()));
				item.add(new Label("enabled", forwarding.getEnabled().toString()));
				item.add(new ExternalLink("viewButton", getServiceLink(xri)));
				
				item.add(new PageLink("editButton", new IPageLink() {

					private static final long serialVersionUID = -3054850438184365093L;

					public Page getPage() {
						
						return(new IServicesEditForwarding(xri, forwarding));
					}

					public Class<?> getPageIdentity() {
						
						return(IServicesEditForwarding.class);
					}
				}));
				
				item.add(new PageLink("deleteButton", new IPageLink() {

					private static final long serialVersionUID = 8434061014486625348L;

					public Page getPage() {
						
						return(new IServicesDelete(xri, forwarding));
					}

					public Class<?> getPageIdentity() {
						
						return(IServicesDelete.class);
					}
				}));
			}
		};

		this.add(userForwardingsListView);
		this.add(new Label("forwardingCount", Integer.toString(userQxriForwardingsModel.getSize())));
	}

	private String getServiceLink(Xri xri) {

		Properties properties = ((IbrokerApplication) this.getApplication()).getProperties();

		String link = properties.getProperty("forwarding-service");
		if (! xri.isStale()) link += xri.toString() + "/" + ForwardingService.INDEX_PATH;

		return(link);
	}
}
