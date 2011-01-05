package ibrokerkit.oauthfront.webpages.oauth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.IRequestTarget;
import org.apache.wicket.request.RequestParameters;
import org.apache.wicket.request.target.coding.IRequestTargetUrlCodingStrategy;

public class ExchangeRequestTokenEndpointStrategy implements IRequestTargetUrlCodingStrategy {

	private static Log log = LogFactory.getLog(ExchangeRequestTokenEndpointStrategy.class.getName());

	private String mountPath;

	public ExchangeRequestTokenEndpointStrategy(String mountPath) {

		this.mountPath = mountPath;
	}

	@SuppressWarnings("unchecked")
	public IRequestTarget decode(RequestParameters requestParameters) {

		log.info("Got request with parameters: " + requestParameters.toString());

		// return an endpoint that can answer the request

		return(new ExchangeRequestTokenEndpoint(requestParameters.getParameters()));
	}

	public CharSequence encode(IRequestTarget requestTarget) {

		return(null);
	}

	public String getMountPath() {

		return(this.mountPath);
	}

	public boolean matches(IRequestTarget requestTarget) {

		return(requestTarget instanceof ObtainRequestTokenEndpoint);
	}

	public boolean matches(String path) {

		return(path.indexOf(this.getMountPath()) == 0);
	}
}
