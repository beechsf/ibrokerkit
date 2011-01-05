package ibrokerkit.xdifront.messagingtargets.timetoken;


import ibrokerkit.ibrokerstore.store.StoreUtil;
import ibrokerkit.ibrokerstore.store.User;
import ibrokerkit.iname4java.store.Xri;
import ibrokerkit.xdifront.DynamicEndpointServlet;

import java.security.PrivateKey;
import java.security.SecureRandom;

import org.apache.commons.codec.binary.Base64;
import org.eclipse.higgins.xdi4j.Subject;
import org.eclipse.higgins.xdi4j.addressing.Addressing;
import org.eclipse.higgins.xdi4j.dictionary.Dictionary;
import org.eclipse.higgins.xdi4j.exceptions.MessagingException;
import org.eclipse.higgins.xdi4j.messaging.Message;
import org.eclipse.higgins.xdi4j.messaging.MessageResult;
import org.eclipse.higgins.xdi4j.messaging.Operation;
import org.eclipse.higgins.xdi4j.messaging.server.impl.AbstractResourceHandler;
import org.eclipse.higgins.xdi4j.messaging.server.impl.ExecutionContext;
import org.eclipse.higgins.xdi4j.xri3.impl.XRI3;
import org.eclipse.higgins.xdi4j.xri3.impl.XRI3Segment;

public class TimeTokenSubjectResourceHandler extends AbstractResourceHandler {

	private static final String SIGNATURE_ALGORITHM = "SHA1withRSA";

	private static final SecureRandom random = new SecureRandom();

	public TimeTokenSubjectResourceHandler(Message message, Subject subject) {

		super(message, subject);
	}

	@Override
	public boolean executeGet(Operation operation, MessageResult messageResult, ExecutionContext executionContext) throws MessagingException {

		ibrokerkit.iname4java.store.XriStore xriStore = DynamicEndpointServlet.getInstance().getXriStore();
		ibrokerkit.ibrokerstore.store.Store ibrokerStore = DynamicEndpointServlet.getInstance().getIbrokerStore();

		// read information from the message

		String senderXriString = operation.getSenderXri().toString();
		String senderPassword = Addressing.findLiteralData(operation.getSender(), new XRI3("$password"));
		String data = Addressing.findLiteralData(operation.getSender(), new XRI3("+data"));

		// retrieve the sender xri and user

		Xri senderXri = null;
		String senderUserIdentifier = null;
		User senderUser = null;

		if (Dictionary.hasInheritance(operation.getSender(), new XRI3Segment("="), null) || Dictionary.hasInheritance(operation.getSender(), new XRI3Segment("@"), null)) {

			try {

				senderXri = xriStore.findXri(senderXriString);
				if (senderXri != null) senderUserIdentifier = senderXri.getUserIdentifier();
				if (senderUserIdentifier != null) senderUser = ibrokerStore.findUser(senderUserIdentifier);
			} catch (Exception ex) {

				throw new MessagingException("Cannot find sender user " + senderXriString + ": " + ex.getMessage(), ex);
			}
		} else {

			throw new MessagingException("Sender must be a user XRI.");
		}

		// check if the user password is correct

		boolean userPasswordCorrect = senderPassword != null && senderUser != null && StoreUtil.checkPass(senderUser.getPass(), senderPassword);

		// can only do this with correct password

		if (userPasswordCorrect) {

			// calculate the time left

			long now = System.currentTimeMillis();
			long timeleft;

			try {

				String lasttimeStr;
				long lasttime;

				lasttimeStr = senderXri.getAuthorityAttribute("timetoken_time_" + this.operationSubject.getSubjectXri().toString());
				lasttime = lasttimeStr == null ? 0 : Long.valueOf(lasttimeStr);
				timeleft = this.calculateTimeleft(this.operationSubject.getSubjectXri(), lasttime, now);
			} catch (Exception ex) {

				throw new MessagingException("Cannot calculate time left: " + ex.getMessage(), ex);
			}

			// time token requested?

			if (this.operationSubject.containsPredicate(new XRI3Segment("$time")) &&
					this.operationSubject.containsPredicate(new XRI3Segment("$value")) &&
					data != null &&
					timeleft <= 0) {

				long time;
				String value;

				try {

					time = now;
					value = this.makeTimeToken(this.operationSubject.getSubjectXri(), data, time);

					senderXri.setAuthorityAttribute("timetoken_time_" + this.operationSubject.getSubjectXri().toString(), Long.toString(time));
				} catch (Exception ex) {

					throw new MessagingException("Cannot sign data: " + ex.getMessage(), ex);
				}

				messageResult.getGraph().createStatement(this.operationSubject.getSubjectXri(), new XRI3Segment("$time"), Long.toString(time));
				messageResult.getGraph().createStatement(this.operationSubject.getSubjectXri(), new XRI3Segment("$value"), value);
			}

			// time left requested?

			if (this.operationSubject.containsPredicate(new XRI3Segment("$timeleft"))) {

				messageResult.getGraph().createStatement(this.operationSubject.getSubjectXri(), new XRI3Segment("$timeleft"), Long.toString(timeleft));
			}
		}

		return true;
	}

	private String makeTimeToken(XRI3Segment subjectXri, String data, long time) throws Exception {

		PrivateKey brokerPrivateKey = DynamicEndpointServlet.getInstance().getBrokerPrivateKey();

		String string = subjectXri.toString() + " " + data + " " + Long.toString(time);

		java.security.Signature s = java.security.Signature.getInstance(SIGNATURE_ALGORITHM);
		s.initSign(brokerPrivateKey, random);
		s.update(string.getBytes("UTF-8"));
		return new String(Base64.encodeBase64(s.sign()));
	}

	private long calculateTimeleft(XRI3Segment subjectXri, long lasttime, long now) throws MessagingException {

		long timeleft;

		if (subjectXri.equals(new XRI3Segment("$timetoken$1")))
			timeleft = 3600000L - (now - lasttime);
		else if (subjectXri.equals(new XRI3Segment("$timetoken$24")))
			timeleft = 86400000L - (now - lasttime);
		else if (subjectXri.equals(new XRI3Segment("$timetoken$30")))
			timeleft = 2592000000L - (now - lasttime);
		else 
			throw new MessagingException("Invalid time token: " + subjectXri.toString());

		return timeleft;
	}
}
