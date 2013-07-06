package ibrokerkit.ibrokermaintenance;

import ibrokerkit.epptools4java.EppTools;
import ibrokerkit.ibrokermaintenance.jobs.CheckAuthenticationPasswordsJob;
import ibrokerkit.ibrokermaintenance.jobs.Job;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.app.Velocity;
import org.openxri.config.ServerConfig;
import org.openxri.config.impl.XMLServerConfig;
import org.openxri.factories.ServerConfigFactory;
import org.openxri.store.Store;

public class IbrokerMaintenance {

	private static final Log log = LogFactory.getLog(IbrokerMaintenance.class.getName());

	public static Properties properties;
	public static EppTools eppTools;
	public static ibrokerkit.ibrokerstore.store.Store ibrokerStore;
	public static org.openxri.store.Store openxriStore;
	public static ibrokerkit.iname4java.store.XriStore xriStore;
	public static ibrokerkit.iservicestore.store.Store iserviceStore;

	//	public static Job[] jobs = new Job[] { new CheckGrsAuthorityJob(true), new CheckDatesJob(true), new CheckEmailsJob(true), new CheckXRDJob(true) };
	//	public static Job[] jobs = new Job[] { new DeleteInameJob() };
	public static Job[] jobs = new Job[] { new CheckAuthenticationPasswordsJob(true) };

	private static void init() throws Exception {

		// init properties

		properties = new Properties();
		properties.load(new FileInputStream(new File("application.properties")));

		// init Velocity

		Velocity.init();

		// init eppTools

		Properties eppToolsProperties = new Properties();
		eppToolsProperties.load(new FileInputStream(new File("epptools.properties")));
		eppTools = new EppTools(eppToolsProperties);
		eppTools.init();

		// init ibrokerStore

		Properties ibrokerStoreProperties = new Properties();
		ibrokerStoreProperties.load(new FileInputStream(new File("ibrokerstore.properties")));
		ibrokerStore = new ibrokerkit.ibrokerstore.store.impl.db.DatabaseStore(ibrokerStoreProperties);
		ibrokerStore.init();

		// init OpenXRI ServetConfig and OpenXRI store and xriStore

		Properties openxriStoreProperties = new Properties();
		openxriStoreProperties.setProperty(XMLServerConfig.SERVER_CONFIG_FILE, "server.xml");
		ServerConfig openxriServerConfig = ServerConfigFactory.initSingleton(null, openxriStoreProperties);
		openxriStore = ((Store) openxriServerConfig.getComponentRegistry().getComponent(Store.class));
		xriStore = new ibrokerkit.iname4java.store.impl.grs.GrsXriStore(openxriStore, eppTools);

		// init iserviceStore

		Properties iserviceStoreProperties = new Properties();
		iserviceStoreProperties.load(new FileInputStream(new File("iservicestore.properties")));
		iserviceStore = new ibrokerkit.iservicestore.store.impl.db.DatabaseStore(iserviceStoreProperties);
		iserviceStore.init();
	}

	private static void shutdown() throws Exception {

		ibrokerStore.close();
		eppTools.close();
		openxriStore.close();
		iserviceStore.close();
	}

	public static void main(String[] args) throws IOException {

		// initialize everything

		try {

			log.info("----- START INITIALIZING");
			init();
			log.info("----- DONE INITIALIZING");
		} catch (Exception ex) {

			log.error("Initialization", ex);
			return;
		}

		// job

		try {

			for (Job job : jobs) {

				log.info("----- START JOB " + job.getClass().getSimpleName());
				job.args(args);
				job.run();
				log.info("----- DONE JOB");
			}
		} catch (Exception ex) {

			log.error("Job", ex);
			return;
		}

		// shutdown everything

		try {

			log.info("----- START SHUTDOWN");
			shutdown();
			log.info("----- DONE SHUTDOWN");
		} catch (Exception ex) {

			log.error("Shutdown", ex);
			return;
		}
	}
}
