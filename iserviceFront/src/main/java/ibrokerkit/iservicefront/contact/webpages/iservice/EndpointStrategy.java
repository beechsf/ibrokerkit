package ibrokerkit.iservicefront.contact.webpages.iservice;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.IRequestTarget;
import org.apache.wicket.request.RequestParameters;
import org.apache.wicket.request.target.coding.IRequestTargetUrlCodingStrategy;
import org.openxri.IRIUtils;
import org.openxri.XRI;


public class EndpointStrategy implements IRequestTargetUrlCodingStrategy {

	private static Log log = LogFactory.getLog(EndpointStrategy.class.getName());

	private String mountPath;

	public EndpointStrategy(String mountPath) {

		this.mountPath = mountPath;
	}

	public IRequestTarget decode(RequestParameters requestParameters) {

		String endpointPath = this.getMountPath();
		String query = requestParameters.getPath().substring(endpointPath.length());

		if (query.startsWith("/")) query = query.substring(1);

		// construct an XRI from the query

		XRI qxri;

		try {

			qxri = new XRI(IRIUtils.IRItoXRI(IRIUtils.URItoIRI(query)));
		} catch(Exception ex) {

			log.error("Got request for invalid XRI: " + query, ex);
			return(null);
		}

		log.info("Got request for XRI: " + qxri.toString());

		// return an endpoint that can answer the request

		return(new Endpoint(qxri));
	}

	public CharSequence encode(IRequestTarget requestTarget) {

		// construct an URI from an endpoint

		if (! (requestTarget instanceof Endpoint)) return(null);

		String endpointPath = this.getMountPath();
		XRI qxri = ((Endpoint) requestTarget).getQxri();

		return(endpointPath + "/" + qxri.toString());
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
