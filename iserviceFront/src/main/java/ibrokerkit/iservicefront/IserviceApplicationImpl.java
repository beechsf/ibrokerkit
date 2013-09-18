package ibrokerkit.iservicefront;

import ibrokerkit.iservicefront.iservice.webpages.CustomPage;
import ibrokerkit.iservicefront.iservice.webpages.error.InternalErrorPage;
import ibrokerkit.iservicefront.iservice.webpages.error.PageExpiredPage;
import ibrokerkit.iservicefront.iservice.webpages.index.Index;

import java.util.Properties;

import org.apache.velocity.app.Velocity;
import org.apache.wicket.Page;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.Session;
import org.apache.wicket.core.request.mapper.MountedMapper;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.mapper.mount.MountMapper;
import org.apache.wicket.request.mapper.parameter.UrlPathPageParametersEncoder;
import org.openid4java.server.ServerManager;

public class IserviceApplicationImpl extends WebApplication implements IserviceApplication {

	protected Properties properties;
	protected Properties velocityProperties;
	protected ibrokerkit.iservicestore.store.Store iserviceStore;
	protected org.openxri.store.Store openxriStore;
	protected ServerManager serverManager;

	@Override
	public void init() {

		// set up page mounting

		this.mount(new MountedMapper("/page", CustomPage.class, new UrlPathPageParametersEncoder()));
		this.mount(new MountMapper(this.properties.getProperty("authentication-endpoint-path"), new ibrokerkit.iservicefront.authentication.webpages.iservice.Endpoint()));
		this.mount(new MountMapper(this.properties.getProperty("contact-endpoint-path"), new ibrokerkit.iservicefront.contact.webpages.iservice.Endpoint()));
		this.mount(new MountMapper(this.properties.getProperty("forwarding-endpoint-path"), new ibrokerkit.iservicefront.forwarding.webpages.iservice.Endpoint()));

		// set up various wicket parameters

		this.getApplicationSettings().setInternalErrorPage(InternalErrorPage.class);
		this.getApplicationSettings().setPageExpiredErrorPage(PageExpiredPage.class);
		this.getMarkupSettings().setDefaultMarkupEncoding("UTF-8");
		this.getMarkupSettings().setStripComments(true);
		this.getMarkupSettings().setStripWicketTags(true);
		this.getMarkupSettings().setCompressWhitespace(true);
		this.getRequestCycleListeners().add(new IserviceRequestCycleListener());

		// DEVELOPMENT

		/*		this.getMarkupSettings().setStripWicketTags(false);
		this.getMarkupSettings().setStripComments(false);
		this.getResourceSettings().setResourcePollFrequency(Duration.ONE_SECOND);
		this.getDebugSettings().setComponentUseCheck(true);
		this.getDebugSettings().setAjaxDebugModeEnabled(true);
		this.getRequestCycleListeners().add(new FullXRIRequestCycleListener());*/

		// DEPLOYMENT

		this.getMarkupSettings().setStripWicketTags(true);
		this.getMarkupSettings().setStripComments(true);
		this.getResourceSettings().setResourcePollFrequency(null);
		this.getDebugSettings().setComponentUseCheck(false);
		this.getDebugSettings().setAjaxDebugModeEnabled(false);

		// init velocity

		try {

			Velocity.init(this.velocityProperties);
		} catch (Exception ex) {

			throw new RuntimeException("Cannot initialize Velocity.", ex);
		}
	}

	@Override
	public Class<? extends Page> getHomePage() {

		return(Index.class);
	}

	@Override
	public RuntimeConfigurationType getConfigurationType() {

		return RuntimeConfigurationType.DEPLOYMENT;
	}

	@Override
	public Session newSession(Request request, Response response) {

		return(new IserviceSessionImpl(request));
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
