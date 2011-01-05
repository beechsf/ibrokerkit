package ibrokerkit.xdifront.messagingtargets.context;

import org.eclipse.higgins.xdi4j.Graph;
import org.eclipse.higgins.xdi4j.exceptions.MessagingException;
import org.eclipse.higgins.xdi4j.messaging.Message;
import org.eclipse.higgins.xdi4j.messaging.server.impl.ResourceHandler;
import org.eclipse.higgins.xdi4j.messaging.server.impl.ResourceMessagingTarget;
import org.eclipse.higgins.xdi4j.messaging.server.interceptor.impl.ReadOnlyAddressInterceptor;
import org.eclipse.higgins.xdi4j.xri3.impl.XRI3;
import org.eclipse.higgins.xdi4j.xri3.impl.XRI3Segment;

public class ContextResourceMessagingTarget extends ResourceMessagingTarget {

	private XRI3Segment inumber;

	private static ReadOnlyAddressInterceptor readOnlyAddressInterceptor;

	static {

		readOnlyAddressInterceptor = new ReadOnlyAddressInterceptor();
		readOnlyAddressInterceptor.setReadOnlyAddresses(new XRI3[] {
				new XRI3("$/$is"),
				new XRI3("$/$$is"),
				new XRI3("$/$is$a"),
				new XRI3("$/$is($xdi$v$1)")
		});
	}

	public ContextResourceMessagingTarget() {

		super(true);

		this.getAddressInterceptors().add(readOnlyAddressInterceptor);
	}

	@Override
	public ResourceHandler getResource(Message message, Graph operationGraph) throws MessagingException {

		return new ContextGraphResourceHandler(message, operationGraph, this.inumber);
	}

	public XRI3Segment getInumber() {

		return this.inumber;
	}

	public void setInumber(XRI3Segment inumber) {

		this.inumber = inumber;
	}
}
