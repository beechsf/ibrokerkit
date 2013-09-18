package ibrokerkit.iservicefront;

import ibrokerkit.iservicefront.authentication.webpages.error.ExceptionPage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.authorization.UnauthorizedInstantiationException;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.protocol.http.request.urlcompressing.UrlCompressingWebRequestProcessor;

public class IserviceRequestCycleProcessor extends UrlCompressingWebRequestProcessor {

	private static Log log = LogFactory.getLog(IserviceRequestCycleProcessor.class.getName());

	@Override
	public void respond(RuntimeException ex, RequestCycle requestCycle) {

		// let wicket handle its own stuff

		if (ex instanceof UnauthorizedInstantiationException ||
				ex instanceof PageExpiredException) {

			super.respond(ex, requestCycle);
			return;
		}

		// log and display the exception using our exception page

		log.error("Internal Error", ex);

		PageParameters parameters = new PageParameters();
		parameters.put("request", ex);
		parameters.put("ex", ex);

		ExceptionPage page = new ExceptionPage(requestCycle, ex);

		requestCycle.setResponsePage(page);
	}
}
