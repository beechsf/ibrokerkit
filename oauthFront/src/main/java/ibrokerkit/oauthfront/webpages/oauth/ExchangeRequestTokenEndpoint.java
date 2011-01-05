package ibrokerkit.oauthfront.webpages.oauth;

import ibrokerkit.oauthfront.oauth.OauthSupport;
import ibrokerkit.oauthfront.webapplication.OauthApplication;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
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

public class ExchangeRequestTokenEndpoint implements IRequestTarget {

	private static Log log = LogFactory.getLog(ExchangeRequestTokenEndpoint.class.getName());

	private Map<String, String> parameters;

	public ExchangeRequestTokenEndpoint(Map<String, String> parameters) {

		this.parameters = parameters;
	}

	public void respond(RequestCycle requestCycle) {

		Properties properties = ((OauthApplication) Application.get()).getProperties();

		try {

			// read and validate the OAuth message

			String url = properties.getProperty("exchangerequesttoken-endpoint-url");
			OAuthMessage requestMessage = OAuthServlet.getMessage(((WebRequest) requestCycle.getRequest()).getHttpServletRequest(), url);
			OAuthAccessor oAuthAccessor = OauthSupport.getAccessor(requestMessage);

			log.info("ExchangeRequestTokenEndpoint: Got oAuth message: " + requestMessage.toString());

			OauthSupport.getValidator().validateMessage(requestMessage, oAuthAccessor);

			// make sure token is authorized

			if (! Boolean.TRUE.equals(oAuthAccessor.getProperty("authorized"))) {
				
				throw new OAuthProblemException("permission_denied");
			}

			// generate access token and secret
			
			OauthSupport.generateAccessToken(oAuthAccessor);

			log.info("Generated oAuth token secret: " + oAuthAccessor.tokenSecret);

			List<Parameter> parameters = OAuth.newList(
					"oauth_token", oAuthAccessor.accessToken,
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
