package ibrokerkit.ibrokertask;

import ibrokerkit.epptools4java.EppTools;
import ibrokerkit.ibrokerstore.store.impl.db.DatabaseStore;
import ibrokerkit.ibrokertask.jobs.CheckExpirationJob;
import ibrokerkit.ibrokertask.jobs.PollJob;
import ibrokerkit.iname4java.store.impl.grs.GrsXriStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.velocity.app.Velocity;
import org.openxri.config.ServerConfig;
import org.openxri.config.impl.XMLServerConfig;
import org.openxri.factories.ServerConfigFactory;
import org.openxri.store.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IbrokerTask {

	private static final Logger log = LoggerFactory.getLogger(IbrokerTask.class.getName());

	public static Properties properties;
	public static EppTools eppTools;
	public static ibrokerkit.ibrokerstore.store.Store ibrokerStore;
	public static ibrokerkit.iname4java.store.XriStore xriStore;

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
		ibrokerStore = new DatabaseStore(ibrokerStoreProperties);
		ibrokerStore.init();

		// init OpenXRI ServetConfig and xriStore

		Properties openxriStoreProperties = new Properties();
		openxriStoreProperties.setProperty(XMLServerConfig.SERVER_CONFIG_FILE, "server.xml");
		ServerConfig openxriServerConfig = ServerConfigFactory.initSingleton(null, openxriStoreProperties);
		xriStore = new GrsXriStore(((Store) openxriServerConfig.getComponentRegistry().getComponent(Store.class)), eppTools);
	}

	private static void shutdown() throws Exception {

		ibrokerStore.close();
		eppTools.close();
	}

	public static void main(String[] args) throws IOException {

		// read options

		Options options = new Options(args);
		log.info("DO check expiration: " + Boolean.toString(options.isDoCheckExpiration()));
		log.info("DO poll: " + Boolean.toString(options.isDoPoll()));
		log.info("ACT check expiration: " + Boolean.toString(options.isActCheckExpiration()));
		log.info("ACT poll: " + Boolean.toString(options.isActPoll()));

		// initialize everything

		try {

			log.info("----- START INITIALIZING");
			init();
			log.info("----- DONE INITIALIZING");
		} catch (Exception ex) {

			log.error("Initialization", ex);
			return;
		}

		// check for expiration and check for polls

		if (options.isDoCheckExpiration()) {

			try {

				log.info("----- CHECKING EXPIRATIONS");
				CheckExpirationJob.run(options.isActCheckExpiration());
			} catch (Exception ex) {

				log.error("CheckExpirationTask", ex);
				return;
			}
		}

		if (options.isDoPoll()) {

			try {

				log.info("----- POLLING");
				PollJob.run(options.isActPoll());
			} catch (Exception ex) {

				log.error("PollTask", ex);
				return;
			}
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
