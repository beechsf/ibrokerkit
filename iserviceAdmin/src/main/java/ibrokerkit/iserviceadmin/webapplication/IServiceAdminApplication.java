package ibrokerkit.iserviceadmin.webapplication;

import ibrokerkit.iserviceadmin.webpages.error.AccessDenied;
import ibrokerkit.iserviceadmin.webpages.error.InternalError;
import ibrokerkit.iserviceadmin.webpages.error.PageExpired;

import java.util.Properties;

import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.request.IRequestCycleProcessor;
import org.apache.wicket.spring.SpringWebApplication;
import org.apache.wicket.util.lang.PackageName;

public class IServiceAdminApplication extends SpringWebApplication {

	protected Properties properties;
	protected ibrokerkit.iservicestore.store.Store iserviceStore;

	@Override
	public void init() {
	
		// set up page mounting

		this.mount("/home", PackageName.forClass(ibrokerkit.iserviceadmin.webpages.index.Index.class));
		this.mount("/authentications", PackageName.forClass(ibrokerkit.iserviceadmin.webpages.contacts.ListContacts.class));
		this.mount("/busys", PackageName.forClass(ibrokerkit.iserviceadmin.webpages.contacts.ListContacts.class));
		this.mount("/contacts", PackageName.forClass(ibrokerkit.iserviceadmin.webpages.contacts.ListContacts.class));
		this.mount("/forwardings", PackageName.forClass(ibrokerkit.iserviceadmin.webpages.contacts.ListContacts.class));
		this.mount("/locators", PackageName.forClass(ibrokerkit.iserviceadmin.webpages.contacts.ListContacts.class));
		this.mount("/sorry", PackageName.forClass(AccessDenied.class));

		// set up various wicket parameters

		this.getApplicationSettings().setClassResolver(new IServiceAdminClassResolver());
		this.getApplicationSettings().setAccessDeniedPage(AccessDenied.class);
		this.getApplicationSettings().setInternalErrorPage(InternalError.class);
		this.getApplicationSettings().setPageExpiredErrorPage(PageExpired.class);
		this.getSecuritySettings().setAuthorizationStrategy(new IServiceAdminAuthorizationStrategy());
		this.getMarkupSettings().setStripXmlDeclarationFromOutput(false);
		this.getMarkupSettings().setDefaultMarkupEncoding("UTF-8");

		// DEVELOPMENT

		/*		this.getMarkupSettings().setStripWicketTags(false);
		this.getMarkupSettings().setStripComments(false);
		this.getResourceSettings().setResourcePollFrequency(Duration.ONE_SECOND);
		this.getDebugSettings().setComponentUseCheck(true);
		this.getDebugSettings().setAjaxDebugModeEnabled(true);*/

		// DEPLOYMENT

		this.getMarkupSettings().setStripWicketTags(true);
		this.getMarkupSettings().setStripComments(true);
		this.getResourceSettings().setResourcePollFrequency(null);
		this.getDebugSettings().setComponentUseCheck(false);
		this.getDebugSettings().setAjaxDebugModeEnabled(false);
	}

	@Override
	public Class<?> getHomePage() {

		return(ibrokerkit.iserviceadmin.webpages.index.Index.class);
	}

	@Override
	protected IRequestCycleProcessor newRequestCycleProcessor() {

		return(new IServiceAdminRequestCycleProcessor());
	}

	@Override
	public String getConfigurationType() {
		
		return("DEPLOYMENT");
	}
	
	@Override
	public Session newSession(Request request, Response response) {

		return(new IServiceAdminSession(request));
	}

	public Properties getProperties() {
		
		return (this.properties);
	}

	public void setProperties(Properties properties) {
		
		this.properties = properties;
	}

	public ibrokerkit.iservicestore.store.Store getIserviceStore() {

		return(this.iserviceStore);
	}

	public void setIserviceStore(ibrokerkit.iservicestore.store.Store iservice4javaStore) {
		
		this.iserviceStore = iservice4javaStore;
	}
}
