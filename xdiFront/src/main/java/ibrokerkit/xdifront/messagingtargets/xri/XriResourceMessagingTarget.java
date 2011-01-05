package ibrokerkit.xdifront.messagingtargets.xri;

import ibrokerkit.ibrokerstore.store.User;
import ibrokerkit.iname4java.store.Xri;

import org.eclipse.higgins.xdi4j.Literal;
import org.eclipse.higgins.xdi4j.Predicate;
import org.eclipse.higgins.xdi4j.Subject;
import org.eclipse.higgins.xdi4j.exceptions.MessagingException;
import org.eclipse.higgins.xdi4j.messaging.Message;
import org.eclipse.higgins.xdi4j.messaging.server.impl.ResourceHandler;
import org.eclipse.higgins.xdi4j.messaging.server.impl.ResourceMessagingTarget;
import org.eclipse.higgins.xdi4j.messaging.server.interceptor.impl.ReadOnlyAddressInterceptor;
import org.eclipse.higgins.xdi4j.xri3.impl.XRI3;
import org.eclipse.higgins.xdi4j.xri3.impl.XRI3Segment;

public class XriResourceMessagingTarget extends ResourceMessagingTarget {

	private XRI3Segment inumber;
	private Xri xri;
	private User user;

	private ReadOnlyAddressInterceptor readOnlyAddressInterceptor;

	public XriResourceMessagingTarget() {

		super(true);

		this.readOnlyAddressInterceptor = new ReadOnlyAddressInterceptor();
		this.getAddressInterceptors().add(this.readOnlyAddressInterceptor);
	}

	@Override
	public ResourceHandler getResource(Message message, Subject operationSubject) throws MessagingException {

		if (operationSubject.getSubjectXri().equals(this.inumber)) {

			return new XriSubjectResourceHandler(message, operationSubject, this.xri, this.user);
		}

		return null;
	}

	@Override
	public ResourceHandler getResource(Message message, Subject operationSubject, Predicate operationPredicate, Literal operationLiteral) throws MessagingException {

		if (operationSubject.getSubjectXri().equals(this.inumber) &&
				operationPredicate.getPredicateXri().equals(new XRI3Segment("$password"))) {

			return new XriSubjectPredicateLiteralResourceHandler(message, operationSubject, operationPredicate, operationLiteral, this.user);
		}

		return null;
	}

	public XRI3Segment getInumber() {

		return this.inumber;
	}

	public void setInumber(XRI3Segment inumber) {

		this.inumber = inumber;

		this.readOnlyAddressInterceptor.setReadOnlyAddresses(new XRI3[] {

				new XRI3(inumber.toString() + "/$is"),
				new XRI3(inumber.toString() + "/$$is"),
				new XRI3(inumber.toString() + "/$is$a")
		});
	}

	public Xri getXri() {

		return this.xri;
	}

	public void setXri(Xri xri) {

		this.xri = xri;
	}

	public User getUser() {

		return this.user;
	}

	public void setUser(User user) {

		this.user = user;
	}
}
