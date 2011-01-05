package ibrokerkit.xdifront;

import ibrokerkit.epptools4java.EppTools;
import ibrokerkit.iname4java.store.impl.grs.GrsXriStore;
import ibrokerkit.iname4java.store.impl.openxri.OpenxriXriStore;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.eclipse.higgins.xdi4j.messaging.server.EndpointServlet;
import org.openxri.config.ServerConfig;
import org.openxri.factories.ServerConfigFactory;
import org.openxri.store.Store;

public class DynamicEndpointServlet extends EndpointServlet {

	private static final long serialVersionUID = -2813658193208774976L;

	private static DynamicEndpointServlet instance;

	private String contextName;
	private Properties properties;
	private EppTools eppTools;
	private ibrokerkit.iname4java.store.XriStore xriStore;
	private ibrokerkit.ibrokerstore.store.Store ibrokerStore;
	private ibrokerkit.iservicestore.store.Store iserviceStore;
	private X509Certificate brokerCertificate;
	private PrivateKey brokerPrivateKey;
	private KeyPairGenerator keyPairGenerator;
	private X509V3CertificateGenerator certificateGenerator;

	public static DynamicEndpointServlet getInstance() {

		return(instance);
	}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {

		super.init(servletConfig);

		instance = this;

		// remember context name for BDB path

		this.contextName = servletConfig.getServletContext().getServletContextName();

		// load properties

		try {

			this.properties = new Properties();
			String propertiesFile = servletConfig.getServletContext().getRealPath("WEB-INF/application.properties");
			this.properties.load(new FileReader(propertiesFile));
		} catch (Exception ex) {

			throw new ServletException(ex);
		}

		// load eppTools

		try {

			Properties eppToolsProperties = new Properties();
			String eppToolsPropertiesFile = servletConfig.getServletContext().getRealPath("WEB-INF/epptools.properties");

			if (new File(eppToolsPropertiesFile).exists()) {

				eppToolsProperties.load(new FileReader(eppToolsPropertiesFile));

				this.eppTools = new EppTools(eppToolsProperties);
				this.eppTools.init();
			} else {

				this.eppTools = null;
			}
		} catch (Exception ex) {

			throw new ServletException(ex);
		}

		// init OpenXRI ServletConfig and xriStore

		try {

			ServerConfig openxriServerConfig = ServerConfigFactory.initSingleton(servletConfig);
			if (this.eppTools != null)
				this.xriStore = new GrsXriStore(((Store) openxriServerConfig.getComponentRegistry().getComponent(Store.class)), this.eppTools);
			else
				this.xriStore = new OpenxriXriStore(((Store) openxriServerConfig.getComponentRegistry().getComponent(Store.class)));
		} catch (Exception ex) {

			throw new ServletException(ex);
		}

		// load ibrokerStore

		try {

			Properties ibrokerStoreProperties = new Properties();
			String ibrokerStorePropertiesFile = servletConfig.getServletContext().getRealPath("WEB-INF/ibrokerstore.properties");
			ibrokerStoreProperties.load(new FileReader(ibrokerStorePropertiesFile));

			this.ibrokerStore = new ibrokerkit.ibrokerstore.store.impl.db.DatabaseStore(ibrokerStoreProperties);
			this.ibrokerStore.init();
		} catch (Exception ex) {

			throw new ServletException(ex);
		}

		// load iserviceStore

		try {

			Properties iserviceStoreProperties = new Properties();
			String iserviceStorePropertiesFile = servletConfig.getServletContext().getRealPath("WEB-INF/iservicestore.properties");
			iserviceStoreProperties.load(new FileReader(iserviceStorePropertiesFile));

			this.iserviceStore = new ibrokerkit.iservicestore.store.impl.db.DatabaseStore(iserviceStoreProperties);
			this.iserviceStore.init();
		} catch (Exception ex) {

			throw new ServletException(ex);
		}

		// load keys, token generator and token verifier

		try {

			Security.addProvider(new BouncyCastleProvider());
			KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BC");
			CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509", "BC");

			InputStream publicBrokerCertificateInputStream = new ByteArrayInputStream(Base64.decodeBase64(this.properties.getProperty("broker-certificate").getBytes("UTF-8"))); 
			this.brokerCertificate = (X509Certificate) certificateFactory.generateCertificate(publicBrokerCertificateInputStream);

			KeySpec publicBrokerPrivateKeySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(this.properties.getProperty("broker-private-key").getBytes("UTF-8")));
			this.brokerPrivateKey = keyFactory.generatePrivate(publicBrokerPrivateKeySpec);

			this.keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
			this.keyPairGenerator.initialize(2048);

			this.certificateGenerator = new X509V3CertificateGenerator();
		} catch (Exception ex) {

			throw new ServletException(ex);
		}
	}

	public String getContextName() {

		return(this.contextName);
	}

	public Properties getProperties() {

		return this.properties;
	}

	public ibrokerkit.iname4java.store.XriStore getXriStore() {

		return this.xriStore;
	}

	public ibrokerkit.ibrokerstore.store.Store getIbrokerStore() {

		return this.ibrokerStore;
	}

	public ibrokerkit.iservicestore.store.Store getIServiceStore() {

		return this.iserviceStore;
	}

	public X509Certificate getBrokerCertificate() {

		return this.brokerCertificate;
	}

	public PrivateKey getBrokerPrivateKey() {

		return this.brokerPrivateKey;
	}

	public KeyPairGenerator getKeyPairGenerator() {
		
		return this.keyPairGenerator;
	}

	public X509V3CertificateGenerator getCertificateGenerator() {

		return this.certificateGenerator;
	}
}
