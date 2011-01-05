package ibrokerkit.xdifront.messagingtargets.xri;


import ibrokerkit.ibrokerstore.store.StoreUtil;
import ibrokerkit.ibrokerstore.store.User;
import ibrokerkit.iname4java.store.Xri;
import ibrokerkit.iname4java.store.XriStoreException;
import ibrokerkit.xdifront.DynamicEndpointServlet;

import java.util.List;

import org.eclipse.higgins.xdi4j.Subject;
import org.eclipse.higgins.xdi4j.addressing.Addressing;
import org.eclipse.higgins.xdi4j.constants.DictionaryConstants;
import org.eclipse.higgins.xdi4j.exceptions.MessagingException;
import org.eclipse.higgins.xdi4j.messaging.Message;
import org.eclipse.higgins.xdi4j.messaging.MessageResult;
import org.eclipse.higgins.xdi4j.messaging.Operation;
import org.eclipse.higgins.xdi4j.messaging.server.impl.AbstractResourceHandler;
import org.eclipse.higgins.xdi4j.messaging.server.impl.ExecutionContext;
import org.eclipse.higgins.xdi4j.xri3.impl.XRI3;
import org.eclipse.higgins.xdi4j.xri3.impl.XRI3Segment;

public class XriSubjectResourceHandler extends AbstractResourceHandler {

	private Xri xri;
	private User user;

	public XriSubjectResourceHandler(Message message, Subject subject, Xri xri, User user) {

		super(message, subject);

		this.xri = xri;
		this.user = user;
	}

	@Override
	public boolean executeGet(Operation operation, MessageResult messageResult, ExecutionContext executionContext) throws MessagingException {

		ibrokerkit.iname4java.store.XriStore xriStore = DynamicEndpointServlet.getInstance().getXriStore();
		ibrokerkit.ibrokerstore.store.Store ibrokerStore = DynamicEndpointServlet.getInstance().getIbrokerStore();

		// read information from the message

		String senderXriString = operation.getSenderXri().toString();
		String senderPassword = Addressing.findLiteralData(operation.getSender(), new XRI3("$password"));

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

		// anyone can check if an XRI exists or not

		messageResult.getGraph().createStatement(this.operationSubject.getSubjectXri(), DictionaryConstants.XRI_IS_A, new XRI3Segment(this.operationSubject.getSubjectXri().getFirstSubSegment().getGCS().toString()));

		try {

			List<String> aliases = this.xri.getAliases();
			for (String alias : aliases) messageResult.getGraph().createStatement(this.operationSubject.getSubjectXri(), DictionaryConstants.XRI_IS, new XRI3Segment(alias));

			if (this.xri.getCanonicalID() != null) {

				messageResult.getGraph().createStatement(this.operationSubject.getSubjectXri(), new XRI3Segment("$$is"), new XRI3Segment(this.xri.getCanonicalID().getValue()));
			}
		} catch (XriStoreException ex) {

			throw new MessagingException("Cannot get XRI synonyms from store: " + ex.getMessage(), ex);
		}

		// with the correct password you can check anything on yourself

		try {

			if (isSelf && userPasswordCorrect) {

				messageResult.getGraph().createStatement(this.operationSubject.getSubjectXri(), new XRI3Segment("$password"), senderPassword);

				if (this.xri.hasAuthorityAttribute("publickey"))
					messageResult.getGraph().createStatement(this.operationSubject.getSubjectXri(), new XRI3Segment("$key$public"), this.xri.getAuthorityAttribute("publickey"));

				if (this.xri.hasAuthorityAttribute("privatekey"))
					messageResult.getGraph().createStatement(this.operationSubject.getSubjectXri(), new XRI3Segment("$key$private"), this.xri.getAuthorityAttribute("privatekey"));

				if (this.xri.hasAuthorityAttribute("certificate"))
					messageResult.getGraph().createStatement(this.operationSubject.getSubjectXri(), new XRI3Segment("$certificate$x.509"), this.xri.getAuthorityAttribute("certificate"));
			}
		} catch (XriStoreException ex) {

			throw new MessagingException("Cannot get XRI attributes from store: " + ex.getMessage(), ex);
		}

		return(true);
	}
}
