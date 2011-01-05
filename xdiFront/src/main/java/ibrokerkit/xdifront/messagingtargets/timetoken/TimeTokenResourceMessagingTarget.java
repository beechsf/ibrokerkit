package ibrokerkit.xdifront.messagingtargets.timetoken;

import org.eclipse.higgins.xdi4j.Subject;
import org.eclipse.higgins.xdi4j.exceptions.MessagingException;
import org.eclipse.higgins.xdi4j.messaging.Message;
import org.eclipse.higgins.xdi4j.messaging.server.impl.ResourceHandler;
import org.eclipse.higgins.xdi4j.messaging.server.impl.ResourceMessagingTarget;
import org.eclipse.higgins.xdi4j.xri3.impl.XRI3SubSegment;

public class TimeTokenResourceMessagingTarget extends ResourceMessagingTarget {

	private String target;

	public TimeTokenResourceMessagingTarget() {

		super(true);
	}

	@Override
	public ResourceHandler getResource(Message message, Subject subject) throws MessagingException {

		if (subject.getSubjectXri().startsWith(new XRI3SubSegment[] { new XRI3SubSegment("$timetoken") })) {

			return new TimeTokenSubjectResourceHandler(message, subject);
		}

		return null;
	}

	public String getTarget() {

		return this.target;
	}

	public void setTarget(String target) {

		this.target = target;
	}
}
