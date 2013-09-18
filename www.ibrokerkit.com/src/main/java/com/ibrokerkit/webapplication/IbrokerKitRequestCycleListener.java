package com.ibrokerkit.webapplication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.authorization.UnauthorizedInstantiationException;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.ibrokerkit.webpages.error.ExceptionPage;

public class IbrokerKitRequestCycleListener extends AbstractRequestCycleListener implements IRequestCycleListener {

	private static Log log = LogFactory.getLog(IbrokerKitRequestCycleListener.class.getName());

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
