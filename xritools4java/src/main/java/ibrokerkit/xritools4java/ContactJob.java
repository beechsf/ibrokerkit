package ibrokerkit.xritools4java;

import java.util.List;

import org.openxri.XRI;
import org.openxri.resolve.Resolver;
import org.openxri.resolve.ResolverFlags;
import org.openxri.resolve.ResolverState;
import org.openxri.resolve.exception.IllegalTrustTypeException;
import org.openxri.resolve.exception.PartialResolutionException;
import org.openxri.xml.ContactService;

public class ContactJob extends Job {

	private String xri;
	
	public ContactJob(String xri) {

		this.xri = xri;

		if (this.xri.toLowerCase().startsWith("xri://")) this.xri = this.xri.substring(6);
	}

	@Override
	public void execute(Resolver resolver) throws PartialResolutionException, IllegalTrustTypeException {

		// init resolver flags, resolver state and resolver

		ResolverFlags flags = new ResolverFlags();
		
		ResolverState state = new ResolverState();
		
		resolver.setProxyURI(null);

		// do the ping
		
		List<?> uris = resolver.resolveSEPToURIList(
				new XRI(this.xri), 
				ContactService.SERVICE_TYPE, 
				null, 
				flags,
				state);

		// produce full result

		this.result = new StringBuffer();

		this.result.append("Contact Service endpoint(s) for " + this.xri + ":<br>");

		for (int i=0; i<uris.size(); i++) {

			String uri = (String) uris.get(i);
			this.result.append("<a target=\"_blank\" href=\"" + uri.toString() + "\">");
			this.result.append(uri.toString());
			this.result.append("</a><br>");
		}

		// produce one line result

		this.oneLineResult = new StringBuffer();

		this.oneLineResult.append("Contact Service endpoint(s) for " + this.xri + ": ");

		for (int i=0; i<uris.size(); i++) {

			if (i > 0) this.oneLineResult.append(", ");

			String uri = (String) uris.get(i);
			this.oneLineResult.append(uri.toString());
		}
		
		// produce stats
		
		this.stats = null;
	}

	@Override
	public String getJobName() {

		return("Contact Service");
	}
}
