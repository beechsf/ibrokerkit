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
		
		this.add(new BookmarkablePageLink<String> ("HomeLink", Index.class));
		this.add(new BookmarkablePageLink<String> ("ScreenshotsLink", Screenshots.class));
		this.add(new BookmarkablePageLink<String> ("Architecture1Link", Architecture1.class));
		this.add(new BookmarkablePageLink<String> ("Architecture2Link", Architecture2.class));
		this.add(new BookmarkablePageLink<String> ("ReferencesLink", References.class));
		this.add(new BookmarkablePageLink<String> ("IbrokerStoreLink", IbrokerStore.class));
		this.add(new BookmarkablePageLink<String> ("IbrokerFrontLink", IbrokerFront.class));
		this.add(new BookmarkablePageLink<String> ("IbrokerTaskLink", IbrokerTask.class));
		this.add(new BookmarkablePageLink<String> ("IbrokerCertLink", IbrokerCert.class));
		this.add(new BookmarkablePageLink<String> ("OpenxriLink", Openxri.class));
		this.add(new BookmarkablePageLink<String> ("OpenxriAdminLink", OpenxriAdmin.class));
		this.add(new BookmarkablePageLink<String> ("IserviceFrontLink", IserviceFront.class));
		this.add(new BookmarkablePageLink<String> ("IserviceStoreLink", IserviceStore.class));
		this.add(new BookmarkablePageLink<String> ("IserviceAdminLink", IserviceAdmin.class));
		this.add(new BookmarkablePageLink<String> ("OauthFrontLink", OauthFront.class));
		this.add(new BookmarkablePageLink<String> ("XdiFrontLink", XdiFront.class));
		this.add(new BookmarkablePageLink<String> ("Iname4JavaLink", Iname4Java.class));
		this.add(new BookmarkablePageLink<String> ("Epptools4JavaLink", Epptools4Java.class));
		this.add(new BookmarkablePageLink<String> ("Xritools4JavaLink", Xritools4Java.class));
		this.add(new BookmarkablePageLink<String> ("InstallationLink", Installation.class));
		this.add(new BookmarkablePageLink<String> ("HowtosLink", Howtos.class));
	}
}
