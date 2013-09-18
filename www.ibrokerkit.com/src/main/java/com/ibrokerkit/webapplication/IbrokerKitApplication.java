package com.ibrokerkit.webapplication;

import org.apache.wicket.Page;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.protocol.http.WebApplication;

import com.ibrokerkit.webpages.components.Openxri;
import com.ibrokerkit.webpages.error.InternalError;
import com.ibrokerkit.webpages.error.PageExpired;
import com.ibrokerkit.webpages.index.Index;
import com.ibrokerkit.webpages.installation.Installation;

public class IbrokerKitApplication extends WebApplication {

	@Override
	public void init() {

		// set up page mounting

		this.mountPackage("/home", Index.class);
		this.mountPackage("/components", Openxri.class);
		this.mountPackage("/installation", Installation.class);

		// set up various wicket parameters

		this.getApplicationSettings().setInternalErrorPage(InternalError.class);
		this.getApplicationSettings().setPageExpiredErrorPage(PageExpired.class);
		this.getMarkupSettings().setDefaultMarkupEncoding("UTF-8");
		this.getMarkupSettings().setStripComments(true);
		this.getMarkupSettings().setStripWicketTags(true);
		this.getMarkupSettings().setCompressWhitespace(true);
		this.getRequestCycleListeners().add(new IbrokerKitRequestCycleListener());

		// DEPLOYMENT

		this.getMarkupSettings().setStripWicketTags(true);
		this.getMarkupSettings().setStripComments(true);
		this.getResourceSettings().setResourcePollFrequency(null);
		this.getDebugSettings().setComponentUseCheck(false);
		this.getDebugSettings().setAjaxDebugModeEnabled(false);
	}

	@Override
	public Class<? extends Page> getHomePage() {

		return(Index.class);
	}

	@Override
	public RuntimeConfigurationType getConfigurationType() {

		return RuntimeConfigurationType.DEPLOYMENT;
	}
}
