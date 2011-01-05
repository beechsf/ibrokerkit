package com.ibrokerkit.webpages;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;

import com.ibrokerkit.webpages.components.Epptools4Java;
import com.ibrokerkit.webpages.components.IbrokerCert;
import com.ibrokerkit.webpages.components.IbrokerFront;
import com.ibrokerkit.webpages.components.IbrokerStore;
import com.ibrokerkit.webpages.components.IbrokerTask;
import com.ibrokerkit.webpages.components.Iname4Java;
import com.ibrokerkit.webpages.components.IserviceAdmin;
import com.ibrokerkit.webpages.components.IserviceFront;
import com.ibrokerkit.webpages.components.IserviceStore;
import com.ibrokerkit.webpages.components.OauthFront;
import com.ibrokerkit.webpages.components.Openxri;
import com.ibrokerkit.webpages.components.OpenxriAdmin;
import com.ibrokerkit.webpages.components.XdiFront;
import com.ibrokerkit.webpages.components.Xritools4Java;
import com.ibrokerkit.webpages.index.Architecture1;
import com.ibrokerkit.webpages.index.Architecture2;
import com.ibrokerkit.webpages.index.Index;
import com.ibrokerkit.webpages.index.References;
import com.ibrokerkit.webpages.index.Screenshots;
import com.ibrokerkit.webpages.installation.Howtos;
import com.ibrokerkit.webpages.installation.Installation;

public class BasePageSidePanel extends Panel {

	private static final long serialVersionUID = -6517057009303244973L;

	public BasePageSidePanel(String id) {

		super(id);

		// create and add components
		
		this.add(new BookmarkablePageLink("HomeLink", Index.class));
		this.add(new BookmarkablePageLink("ScreenshotsLink", Screenshots.class));
		this.add(new BookmarkablePageLink("Architecture1Link", Architecture1.class));
		this.add(new BookmarkablePageLink("Architecture2Link", Architecture2.class));
		this.add(new BookmarkablePageLink("ReferencesLink", References.class));
		this.add(new BookmarkablePageLink("IbrokerStoreLink", IbrokerStore.class));
		this.add(new BookmarkablePageLink("IbrokerFrontLink", IbrokerFront.class));
		this.add(new BookmarkablePageLink("IbrokerTaskLink", IbrokerTask.class));
		this.add(new BookmarkablePageLink("IbrokerCertLink", IbrokerCert.class));
		this.add(new BookmarkablePageLink("OpenxriLink", Openxri.class));
		this.add(new BookmarkablePageLink("OpenxriAdminLink", OpenxriAdmin.class));
		this.add(new BookmarkablePageLink("IserviceFrontLink", IserviceFront.class));
		this.add(new BookmarkablePageLink("IserviceStoreLink", IserviceStore.class));
		this.add(new BookmarkablePageLink("IserviceAdminLink", IserviceAdmin.class));
		this.add(new BookmarkablePageLink("OauthFrontLink", OauthFront.class));
		this.add(new BookmarkablePageLink("XdiFrontLink", XdiFront.class));
		this.add(new BookmarkablePageLink("Iname4JavaLink", Iname4Java.class));
		this.add(new BookmarkablePageLink("Epptools4JavaLink", Epptools4Java.class));
		this.add(new BookmarkablePageLink("Xritools4JavaLink", Xritools4Java.class));
		this.add(new BookmarkablePageLink("InstallationLink", Installation.class));
		this.add(new BookmarkablePageLink("HowtosLink", Howtos.class));
	}
}
