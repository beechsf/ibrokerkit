package ibrokerkit.xdifront.messagingtargets.ibroker;


import ibrokerkit.iname4java.store.Xri;
import ibrokerkit.iname4java.store.XriStoreException;
import ibrokerkit.xdifront.DynamicEndpointServlet;

import java.util.List;

import org.eclipse.higgins.xdi4j.Subject;
import org.eclipse.higgins.xdi4j.constants.DictionaryConstants;
import org.eclipse.higgins.xdi4j.exceptions.MessagingException;
import org.eclipse.higgins.xdi4j.messaging.Message;
import org.eclipse.higgins.xdi4j.messaging.MessageResult;
import org.eclipse.higgins.xdi4j.messaging.Operation;
import org.eclipse.higgins.xdi4j.messaging.server.impl.AbstractResourceHandler;
import org.eclipse.higgins.xdi4j.messaging.server.impl.ExecutionContext;
import org.eclipse.higgins.xdi4j.xri3.impl.XRI3Segment;

public class IbrokerSubjectResourceHandler extends AbstractResourceHandler {

	public IbrokerSubjectResourceHandler(Message message, Subject subject) {

		super(message, subject);
	}

	@Override
	public boolean executeGet(Operation operation, MessageResult messageResult, ExecutionContext executionContext) throws MessagingException {

		ibrokerkit.iname4java.store.XriStore xriStore = DynamicEndpointServlet.getInstance().getXriStore();

		// read information from the message

		String xriString = this.operationSubject.getSubjectXri().toString();

		// retrieve the xri and user

		Xri xri = null;

		try {

			xri = xriStore.findXri(xriString);
		} catch (Exception ex) {

			throw new MessagingException("Cannot find xri " + xriString + ": " + ex.getMessage(), ex);
		}

		// anyone can check if an XRI exists or not

		if (xri == null) return false;

		messageResult.getGraph().createStatement(this.operationSubject.getSubjectXri(), DictionaryConstants.XRI_IS_A, new XRI3Segment(this.operationSubject.getSubjectXri().getFirstSubSegment().getGCS().toString()));

		try {

			List<String> aliases = xri.getAliases();
			for (String alias : aliases) messageResult.getGraph().createStatement(this.operationSubject.getSubjectXri(), DictionaryConstants.XRI_IS, new XRI3Segment(alias));

			if (xri.getCanonicalID() != null) {

				messageResult.getGraph().createStatement(this.operationSubject.getSubjectXri(), new XRI3Segment("$$is"), new XRI3Segment(xri.getCanonicalID().getValue()));
			}
		} catch (XriStoreException ex) {

			throw new MessagingException("Cannot get XRI synonyms from store: " + ex.getMessage(), ex);
		}

		return(true);
	}
}
