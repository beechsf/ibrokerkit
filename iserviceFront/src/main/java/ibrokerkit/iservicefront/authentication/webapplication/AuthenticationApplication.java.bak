package ibrokerkit.iservicefront.authentication.webapplication;

import ibrokerkit.iservicefront.authentication.webpages.error.InternalError;
import ibrokerkit.iservicefront.authentication.webpages.error.PageExpired;
import ibrokerkit.iservicefront.authentication.webpages.index.Index;
import ibrokerkit.iservicefront.authentication.webpages.iservice.CustomPage;
import ibrokerkit.iservicefront.authentication.webpages.iservice.EndpointStrategy;

import java.util.Properties;

import org.apache.velocity.app.Velocity;
import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.request.IRequestCycleProcessor;
import org.apache.wicket.settings.IExceptionSettings;
import org.apache.wicket.spring.SpringWebApplication;
import org.openid4java.server.ServerManager;

public class AuthenticationApplication extends SpringWebApplication {

	protected Properties properties;
	protected Properties velocityProperties;
	protected ibrokerkit.iservicestore.store.Store iserviceStore;
	protected org.openxri.store.Store openxriStore;
	protected ServerManager serverManager;

	@Override
	public void init() {

		// set up page mounting

		this.mountBookmarkablePage("/page", CustomPage.class);
		this.mount(new EndpointStrategy(this.properties.getProperty("authentication-endpoint-path")));

		// set up various wicket parameters

		this.getApplicationSettings().setClassResolver(new AuthenticationClassResolver());
		this.getApplicationSettings().setInternalErrorPage(InternalError.class);
		this.getApplicationSettings().setPageExpiredErrorPage(PageExpired.class);
		this.getMarkupSettings().setStripXmlDeclarationFromOutput(false);
		this.getMarkupSettings().setDefaultMarkupEncoding("UTF-8");

		// DEVELOPMENT

		/*		this.getMarkupSettings().setStripWicketTags(false);
		this.getMarkupSettings().setStripComments(false);
		this.getResourceSettings().setResourcePollFrequency(Duration.ONE_SECOND);
		this.getDebugSettings().setComponentUseCheck(true);
		this.getDebugSettings().setAjaxDebugModeEnabled(true);
		this.getExceptionSettings().setUnexpectedExceptionDisplay(IExceptionSettings.SHOW_EXCEPTION_PAGE);*/

		// DEPLOYMENT

		this.getMarkupSettings().setStripWicketTags(true);
		this.getMarkupSettings().setStripComments(true);
		this.getResourceSettings().setResourcePollFrequency(null);
		this.getDebugSettings().setComponentUseCheck(false);
		this.getDebugSettings().setAjaxDebugModeEnabled(false);
		this.getExceptionSettings().setUnexpectedExceptionDisplay(IExceptionSettings.SHOW_INTERNAL_ERROR_PAGE);

		// init velocity

		try {

			Velocity.init(this.velocityProperties);
		} catch (Exception ex) {

			throw new RuntimeException("Cannot initialize Velocity.", ex);
		}
	}

	@Override
	public Class<?> getHomePage() {

		return(Index.class);
	}

	@Override
	protected IRequestCycleProcessor newRequestCycleProcessor() {

		return(new AuthenticationRequestCycleProcessor());
	}

	@Override
	public String getConfigurationType() {
		
		return("DEPLOYMENT");
	}

	@Override
	public Session newSession(Request request, Response response) {

		return(new AuthenticationSession(request));
	}

	public ibrokerkit.iservicestore.store.Store getIserviceStore() {
		return (this.iserviceStore);
	}

	public void setIserviceStore(ibrokerkit.iservicestore.store.Store iserviceStore) {
		this.iserviceStore = iserviceStore;
	}

	public org.openxri.store.Store getOpenxriStore() {
		return (this.openxriStore);
	}

	public void setOpenxriStore(org.openxri.store.Store openxriStore) {
		this.openxriStore = openxriStore;
	}

	public Properties getProperties() {
		return (this.properties);
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public Properties getVelocityProperties() {
		return (this.velocityProperties);
	}

	public void setVelocityProperties(Properties velocityProperties) {
		this.velocityProperties = velocityProperties;
	}

	public ServerManager getServerManager() {
		return (this.serverManager);
	}

	public void setServerManager(ServerManager serverManager) {
		this.serverManager = serverManager;
	}
}
