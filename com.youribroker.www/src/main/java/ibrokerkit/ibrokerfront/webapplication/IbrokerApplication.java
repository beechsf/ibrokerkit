package ibrokerkit.ibrokerfront.webapplication;

import ibrokerkit.epptools4java.EppTools;
import ibrokerkit.ibrokerfront.webpages.error.AccessDenied;
import ibrokerkit.ibrokerfront.webpages.error.InternalError;
import ibrokerkit.ibrokerfront.webpages.error.PageExpired;
import ibrokerkit.ibrokerfront.webpages.index.Index;
import ibrokerkit.ibrokerfront.webpages.index.grs.DoRegister;
import ibrokerkit.ibrokerfront.webpages.information.TermsOfUse;
import ibrokerkit.ibrokerfront.webpages.user.Login;
import ibrokerkit.ibrokerfront.webpages.xri.YourXRIs;
import ibrokerkit.ibrokerfront.webpages.xri.renew.DoRenewXRI;
import ibrokerkit.ibrokerfront.webpages.xri.transfer.DoTransferXRIIn;
import ibrokerkit.ibrokerfront.webpages.xri.transfer.TransferXRI;
import ibrokerkit.iname4java.store.impl.grs.GrsXriStore;
import ibrokerkit.iname4java.store.impl.openxri.OpenxriXriStore;

import java.util.Properties;

import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.request.IRequestCycleProcessor;
import org.apache.wicket.request.target.coding.QueryStringUrlCodingStrategy;
import org.apache.wicket.spring.SpringWebApplication;
import org.apache.wicket.util.lang.PackageName;
import org.openid4java.consumer.ConsumerManager;
import org.openxri.config.ServerConfig;
import org.openxri.factories.ServerConfigFactory;
import org.openxri.store.Store;

public class IbrokerApplication extends SpringWebApplication {

	protected Properties properties;
	protected EppTools eppTools;
	protected ibrokerkit.ibrokerstore.store.Store ibrokerStore;
	protected ibrokerkit.iservicestore.store.Store iserviceStore;
	protected ibrokerkit.iname4java.store.XriStore xriStore;
	protected ConsumerManager consumerManager;
	
	@Override
	public void init() {

		// init OpenXRI ServetConfig and xriStore

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

		this.mount(new QueryStringUrlCodingStrategy(this.properties.getProperty("payment-callback-path-register"), DoRegister.class));
		this.mount(new QueryStringUrlCodingStrategy(this.properties.getProperty("payment-callback-path-transferxriin"), DoTransferXRIIn.class));
		this.mount(new QueryStringUrlCodingStrategy(this.properties.getProperty("payment-callback-path-renewxri"), DoRenewXRI.class));
		this.mountBookmarkablePage("/TermsOfUse", TermsOfUse.class);
		this.mount("/user", PackageName.forClass(Login.class));
		this.mount("/xri", PackageName.forClass(YourXRIs.class));
		this.mount("/transfer", PackageName.forClass(TransferXRI.class));
		this.mount("/sorry", PackageName.forClass(AccessDenied.class));

		// set up various wicket parameters

		this.getApplicationSettings().setClassResolver(new IbrokerClassResolver());
		this.getApplicationSettings().setAccessDeniedPage(AccessDenied.class);
		this.getApplicationSettings().setInternalErrorPage(InternalError.class);
		this.getApplicationSettings().setPageExpiredErrorPage(PageExpired.class);
		this.getSecuritySettings().setAuthorizationStrategy(new IbrokerAuthorizationStrategy());
		this.getMarkupSettings().setStripXmlDeclarationFromOutput(false);
		this.getMarkupSettings().setDefaultMarkupEncoding("UTF-8");
		this.getPageSettings().setAutomaticMultiWindowSupport(false);

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

		return(Index.class);
	}

	@Override
	protected IRequestCycleProcessor newRequestCycleProcessor() {

		return(new IbrokerRequestCycleProcessor());
	}

	@Override
	public String getConfigurationType() {

		return("DEPLOYMENT");
	}

	@Override
	public Session newSession(Request request, Response response) {

		return(new IbrokerSession(request));
	}

	public Properties getProperties() {
		return (this.properties);
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public EppTools getEppTools() {
		return (this.eppTools);
	}

	public void setEppTools(EppTools eppTools) {
		this.eppTools = eppTools;
	}

	public ibrokerkit.ibrokerstore.store.Store getIbrokerStore() {
		return (this.ibrokerStore);
	}

	public void setIbrokerStore(ibrokerkit.ibrokerstore.store.Store ibrokerStore) {
		this.ibrokerStore = ibrokerStore;
	}

	public ibrokerkit.iservicestore.store.Store getIserviceStore() {
		return (this.iserviceStore);
	}

	public void setIserviceStore(ibrokerkit.iservicestore.store.Store iserviceStore) {
		this.iserviceStore = iserviceStore;
	}

	public ibrokerkit.iname4java.store.XriStore getXriStore() {
		return (this.xriStore);
	}

	public void setXriStore(ibrokerkit.iname4java.store.XriStore xriStore) {
		this.xriStore = xriStore;
	}

	public ConsumerManager getConsumerManager() {
		return (this.consumerManager);
	}

	public void setConsumerManager(ConsumerManager consumerManager) {
		this.consumerManager = consumerManager;
	}
}
