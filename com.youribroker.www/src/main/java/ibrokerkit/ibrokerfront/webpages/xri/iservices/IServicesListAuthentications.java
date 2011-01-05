package ibrokerkit.ibrokerfront.webpages.xri.iservices;

import ibrokerkit.ibrokerfront.models.InameAuthenticationsModel;
import ibrokerkit.iname4java.store.Xri;
import ibrokerkit.iservicestore.store.Authentication;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.IPageLink;
import org.apache.wicket.markup.html.link.PageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;



public class IServicesListAuthentications extends IServices {

	private static final long serialVersionUID = -3043428925984043426L;

	public IServicesListAuthentications(Xri xri) {

		this(xri, true);
	}

	protected IServicesListAuthentications(final Xri xri, boolean createPages) {

		super(xri, createPages);

		// create and add components

		InameAuthenticationsModel userQxriAuthenticationsModel = new InameAuthenticationsModel(xri);

		ListView userAuthenticationsListView = new ListView("userQxriAuthentications", userQxriAuthenticationsModel) {

			private static final long serialVersionUID = 6134433917933066595L;

			@Override
			protected void populateItem(ListItem item) {

				final Authentication authentication = (Authentication) item.getModelObject();

				Fragment qxriBound = new Fragment("qxriBound", "qxriBoundFragment", item);
				qxriBound.setVisible(! authentication.getQxri().equals(xri.getAuthorityId()));
				item.add(qxriBound);

				item.add(new Label("name", authentication.getName()));
				item.add(new Label("enabled", authentication.getEnabled().toString()));

				item.add(new PageLink("editButton", new IPageLink() {

					private static final long serialVersionUID = -7481838232409302415L;

					public Page getPage() {

						return(new IServicesEditAuthentication(xri, authentication));
					}

					public Class<?> getPageIdentity() {

						return(IServicesEditAuthentication.class);
					}

				}));

				item.add(new PageLink("deleteButton", new IPageLink() {

					private static final long serialVersionUID = -300719787622110310L;

					public Page getPage() {

						return(new IServicesDelete(xri, authentication));
					}

					public Class<?> getPageIdentity() {

						return(IServicesDelete.class);
					}

				}));
			}
		};

		this.add(userAuthenticationsListView);
		this.add(new Label("authenticationCount", Integer.toString(userQxriAuthenticationsModel.getSize())));
	}
}
