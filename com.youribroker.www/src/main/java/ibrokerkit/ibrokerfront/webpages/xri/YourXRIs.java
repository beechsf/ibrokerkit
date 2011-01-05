package ibrokerkit.ibrokerfront.webpages.xri;

import ibrokerkit.ibrokerfront.models.UserInamesModel;
import ibrokerkit.ibrokerfront.webapplication.flags.LoggedInPage;
import ibrokerkit.ibrokerfront.webpages.BasePage;
import ibrokerkit.ibrokerfront.webpages.xri.config.ConfigXRIIndex;
import ibrokerkit.ibrokerfront.webpages.xri.iservices.IServicesIndex;
import ibrokerkit.ibrokerfront.webpages.xri.wizard.WizardXRI;
import ibrokerkit.iname4java.store.Xri;

import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.IPageLink;
import org.apache.wicket.markup.html.link.PageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;


public class YourXRIs extends BasePage implements LoggedInPage {

	private static final long serialVersionUID = 547970543577563490L;

	public YourXRIs () {

		this.setTitle(this.getString("title"));

		// create and add components

		UserInamesModel userXrisModel = new UserInamesModel();

		ListView userXrisListView = new ListView("userXris", userXrisModel) {

			private static final long serialVersionUID = -3819833947577778478L;

			@Override
			protected void populateItem(ListItem item) {

				final Xri xri = (Xri) item.getModelObject();

				List<String> fullNames = xri.getFullNames();

				item.add(new PageLink("wizardButton", new IPageLink() {

					private static final long serialVersionUID = 7932201317262401556L;

					public Page getPage() {

						return(new WizardXRI(xri));
					}

					public Class<?> getPageIdentity() {
						
						return(WizardXRI.class);
					}
				}));
				
				item.add(new PageLink("iservicesButton", new IPageLink() {

					private static final long serialVersionUID = -5085950832995950069L;

					public Page getPage() {
						
						return(new IServicesIndex(xri));
					}

					public Class<?> getPageIdentity() {
						
						return(IServicesIndex.class);
					}
				}));
				
				item.add(new PageLink("configureButton", new IPageLink() {

					private static final long serialVersionUID = -8487587921493997938L;

					public Page getPage() {
						
						return(new ConfigXRIIndex(xri));
					}

					public Class<?> getPageIdentity() {
						
						return(ConfigXRIIndex.class);
					}
				}));
				
				item.add(new PageLink("deleteButton", new IPageLink() {

					private static final long serialVersionUID = -4650469595402855421L;

					public Page getPage() {
						
						return(new DeleteXRI(xri));
					}

					public Class<?> getPageIdentity() {
						
						return(DeleteXRI.class);
					}
				}));

				item.add(new ListView("fullNames", fullNames) {

					private static final long serialVersionUID = 4271898112101898374L;

					@Override
					protected void populateItem(ListItem item) {

						item.add(new Label("fullName", item.getModelObjectAsString()));
					}
				});
				item.add(new Label("registrationDate", new SimpleDateFormat().format(xri.getDate())));

				Fragment parentWarning = new Fragment("parentWarning", "parentWarningFragment", item);
				item.add(parentWarning);
				if (fullNames.size() <= 1) parentWarning.setVisible(false); 
			}
		};
		this.add(userXrisListView);

		this.add(new Label("xriCount", Integer.toString(userXrisListView.getList().size())));
	}
}
