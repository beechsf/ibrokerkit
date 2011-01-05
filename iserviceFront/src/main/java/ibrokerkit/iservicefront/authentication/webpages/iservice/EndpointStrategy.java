package ibrokerkit.iservicefront.authentication.webpages.iservice;

import org.apache.wicket.IRequestTarget;
import org.apache.wicket.request.RequestParameters;
import org.apache.wicket.request.target.coding.IRequestTargetUrlCodingStrategy;

public class EndpointStrategy implements IRequestTargetUrlCodingStrategy {
	
	private String mountPath;
	
	public EndpointStrategy(String mountPath) {
		
		this.mountPath = mountPath;
	}

	public IRequestTarget decode(RequestParameters requestParameters) {

		// return an endpoint that can answer the request

		return(new Endpoint());
	}

	public CharSequence encode(IRequestTarget requestTarget) {

		// construct a URI from an endpoint

		if (! (requestTarget instanceof Endpoint)) return(null);

		String endpointPath = this.getMountPath();

		return(endpointPath);
	}

	public String getMountPath() {

		return(this.mountPath);
	}

	public boolean matches(IRequestTarget requestTarget) {

		return(requestTarget instanceof Endpoint);
	}

	public boolean matches(String path) {

		return(path.indexOf(this.getMountPath()) == 0);
	}
}
