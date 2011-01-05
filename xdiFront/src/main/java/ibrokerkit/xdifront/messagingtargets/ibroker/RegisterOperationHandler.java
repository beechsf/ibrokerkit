package ibrokerkit.xdifront.messagingtargets.ibroker;

import ibrokerkit.ibrokerstore.store.StoreUtil;
import ibrokerkit.iname4java.store.Xri;
import ibrokerkit.iname4java.store.XriStoreException;
import ibrokerkit.iname4java.store.impl.openxri.OpenxriXriData;
import ibrokerkit.xdifront.DynamicEndpointServlet;
import ibrokerkit.xdifront.util.XriWizard;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.higgins.xdi4j.exceptions.MessagingException;
import org.eclipse.higgins.xdi4j.messaging.MessageResult;
import org.eclipse.higgins.xdi4j.messaging.Operation;
import org.eclipse.higgins.xdi4j.messaging.server.impl.OperationHandler;
import org.eclipse.higgins.xdi4j.xri3.impl.XRI3Segment;
import org.openxri.GCSAuthority;
import org.openxri.XRI;
import org.openxri.xml.XRD;

public class RegisterOperationHandler implements OperationHandler {

	private static final Log log = LogFactory.getLog(RegisterOperationHandler.class);

	@Override
	public boolean execute(Operation operation, MessageResult messageResult, Object executionContext) throws MessagingException {

		ibrokerkit.iname4java.store.XriStore xriStore = DynamicEndpointServlet.getInstance().getXriStore();
		ibrokerkit.ibrokerstore.store.Store ibrokerStore = DynamicEndpointServlet.getInstance().getIbrokerStore();

		// read information from the operation

		RegisterOperation registerOperation = (RegisterOperation) operation;

		String iname = registerOperation.getIname().toString();
		String password = registerOperation.getPassword();
		String email = registerOperation.getEmail();

		// try to find parent xri

		GCSAuthority gcsAuthority = (GCSAuthority) new XRI(iname).getAuthorityPath();
		GCSAuthority parentAuthority = (GCSAuthority) gcsAuthority.getParent();
		Xri parentXri = null;
		String localName = gcsAuthority.getSubSegmentAt(gcsAuthority.getNumSubSegments() - 1).toString();

		try {

			parentXri = xriStore.findXri(parentAuthority.toString());
			if (xriStore.existsXri(parentXri, localName)) throw new MessagingException("XRI exists already.");
		} catch (XriStoreException ex) {

			log.warn("Can not look up parent XRI: " + ex.getMessage(), ex);
			throw new MessagingException("Can not look up parent XRI: " + ex.getMessage(), ex);
		}

		if (parentXri == null) throw new MessagingException("Can not find parent XRI.");

		// create user and xri

		Xri xri;
		String inumber;

		try {

			ibrokerStore.createOrUpdateUser(iname, StoreUtil.hashPass(password), null, iname, email, Boolean.FALSE);

			OpenxriXriData xriData = new OpenxriXriData();
			xriData.setUserIdentifier(iname);
			xriData.setXrd(new XRD());

			xri = xriStore.registerXri(parentXri, localName, xriData, 0);
			inumber = xri.getCanonicalID().getValue();

			XriWizard.configure(xri);
		} catch (Exception ex) {

			log.warn("Can not create XRI: " + ex.getMessage(), ex);
			throw new MessagingException("Can not create XRI: " + ex.getMessage(), ex);
		}

		// done

		messageResult.getGraph().createStatement(new XRI3Segment(iname), new XRI3Segment("$is$"), new XRI3Segment(inumber));
		return true;
	}
}
