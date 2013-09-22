package ibrokerkit.iservicefront;

import ibrokerkit.iservicefront.iservice.webpages.error.ExceptionPage;

import org.apache.wicket.authorization.UnauthorizedInstantiationException;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IserviceRequestCycleListener extends AbstractRequestCycleListener implements IRequestCycleListener {

	private final static Logger log = LoggerFactory.getLogger(IserviceRequestCycleListener.class.getName());

	@Override
	public IRequestHandler onException(RequestCycle requestCycle, Exception ex) {
		
		// let wicket handle its own stuff

		if (ex instanceof UnauthorizedInstantiationException || ex instanceof PageExpiredException) {

			return super.onException(requestCycle, ex);
		}

		// log and display the exception using our exception page

		log.error("Internal Error", ex);

		PageParameters parameters = new PageParameters();
		parameters.add("request", ex);
		parameters.add("ex", ex);

		ExceptionPage page = new ExceptionPage(requestCycle, ex);

		return new RenderPageRequestHandler(new PageProvider(page));
	}
}
