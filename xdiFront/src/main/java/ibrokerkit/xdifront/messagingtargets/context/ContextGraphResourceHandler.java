package ibrokerkit.xdifront.messagingtargets.context;

import ibrokerkit.xdifront.DynamicEndpointServlet;

import java.util.Properties;

import org.eclipse.higgins.xdi4j.Graph;
import org.eclipse.higgins.xdi4j.exceptions.MessagingException;
import org.eclipse.higgins.xdi4j.messaging.Message;
import org.eclipse.higgins.xdi4j.messaging.MessageResult;
import org.eclipse.higgins.xdi4j.messaging.Operation;
import org.eclipse.higgins.xdi4j.messaging.server.impl.AbstractResourceHandler;
import org.eclipse.higgins.xdi4j.messaging.server.impl.ExecutionContext;
import org.eclipse.higgins.xdi4j.xri3.impl.XRI3Segment;

public class ContextGraphResourceHandler extends AbstractResourceHandler {

	private XRI3Segment inumber;

	public ContextGraphResourceHandler(Message message, Graph operationGraph, XRI3Segment inumber) {

		super(message, operationGraph);

		this.inumber = inumber;
	}

	@Override
	public boolean executeGet(Operation operation, MessageResult messageResult, ExecutionContext executionContext) throws MessagingException {

		Properties properties = DynamicEndpointServlet.getInstance().getProperties();

		messageResult.getGraph().createStatement(new XRI3Segment("$"), new XRI3Segment("$is$a"), new XRI3Segment("($xdi$v$1)"));
		messageResult.getGraph().createStatement(new XRI3Segment("$"), new XRI3Segment("$is$a"), new XRI3Segment("($pds$v$1)"));
		messageResult.getGraph().createStatement(new XRI3Segment("$"), new XRI3Segment("$is($xdi$v$1)"), this.inumber);

		String xdiService = properties.getProperty("xdi-service");
		if (xdiService != null) {

			if (! xdiService.endsWith("/")) xdiService += "/";
			xdiService += this.inumber.toString() + "/";

			if (xdiService.startsWith("https://"))
				messageResult.getGraph().createStatement(new XRI3Segment("$"), new XRI3Segment("$https$uri$1"), xdiService);
			else if (xdiService.startsWith("http://"))
				messageResult.getGraph().createStatement(new XRI3Segment("$"), new XRI3Segment("$http$uri$1"), xdiService);
		}

		return true;
	}
}
