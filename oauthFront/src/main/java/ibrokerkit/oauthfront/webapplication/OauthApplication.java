package ibrokerkit.oauthfront.webapplication;

import ibrokerkit.epptools4java.EppTools;
import ibrokerkit.iname4java.store.impl.grs.GrsXriStore;
import ibrokerkit.iname4java.store.impl.openxri.OpenxriXriStore;
import ibrokerkit.oauthfront.webpages.error.InternalError;
import ibrokerkit.oauthfront.webpages.error.PageExpired;
import ibrokerkit.oauthfront.webpages.index.Index;
import ibrokerkit.oauthfront.webpages.oauth.AuthorizeRequestTokenEndpointStrategy;
import ibrokerkit.oauthfront.webpages.oauth.CustomPage;
import ibrokerkit.oauthfront.webpages.oauth.ExchangeRequestTokenEndpointStrategy;
import ibrokerkit.oauthfront.webpages.oauth.ExecuteAccessTokenEndpointStrategy;
import ibrokerkit.oauthfront.webpages.oauth.ObtainRequestTokenEndpointStrategy;

import java.util.Properties;

import org.apache.velocity.app.Velocity;
import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.request.IRequestCycleProcessor;
import org.apache.wicket.settings.IExceptionSettings;
import org.apache.wicket.spring.SpringWebApplication;
import org.openxri.config.ServerConfig;
import org.openxri.factories.ServerConfigFactory;
import org.openxri.store.Store;

public class OauthApplication extends SpringWebApplication {

	protected Properties properties;
	protected Properties velocityProperties;
	protected EppTools eppTools;
	protected ibrokerkit.ibrokerstore.store.Store ibrokerStore;
	protected ibrokerkit.iname4java.store.XriStore xriStore;

	@Override
	public void init() {

		// init OpenXRI ServletConfig and xriStore

		try {

			ServerConfig openxriServerConfig = ServerConfigFactory.initSingleton(this.getWicketFilter().getFilterConfig());
			if (this.eppTools != null)
				this.xriStore = new GrsXriStore(((Store) openxriServerConfig.getComponentRegistry().getComponent(Store.class)), this.eppTools);
			else
				this.xriStore = new OpenxriXriStore(((Store) openxriServerConfig.getComponentRegistry().getComponent(Store.class)));
		} catch (Exception ex) {

			throw new RuntimeException(ex);
		}

		// set up page mounting

		this.mountBookmarkablePage("/page", CustomPage.class);
		this.mount(new ObtainRequestTokenEndpointStrategy(this.properties.getProperty("obtainrequesttoken-endpoint-path")));
		this.mount(new AuthorizeRequestTokenEndpointStrategy(this.properties.getProperty("authorizerequesttoken-endpoint-path")));
		this.mount(new ExchangeRequestTokenEndpointStrategy(this.properties.getProperty("exchangerequesttoken-endpoint-path")));
		this.mount(new ExecuteAccessTokenEndpointStrategy(this.properties.getProperty("executeaccesstoken-endpoint-path")));

		// set up various wicket parameters

		this.getApplicationSettings().setClassResolver(new OauthClassResolver());
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

		return(new OauthRequestCycleProcessor());
	}

	@Override
	public String getConfigurationType() {

		return("DEPLOYMENT");
	}

	@Override
	public Session newSession(Request request, Response response) {

		return(new OauthSession(request));
	}

	public Properties getProperties() {

		return(this.properties);
	}

	public void setProperties(Properties properties) {

		this.properties = properties;
	}

	public Properties getVelocityProperties() {

		return(this.velocityProperties);
	}

	public void setVelocityProperties(Properties velocityProperties) {

		this.velocityProperties = velocityProperties;
	}

	public EppTools getEppTools() {

		return(this.eppTools);
	}

	public void setEppTools(EppTools eppTools) {

		this.eppTools = eppTools;
	}

	public ibrokerkit.ibrokerstore.store.Store getIbrokerStore() {

		return(this.ibrokerStore);
	}

	public void setIbrokerStore(ibrokerkit.ibrokerstore.store.Store ibrokerStore) {

		this.ibrokerStore = ibrokerStore;
	}

	public ibrokerkit.iname4java.store.XriStore getXriStore() {

		return(this.xriStore);
	}

	public void setXriStore(ibrokerkit.iname4java.store.XriStore xriStore) {

		this.xriStore = xriStore;
	}
}
