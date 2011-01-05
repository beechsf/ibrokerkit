package ibrokerkit.ibrokerfront.webpages.xri.iservices;

import ibrokerkit.ibrokerfront.models.InameLocatorsModel;
import ibrokerkit.ibrokerfront.webapplication.IbrokerApplication;
import ibrokerkit.iname4java.store.Xri;
import ibrokerkit.iservicestore.store.Locator;

import java.util.Properties;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.IPageLink;
import org.apache.wicket.markup.html.link.PageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;

public class IServicesListLocators extends IServices {

	private static final long serialVersionUID = -6661850253354227274L;

	public IServicesListLocators(Xri xri) {

		this(xri, true);
	}

	protected IServicesListLocators(final Xri xri, boolean createPages) {

		super(xri, createPages);

		// create and add components
		
		InameLocatorsModel userQxriLocatorsModel = new InameLocatorsModel(xri);

		ListView userLocatorsListView = new ListView("userQxriLocators", userQxriLocatorsModel) {

			private static final long serialVersionUID = 1299356374762125475L;

			@Override
			protected void populateItem(ListItem item) {

				final Locator locator = (Locator) item.getModelObject();

				Fragment qxriBound = new Fragment("qxriBound", "qxriBoundFragment", item);
				qxriBound.setVisible(! locator.getQxri().equals(xri.getAuthorityId()));
				item.add(qxriBound);

				item.add(new Label("name", locator.getName()));
				item.add(new Label("enabled", locator.getEnabled().toString()));
				item.add(new ExternalLink("viewButton", getServiceLink(xri)));
				
				item.add(new PageLink("editButton", new IPageLink() {

					private static final long serialVersionUID = 2628982231653215031L;

					public Page getPage() {
						
						return(new IServicesEditLocator(xri, locator));
					}

					public Class<?> getPageIdentity() {
						
						return(IServicesEditLocator.class);
					}
					
				}));
				
				item.add(new PageLink("deleteButton", new IPageLink() {

					private static final long serialVersionUID = 3909956236618618615L;

					public Page getPage() {
						
						return(new IServicesDelete(xri, locator));
					}

					public Class<?> getPageIdentity() {
						
						return(IServicesDelete.class);
					}
				}));
			}
		};

		this.add(userLocatorsListView);
		this.add(new Label("locatorCount", Integer.toString(userQxriLocatorsModel.getSize())));
	}

	private String getServiceLink(Xri xri) {

		Properties properties = ((IbrokerApplication) this.getApplication()).getProperties();

		String link = properties.getProperty("locator-service");
		if (! xri.isStale()) link += xri.toString();

		return(link);
	}
}
