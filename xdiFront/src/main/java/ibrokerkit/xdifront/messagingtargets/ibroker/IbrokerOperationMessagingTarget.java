package ibrokerkit.xdifront.messagingtargets.ibroker;

import org.eclipse.higgins.xdi4j.messaging.server.EndpointRegistry;
import org.eclipse.higgins.xdi4j.messaging.server.impl.OperationMessagingTarget;

public class IbrokerOperationMessagingTarget extends OperationMessagingTarget {

	@Override
	public void init(EndpointRegistry endpointRegistry) throws Exception {

		super.init(endpointRegistry);

		this.registerOperationHandler(RegisterOperation.class, new RegisterOperationHandler());
	}
}
