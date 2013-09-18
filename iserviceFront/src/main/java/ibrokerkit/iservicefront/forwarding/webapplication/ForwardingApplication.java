package ibrokerkit.iservicefront.forwarding.webapplication;

import ibrokerkit.iservicefront.forwarding.webpages.error.InternalError;
import ibrokerkit.iservicefront.forwarding.webpages.error.PageExpired;
import ibrokerkit.iservicefront.forwarding.webpages.index.Index;
import ibrokerkit.iservicefront.forwarding.webpages.iservice.CustomPage;
import ibrokerkit.iservicefront.forwarding.webpages.iservice.EndpointStrategy;

import java.util.Properties;

import org.apache.velocity.app.Velocity;
import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.request.IRequestCycleProcessor;
import org.apache.wicket.spring.SpringWebApplication;

public class ForwardingApplication extends SpringWebApplication {

	protected Properties properties;
	protected Properties velocityProperties;
	protected ibrokerkit.iservicestore.store.Store iserviceStore;
	protected org.openxri.store.Store openxriStore;

	@Override
	public void init() {

		// set up page mounting

		this.mountBookmarkablePage("/page", CustomPage.class);
		this.mount(new EndpointStrategy(this.properties.getProperty("forwarding-endpoint-path")));

		// set up various wicket parameters

		this.getApplicationSettings().setClassResolver(new ForwardingClassResolver());
		this.getApplicationSettings().setInternalErrorPage(InternalError.class);
		this.getApplicationSettings().setPageExpiredErrorPage(PageExpired.class);
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

		return(new ForwardingRequestCycleProcessor());
	}

	@Override
	public String getConfigurationType() {

		return("DEPLOYMENT");
	}

	@Override
	public Session newSession(Request request, Response response) {

		return(new ForwardingSession(request));
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
}
