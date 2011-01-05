package ibrokerkit.oauthfront.webpages.oauth;

import ibrokerkit.oauthfront.oauth.OauthSupport;
import ibrokerkit.oauthfront.webapplication.OauthApplication;

import java.util.Map;
import java.util.Properties;

import net.oauth.OAuthAccessor;
import net.oauth.OAuthMessage;
import net.oauth.server.OAuthServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Application;
import org.apache.wicket.IRequestTarget;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;

public class AuthorizeRequestTokenEndpoint implements IRequestTarget {

	private static Log log = LogFactory.getLog(AuthorizeRequestTokenEndpoint.class.getName());

	private Map<String, String> parameters;

	public AuthorizeRequestTokenEndpoint(Map<String, String> parameters) {

		this.parameters = parameters;
	}

	public void respond(RequestCycle requestCycle) {

		Properties properties = ((OauthApplication) Application.get()).getProperties();

		try {

			// read the OAuth message

			String url = properties.getProperty("authorizerequesttoken-endpoint-url");
			OAuthMessage requestMessage = OAuthServlet.getMessage(((WebRequest) requestCycle.getRequest()).getHttpServletRequest(), url);
			OAuthAccessor oAuthAccessor = OauthSupport.getAccessor(requestMessage);

			log.info("AuthorizeRequestTokenEndpoint: Got oAuth message: " + requestMessage.toString());

			// check which operation is being performed

			String xriOperation = (String) oAuthAccessor.getProperty(OauthSupport.PROPERTY_XRI_OPERATION);

			// display oauth page

			if (xriOperation.equals(OauthSupport.XRI_OPERATION_ADD_SERVICE)) {

				requestCycle.setResponsePage(new OauthAddServicePage(oAuthAccessor, requestMessage));
			}
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
