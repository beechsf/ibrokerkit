package ibrokerkit.xdifront.util;

import ibrokerkit.ibrokerstore.store.User;
import ibrokerkit.iname4java.store.Xri;
import ibrokerkit.iservicestore.store.Authentication;
import ibrokerkit.iservicestore.store.Contact;
import ibrokerkit.iservicestore.store.Forwarding;
import ibrokerkit.xdifront.DynamicEndpointServlet;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.openxri.xml.AuthenticationService;
import org.openxri.xml.CertificateService;
import org.openxri.xml.ContactService;
import org.openxri.xml.ForwardingService;
import org.openxri.xml.Service;
import org.openxri.xml.XDIService;

public class XriWizard {

	private XriWizard() { }

	public static void configure(Xri xri) throws Exception {

		Properties properties = DynamicEndpointServlet.getInstance().getProperties();
		ibrokerkit.ibrokerstore.store.Store ibrokerStore = DynamicEndpointServlet.getInstance().getIbrokerStore();
		ibrokerkit.iservicestore.store.Store iserviceStore = DynamicEndpointServlet.getInstance().getIServiceStore();

		xri.deleteAllServices();
		List<Service> services = new ArrayList<Service> ();

		User user = ibrokerStore.findUser(xri.getUserIdentifier());

		// Authentication

		Authentication authentication = iserviceStore.createAuthentication();
		authentication.setQxri(xri.getAuthorityId()); 
		authentication.setName("Default Authentication");
		authentication.setEnabled(Boolean.TRUE); 
		authentication.setIndx(user.getIdentifier()); 
		authentication.setPass(user.getPass());
		iserviceStore.updateObject(authentication);

		services.add(
				new AuthenticationService(
						new URI[] { new URI(properties.getProperty("authentication-service-https")) },
						properties.getProperty("providerid"),
						null,
						true));

		// Contact

		Contact contact = iserviceStore.createContact();
		contact.setQxri(xri.getAuthorityId()); 
		contact.setName("Default Contact");
		contact.setEnabled(Boolean.TRUE);
		contact.setIndx(user.getIdentifier()); 
		contact.setDescription("Contact page for " + user.getName()); 
		contact.setForward(user.getEmail());
		iserviceStore.updateObject(contact);

		services.add(
				new ContactService(
						new URI(properties.getProperty("contact-service")),
						properties.getProperty("providerid"),
						true));

		// Forwarding

		Map<String, String> mappings = new HashMap<String, String> ();

		Forwarding forwarding = iserviceStore.createForwarding();
		forwarding.setQxri(xri.getAuthorityId()); 
		forwarding.setName("Default Forwarding");
		forwarding.setEnabled(Boolean.TRUE); 
		forwarding.setIndx(user.getIdentifier()); 
		forwarding.setMappings(mappings); 
		forwarding.setIndexPage(Boolean.TRUE); 
		forwarding.setErrorPage(Boolean.TRUE);
		iserviceStore.updateObject(forwarding);

		services.add(
				new ForwardingService(
						new URI(properties.getProperty("forwarding-service")),
						properties.getProperty("providerid"),
						false,
						true));

		// set up XDI SEP

		services.add(
				new XDIService(
						new URI(properties.getProperty("xdi-service") + xri.getCanonicalID().getValue()),
						properties.getProperty("providerid")));

		// set up keys/certificate and SEP 

		X509Certificate userCertificate;

		if (! xri.hasAuthorityAttribute("publickey") ||
				! xri.hasAuthorityAttribute("privatekey") ||
				! xri.hasAuthorityAttribute("certificate")) {

			X509Certificate brokerCertificate = DynamicEndpointServlet.getInstance().getBrokerCertificate();
			PrivateKey brokerPrivateKey = DynamicEndpointServlet.getInstance().getBrokerPrivateKey();
			KeyPairGenerator keyPairGenerator = DynamicEndpointServlet.getInstance().getKeyPairGenerator();
			X509V3CertificateGenerator certificateGenerator = DynamicEndpointServlet.getInstance().getCertificateGenerator();

			KeyPair keyPair = keyPairGenerator.generateKeyPair();
			PublicKey userPublicKey = keyPair.getPublic();
			PrivateKey userPrivateKey = keyPair.getPrivate();
			Date userCertificateDate = new Date();

			certificateGenerator.setPublicKey(userPublicKey);
			certificateGenerator.setSubjectDN(new X509Name("cn=" + xri.getCanonicalID().getValue()));
			certificateGenerator.setIssuerDN(brokerCertificate.getIssuerX500Principal());
			certificateGenerator.setNotBefore(userCertificateDate);
			certificateGenerator.setNotAfter(new Date(userCertificateDate.getTime() + Long.parseLong(properties.getProperty("user-certificate-validity"))));
			certificateGenerator.setSerialNumber(BigInteger.valueOf(userCertificateDate.getTime()));
			certificateGenerator.setSignatureAlgorithm(properties.getProperty("user-certificate-signaturealgorithm"));

			userCertificate = certificateGenerator.generate(brokerPrivateKey);

			xri.setAuthorityAttribute("publickey", new String(Base64.encodeBase64(userPublicKey.getEncoded()), "UTF-8"));
			xri.setAuthorityAttribute("privatekey", new String(Base64.encodeBase64(userPrivateKey.getEncoded()), "UTF-8"));
			xri.setAuthorityAttribute("certificate", new String(Base64.encodeBase64(userCertificate.getEncoded()), "UTF-8"));
		} else {

			String userCertificateStr = xri.getAuthorityAttribute("certificate");

			CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509", "BC");
			InputStream userCertificateInputStream = new ByteArrayInputStream(Base64.decodeBase64(userCertificateStr.getBytes("UTF-8"))); 
			userCertificate = (X509Certificate) certificateFactory.generateCertificate(userCertificateInputStream);
		}

		services.add(
				new CertificateService(
						userCertificate));

		// add service endpoints
		
		xri.addServices(services.toArray(new Service[services.size()]));
	}
}
