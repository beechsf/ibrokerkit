package com.ibrokerkit.webapplication;

import org.apache.wicket.request.IRequestCycleProcessor;
import org.apache.wicket.spring.SpringWebApplication;
import org.apache.wicket.util.lang.PackageName;

import com.ibrokerkit.webpages.components.Openxri;
import com.ibrokerkit.webpages.error.InternalError;
import com.ibrokerkit.webpages.error.PageExpired;
import com.ibrokerkit.webpages.index.Index;
import com.ibrokerkit.webpages.installation.Installation;

public class IbrokerKitApplication extends SpringWebApplication {

	@Override
	public void init() {

		// set up page mounting

		this.mount("/home", PackageName.forClass(Index.class));
		this.mount("/components", PackageName.forClass(Openxri.class));
		this.mount("/installation", PackageName.forClass(Installation.class));

		// set up various wicket parameters

		this.getApplicationSettings().setClassResolver(new IbrokerKitClassResolver());
		this.getApplicationSettings().setInternalErrorPage(InternalError.class);
		this.getApplicationSettings().setPageExpiredErrorPage(PageExpired.class);
		this.getMarkupSettings().setStripXmlDeclarationFromOutput(false);
		this.getMarkupSettings().setDefaultMarkupEncoding("UTF-8");
		this.getPageSettings().setAutomaticMultiWindowSupport(false);

		// DEPLOYMENT

		this.getMarkupSettings().setStripWicketTags(true);
		this.getMarkupSettings().setStripComments(true);
		this.getResourceSettings().setResourcePollFrequency(null);
		this.getDebugSettings().setComponentUseCheck(false);
		this.getDebugSettings().setAjaxDebugModeEnabled(false);
	}

	@Override
	public Class<?> getHomePage() {

		return(Index.class);
	}

	@Override
	protected IRequestCycleProcessor newRequestCycleProcessor() {

		return(new IbrokerKitRequestCycleProcessor());
	}

	@Override
	public String getConfigurationType() {

		return("DEPLOYMENT");
	}
}
