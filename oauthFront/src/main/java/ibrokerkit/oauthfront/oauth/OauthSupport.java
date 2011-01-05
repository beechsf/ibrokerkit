package ibrokerkit.oauthfront.oauth;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import net.oauth.OAuthValidator;
import net.oauth.SimpleOAuthValidator;
import net.oauth.server.OAuthServlet;

import org.apache.commons.codec.digest.DigestUtils;
import org.openxri.XRI;
import org.openxri.xml.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class OauthSupport {

	// anyone can make oAuth requests via the "public consumer" key and secret

	public static final String PUBLIC_CONSUMER_KEY = "PUBLIC";
	public static final String PUBLIC_CONSUMER_SECRET = "";
	public static final String PUBLIC_CONSUMER_DESCRIPTION = "Public Consumer";

	// these oAuth parameters are used for the consumer to specify what it wants

	public static final String PARAMETER_XRI_OPERATION = "xri_operation";
	public static final String PARAMETER_XRI_INAME = "xri_iname";
	public static final String PARAMETER_XRI_SERVICE = "xri_service";

	public static final String XRI_OPERATION_ADD_SERVICE = "xri_add_service";

	// these properties store state during the oAuth steps and are used when executing the access token

	public static final String PROPERTY_XRI_OPERATION = "xri_operation";
	public static final String PROPERTY_XRI_INAME = "xri_iname";
	public static final String PROPERTY_XRI_SERVICE = "xri_service";
	public static final String PROPERTY_XRI = "xri_xri";
	public static final String PROPERTY_USER = "xri_user";
	public static final String PROPERTY_USERINPUT = "xri_userinput";

	private static OAuthConsumer publicConsumer;
	private static OAuthValidator validator;
	private static Set<OAuthAccessor> tokens;

	static {

		publicConsumer = new OAuthConsumer(
				null,
				PUBLIC_CONSUMER_KEY,
				PUBLIC_CONSUMER_SECRET,
				null);
		publicConsumer.setProperty("name", PUBLIC_CONSUMER_KEY);
		publicConsumer.setProperty("description", PUBLIC_CONSUMER_DESCRIPTION);

		validator = new SimpleOAuthValidator();

		tokens = new HashSet<OAuthAccessor>();
	}

	private OauthSupport() {

	}

	public static synchronized OAuthConsumer getConsumer(OAuthMessage requestMessage) throws IOException, OAuthProblemException {

		if (requestMessage.getConsumerKey().equals(PUBLIC_CONSUMER_KEY)) {

			return(publicConsumer);
		} else {

			throw new OAuthProblemException("consumer_key_unknown");
		}
	}

	public static synchronized OAuthAccessor createAccessor(OAuthConsumer oAuthConsumer) {

		return(new OAuthAccessor(oAuthConsumer));
	}

	public static synchronized OAuthValidator getValidator() {

		return(validator);
	}

	public static synchronized OAuthAccessor getAccessor(OAuthMessage requestMessage) throws IOException, OAuthProblemException {

		// try to load from local cache if not throw exception
		String consumer_token = requestMessage.getToken();
		OAuthAccessor accessor = null;
		for (OAuthAccessor a : tokens) {
			if(a.requestToken != null) {
				if (a.requestToken.equals(consumer_token)) {
					accessor = a;
					break;
				}
			} else if(a.accessToken != null){
				if (a.accessToken.equals(consumer_token)) {
					accessor = a;
					break;
				}
			}
		}

		if(accessor == null){
			OAuthProblemException problem = new OAuthProblemException("token_expired");
			throw problem;
		}

		return accessor;
	}

	public static synchronized void invalidateAccessor(OAuthAccessor oAuthAccessor) {

		tokens.remove(oAuthAccessor);
	}

	public static synchronized String extractXriOperation(OAuthMessage message) throws OAuthException, IOException {

		String xriOperation = message.getParameter(PARAMETER_XRI_OPERATION);

		if (xriOperation == null) {

			OAuthProblemException ex = new OAuthProblemException("parameter_absent");
			ex.setParameter("oauth_parameters_absent", "xri_operation");
			throw ex;
		}

		if (! xriOperation.equals(XRI_OPERATION_ADD_SERVICE)) {

			OAuthProblemException ex2 = new OAuthProblemException("parameter_rejected");
			ex2.setParameter("oauth_parameters_rejected", "xri_operation");
			ex2.setParameter("oauth_problem_advice", "This XRI operation is not valid: " + xriOperation);
			throw ex2;
		}

		return(xriOperation);
	}

	public static synchronized String extractXriIname(OAuthMessage message) throws OAuthException, IOException {

		String xriIname = message.getParameter(PARAMETER_XRI_INAME);

		if (xriIname == null) {

			OAuthProblemException ex = new OAuthProblemException("parameter_absent");
			ex.setParameter("oauth_parameters_absent", "xri_iname");
			throw ex;
		}

		try {

			new XRI(xriIname);
		} catch (Exception ex) {

			OAuthProblemException ex2 = new OAuthProblemException("parameter_rejected");
			ex2.setParameter("oauth_parameters_rejected", "xri_iname");
			ex2.setParameter("oauth_problem_advice", xriIname + " is not a valid i-name: " + ex.getMessage());
			throw ex2;
		}

		return(xriIname);
	}

	public static synchronized Service extractXriService(OAuthMessage message) throws OAuthException, IOException {

		String xriService = message.getParameter(PARAMETER_XRI_SERVICE);

		if (xriService == null) {

			OAuthProblemException ex = new OAuthProblemException("parameter_absent");
			ex.setParameter("oauth_parameters_absent", "xri_service");
			throw ex;
		}

		// parse service

		Service service;

		try {

			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			ByteArrayInputStream stream = new ByteArrayInputStream(xriService.getBytes());
			Document document = builder.parse(stream);
			Element element = document.getDocumentElement();

			service = new Service(element);
		} catch (Exception ex) {

			OAuthProblemException ex2 = new OAuthProblemException("parameter_rejected");
			ex2.setParameter("oauth_parameters_rejected", "xri_service");
			ex2.setParameter("oauth_problem_advice", "The service endpoint could not be parsed: " + ex.getMessage());
			throw ex2;
		}

		return(service);
	}

	public static synchronized void markAsAuthorized(OAuthAccessor oAuthAccessor) throws OAuthException {

		// first remove the accessor from cache
		tokens.remove(oAuthAccessor);

		oAuthAccessor.setProperty("authorized", Boolean.TRUE);

		// update token in local cache
		tokens.add(oAuthAccessor);
	}

	public static synchronized void generateRequestToken(OAuthAccessor accessor) throws OAuthException {

		// generate oauth_token and oauth_secret
		String consumer_key = (String) accessor.consumer.getProperty("name");
		// generate token and secret based on consumer_key

		// for now use md5 of name + current time as token
		String token_data = consumer_key + System.nanoTime();
		String token = DigestUtils.md5Hex(token_data);
		// for now use md5 of name + current time + token as secret
		String secret_data = consumer_key + System.nanoTime() + token;
		String secret = DigestUtils.md5Hex(secret_data);

		accessor.requestToken = token;
		accessor.tokenSecret = secret;
		accessor.accessToken = null;

		// add to the local cache
		tokens.add(accessor);
	}

	public static synchronized void generateAccessToken(OAuthAccessor accessor) throws OAuthException {

		// generate oauth_token and oauth_secret
		String consumer_key = (String) accessor.consumer.getProperty("name");
		// generate token and secret based on consumer_key

		// for now use md5 of name + current time as token
		String token_data = consumer_key + System.nanoTime();
		String token = DigestUtils.md5Hex(token_data);
		// first remove the accessor from cache
		tokens.remove(accessor);

		accessor.requestToken = null;
		accessor.accessToken = token;

		// update token in local cache
		tokens.add(accessor);
	}

	public static synchronized void handleException(Exception e, HttpServletRequest request, HttpServletResponse response, boolean sendBody) throws IOException, ServletException {

		String realm = (request.isSecure())?"https://":"http://";
		realm += request.getLocalName();
		OAuthServlet.handleException(response, e, realm, sendBody); 
	}
}