package ibrokerkit.xdifront;

import ibrokerkit.ibrokerstore.store.User;
import ibrokerkit.iname4java.store.Xri;
import ibrokerkit.xdifront.messagingtargets.context.ContextResourceMessagingTarget;
import ibrokerkit.xdifront.messagingtargets.ibroker.IbrokerOperationMessagingTarget;
import ibrokerkit.xdifront.messagingtargets.ibroker.IbrokerResourceMessagingTarget;
import ibrokerkit.xdifront.messagingtargets.xri.XriResourceMessagingTarget;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.higgins.xdi4j.Graph;
import org.eclipse.higgins.xdi4j.impl.keyvalue.bdb.BDBGraphFactory;
import org.eclipse.higgins.xdi4j.messaging.server.EndpointRegistry;
import org.eclipse.higgins.xdi4j.messaging.server.MessagingTarget;
import org.eclipse.higgins.xdi4j.messaging.server.impl.CompoundMessagingTarget;
import org.eclipse.higgins.xdi4j.messaging.server.impl.graph.GraphMessagingTarget;
import org.eclipse.higgins.xdi4j.messaging.server.interceptor.impl.RoutingMessageInterceptor;
import org.eclipse.higgins.xdi4j.xri3.impl.XRI3Segment;

public class TargetFilter implements Filter {

	private static Log log = LogFactory.getLog(TargetFilter.class.getName());

	public void init(FilterConfig filterConfig) throws ServletException {

	}

	public void destroy() {

	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

		ibrokerkit.iname4java.store.XriStore xriStore = DynamicEndpointServlet.getInstance().getXriStore();
		ibrokerkit.ibrokerstore.store.Store ibrokerStore = DynamicEndpointServlet.getInstance().getIbrokerStore();

		String requestUri = ((HttpServletRequest) request).getRequestURI();
		String contextPath = ((HttpServletRequest) request).getContextPath(); 
		String path = requestUri.substring(contextPath.length() + 1);
		String target = path;
		if (target.indexOf("/") != -1) target = target.substring(0, path.indexOf("/"));
		if (! target.equals("")) target += "/";

		// check if we already have that target

		EndpointRegistry endpointRegistry = DynamicEndpointServlet.getInstance().getEndpointRegistry();

		MessagingTarget messagingTarget = endpointRegistry.getMessagingTarget(target);

		if (messagingTarget == null) {

			log.info("Creating messaging target for /" + target);

			try {

				if (target.equals("")) {

					// create CompoundMessagingTarget

					CompoundMessagingTarget compoundMessagingTarget = new CompoundMessagingTarget();
					compoundMessagingTarget.setMode(CompoundMessagingTarget.MODE_FIRST_HANDLED);

					// create and add IbrokerOperationMessagingTarget

					IbrokerOperationMessagingTarget ibrokerOperationMessagingTarget = new IbrokerOperationMessagingTarget();
					ibrokerOperationMessagingTarget.init(endpointRegistry);

					compoundMessagingTarget.getMessagingTargets().add(ibrokerOperationMessagingTarget);

					// create and add IbrokerOperationMessagingTarget

					IbrokerResourceMessagingTarget ibrokerResourceMessagingTarget = new IbrokerResourceMessagingTarget();
					ibrokerResourceMessagingTarget.init(endpointRegistry);

					compoundMessagingTarget.getMessagingTargets().add(ibrokerResourceMessagingTarget);

					// finish and register CompoundMessagingTarget

					compoundMessagingTarget.init(endpointRegistry);

					endpointRegistry.registerMessagingTarget(target, compoundMessagingTarget);
				} else {

					// retrieve the authority and user and attributes

					String xriString = target;
					if (xriString.endsWith("/")) xriString = xriString.substring(0, xriString.length() - 1);

					Xri xri = null;
					XRI3Segment inumber = null;
					String userIdentifier = null;
					User user = null;

					xri = xriStore.findXri(xriString);
					if (xri != null && xri.getCanonicalID() != null) inumber = new XRI3Segment(xri.getCanonicalID().getValue());
					if (xri != null) userIdentifier = xri.getUserIdentifier();
					if (userIdentifier != null) user = ibrokerStore.findUser(userIdentifier);
					if (inumber == null || user == null) throw new Exception("Context not found.");

					// create CompoundMessagingTarget

					CompoundMessagingTarget compoundMessagingTarget = new CompoundMessagingTarget();
					compoundMessagingTarget.setMode(CompoundMessagingTarget.MODE_WRITE_FIRST_HANDLED);

					// create and add ContextResourceMessagingTarget

					ContextResourceMessagingTarget contextResourceMessagingTarget = new ContextResourceMessagingTarget();
					contextResourceMessagingTarget.setInumber(inumber);
					contextResourceMessagingTarget.init(endpointRegistry);

					compoundMessagingTarget.getMessagingTargets().add(contextResourceMessagingTarget);

					// create and add IbrokerResourceMessagingTarget

					XriResourceMessagingTarget xriResourceMessagingTarget = new XriResourceMessagingTarget();
					xriResourceMessagingTarget.setInumber(inumber);
					xriResourceMessagingTarget.setXri(xri);
					xriResourceMessagingTarget.setUser(user);
					xriResourceMessagingTarget.init(endpointRegistry);

					compoundMessagingTarget.getMessagingTargets().add(xriResourceMessagingTarget);

					// create and add GraphMessagingTarget

					String databasePath = "./xdiFront-" + DynamicEndpointServlet.getInstance().getContextName() + "/";
					String databaseName = (xri.getCanonicalID() != null) ? xri.getCanonicalID().toString() : target;

					BDBGraphFactory graphFactory = new BDBGraphFactory();
					graphFactory.setDatabasePath(databasePath);
					graphFactory.setDatabaseName(databaseName);

					Graph graph = graphFactory.openGraph();

					GraphMessagingTarget graphMessagingTarget = new GraphMessagingTarget();
					graphMessagingTarget.setGraph(graph);
					graphMessagingTarget.getMessageInterceptors().add(new RoutingMessageInterceptor());
					graphMessagingTarget.init(endpointRegistry);

					compoundMessagingTarget.getMessagingTargets().add(graphMessagingTarget);

					// finish and register CompoundMessagingTarget

					compoundMessagingTarget.init(endpointRegistry);

					endpointRegistry.registerMessagingTarget(target, compoundMessagingTarget);
				}
			} catch (Exception ex) {

				log.error("Cannot create messaging target for /" + target + ": " + ex.getMessage(), ex);
				((HttpServletResponse) response).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
				return;
			}
		} else {

			log.info("Already have messaging target for /" + target);
		}

		chain.doFilter(request, response);
	}
}
