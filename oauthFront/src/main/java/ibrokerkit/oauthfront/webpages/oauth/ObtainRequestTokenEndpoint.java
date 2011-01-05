package ibrokerkit.oauthfront.webpages.oauth;

import ibrokerkit.ibrokerstore.store.Store;
import ibrokerkit.ibrokerstore.store.User;
import ibrokerkit.iname4java.store.Xri;
import ibrokerkit.iname4java.store.XriStore;
import ibrokerkit.oauthfront.oauth.OauthSupport;
import ibrokerkit.oauthfront.webapplication.OauthApplication;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import net.oauth.OAuth.Parameter;
import net.oauth.server.OAuthServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Application;
import org.apache.wicket.IRequestTarget;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;
import org.openxri.xml.Service;

public class ObtainRequestTokenEndpoint implements IRequestTarget {

	private static Log log = LogFactory.getLog(ObtainRequestTokenEndpoint.class.getName());

	private Map<String, String> parameters;

	public ObtainRequestTokenEndpoint(Map<String, String> parameters) {

		this.parameters = parameters;
	}

	public void respond(RequestCycle requestCycle) {

		Properties properties = ((OauthApplication) Application.get()).getProperties();
		XriStore xriStore = ((OauthApplication) Application.get()).getXriStore();
		Store ibrokerStore = ((OauthApplication) Application.get()).getIbrokerStore();

		try {

			// read and validate the OAuth message

			String url = properties.getProperty("obtainrequesttoken-endpoint-url");
			OAuthMessage requestMessage = OAuthServlet.getMessage(((WebRequest) requestCycle.getRequest()).getHttpServletRequest(), url);
			OAuthConsumer oAuthConsumer = OauthSupport.getConsumer(requestMessage);
			OAuthAccessor oAuthAccessor = OauthSupport.createAccessor(oAuthConsumer);

			log.info("ObtainRequestTokenEndpoint: Got oAuth message from consumer " + oAuthConsumer.consumerKey + ": " + requestMessage.toString());

			OauthSupport.getValidator().validateMessage(requestMessage, oAuthAccessor);

			// Support the 'Variable Accessor Secret' extension
			// described in http://oauth.pbwiki.com/AccessorSecret

			String secret = requestMessage.getParameter("oauth_accessor_secret");
			if (secret != null) oAuthAccessor.setProperty(OAuthConsumer.ACCESSOR_SECRET, secret);

			// remember the xri_operation and xri_iname and xri_service of this request

			String xriOperation = OauthSupport.extractXriOperation(requestMessage);
			String xriIname = OauthSupport.extractXriIname(requestMessage);
			Service xriService = OauthSupport.extractXriService(requestMessage);

			oAuthAccessor.setProperty(OauthSupport.PROPERTY_XRI_OPERATION, xriOperation);
			oAuthAccessor.setProperty(OauthSupport.PROPERTY_XRI_INAME, xriIname);
			oAuthAccessor.setProperty(OauthSupport.PROPERTY_XRI_SERVICE, xriService);

			// find and remember xri and user of this request

			Xri xri = null;
			User user = null;

			xri = xriStore.findXri(xriIname);
			if (xri != null) user = ibrokerStore.findUser(xri.getUserIdentifier());

			if (xri == null || user == null) throw new OAuthProblemException("permission_denied");

			oAuthAccessor.setProperty(OauthSupport.PROPERTY_XRI, xri);
			oAuthAccessor.setProperty(OauthSupport.PROPERTY_USER, user);

			// generate request_token and secret

			OauthSupport.generateRequestToken(oAuthAccessor);

			log.info("Generated oAuth request token: " + oAuthAccessor.requestToken);
			log.info("Generated oAuth token secret: " + oAuthAccessor.tokenSecret);

			List<Parameter> parameters = OAuth.newList(
					"oauth_token", oAuthAccessor.requestToken,
					"oauth_token_secret", oAuthAccessor.tokenSecret);

			for (Parameter parameter : parameters) log.info("Sending parameter: " + parameter.getKey() + " -> " + parameter.getValue());

			requestCycle.getResponse().setContentType("text/plain");
			OAuth.formEncode(parameters, requestCycle.getResponse().getOutputStream());
			requestCycle.getResponse().close();
		} catch (Exception ex) {

			log.error(ex.getMessage(), ex);
			
			try {

				OauthSupport.handleException(ex, ((WebRequest) requestCycle.getRequest()).getHttpServletRequest(), ((WebResponse) requestCycle.getResponse()).getHttpServletResponse(), true);
			} catch (Exception ex2) {

				throw new RuntimeException(ex2);
			}
		}
	}

	public Map<String, String> getParameters() {
		return (this.parameters);
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	public void detach(RequestCycle requestCycle) {

	}
}
