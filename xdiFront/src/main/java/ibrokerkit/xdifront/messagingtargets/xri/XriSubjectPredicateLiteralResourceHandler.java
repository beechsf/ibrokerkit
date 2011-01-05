package ibrokerkit.xdifront.messagingtargets.xri;


import ibrokerkit.ibrokerstore.store.StoreException;
import ibrokerkit.ibrokerstore.store.StoreUtil;
import ibrokerkit.ibrokerstore.store.User;
import ibrokerkit.iname4java.store.Xri;
import ibrokerkit.xdifront.DynamicEndpointServlet;

import org.eclipse.higgins.xdi4j.Literal;
import org.eclipse.higgins.xdi4j.Predicate;
import org.eclipse.higgins.xdi4j.Subject;
import org.eclipse.higgins.xdi4j.addressing.Addressing;
import org.eclipse.higgins.xdi4j.exceptions.MessagingException;
import org.eclipse.higgins.xdi4j.messaging.Message;
import org.eclipse.higgins.xdi4j.messaging.MessageResult;
import org.eclipse.higgins.xdi4j.messaging.Operation;
import org.eclipse.higgins.xdi4j.messaging.server.impl.AbstractResourceHandler;
import org.eclipse.higgins.xdi4j.messaging.server.impl.ExecutionContext;
import org.eclipse.higgins.xdi4j.xri3.impl.XRI3;

public class XriSubjectPredicateLiteralResourceHandler extends AbstractResourceHandler {

	private User user;

	public XriSubjectPredicateLiteralResourceHandler(Message message, Subject operationSubject, Predicate operationPredicate, Literal operationLiteral, User user) {

		super(message, operationSubject, operationPredicate, operationLiteral);

		this.user = user;
	}

	@Override
	public boolean executeMod(Operation operation, MessageResult messageResult, ExecutionContext executionContext) throws MessagingException {

		ibrokerkit.iname4java.store.XriStore xriStore = DynamicEndpointServlet.getInstance().getXriStore();
		ibrokerkit.ibrokerstore.store.Store ibrokerStore = DynamicEndpointServlet.getInstance().getIbrokerStore();

		// read information from the message

		String senderXriString = operation.getSenderXri().toString();
		String senderPassword = Addressing.findLiteralData(operation.getSender(), new XRI3("$password"));
		String newPassword = this.operationLiteral.getData();

		// retrieve the sender xri and user

		Xri senderXri = null;
		String senderUserIdentifier = null;
		User senderUser = null;

		try {

			senderXri = xriStore.findXri(senderXriString);
			if (senderXri != null) senderUserIdentifier = senderXri.getUserIdentifier();
			if (senderUserIdentifier != null) senderUser = ibrokerStore.findUser(senderUserIdentifier);
		} catch (Exception ex) {

			throw new MessagingException("Cannot find sender user " + senderXriString + ": " + ex.getMessage(), ex);
		}

		// check if the sender user is the same as the one on which we operate

		boolean isSelf = (senderUser != null && senderUser.equals(this.user));

		// check if the user password is correct

		boolean userPasswordCorrect = senderPassword != null && senderUser != null && StoreUtil.checkPass(senderUser.getPass(), senderPassword);

		// with the correct password we can modify the password

		if (isSelf && userPasswordCorrect) {

			try {

				this.user.setPass(StoreUtil.hashPass(newPassword));
				ibrokerStore.updateObject(this.user);
			} catch (StoreException ex) {

				throw new MessagingException("Cannot update user password: " + ex.getMessage(), ex);
			}
		} else {

			throw new MessagingException("Not allowed to update password.");
		}

		return true;
	}
}
