package ibrokerkit.xdifront.messagingtargets.ibroker;

import org.eclipse.higgins.xdi4j.Subject;
import org.eclipse.higgins.xdi4j.exceptions.MessagingException;
import org.eclipse.higgins.xdi4j.messaging.Message;
import org.eclipse.higgins.xdi4j.messaging.server.impl.ResourceHandler;
import org.eclipse.higgins.xdi4j.messaging.server.impl.ResourceMessagingTarget;

public class IbrokerResourceMessagingTarget extends ResourceMessagingTarget {

	public IbrokerResourceMessagingTarget() {

		super(true);
	}

	@Override
	public ResourceHandler getResource(Message message, Subject subject) throws MessagingException {

		return new IbrokerSubjectResourceHandler(message, subject);
	}
}
