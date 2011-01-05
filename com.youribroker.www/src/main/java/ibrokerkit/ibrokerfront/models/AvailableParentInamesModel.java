package ibrokerkit.ibrokerfront.models;

import ibrokerkit.ibrokerfront.webapplication.IbrokerApplication;
import ibrokerkit.ibrokerfront.webapplication.IbrokerSession;
import ibrokerkit.ibrokerstore.store.User;
import ibrokerkit.iname4java.store.Xri;
import ibrokerkit.iname4java.store.XriStore;
import ibrokerkit.iname4java.store.XriStoreException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Application;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.LoadableDetachableModel;

public class AvailableParentInamesModel extends LoadableDetachableModel implements IChoiceRenderer {

	private static final long serialVersionUID = -605535688450064767L;

	private static Log log = LogFactory.getLog(AvailableParentInamesModel.class.getName());

	private static final String ATTRIBUTE_KEY_INVISIBLE = "invisible";

	@Override
	public Object load() {

		List<Xri> xris = new ArrayList<Xri> ();

		XriStore xriStore = ((IbrokerApplication) Application.get()).getXriStore();

		User user = ((IbrokerSession) Session.get()).getUser();
		String userIdentifier = user == null ? null : user.getIdentifier();

		// list root xris and xris that belong to the user

		log.debug("Listing root and user xris.");

		try {

			xris.addAll(xriStore.listRootXris());
			if (userIdentifier != null) xris.addAll(xriStore.listUserXris(userIdentifier));
		} catch (XriStoreException ex) {

			log.error("Cannot list root and user xris: " + ex.getMessage(), ex);
			throw new RuntimeException(ex);
		}

		// remove xris with an invisible attribute

		try {

			for (Iterator<Xri> i = xris.iterator(); i.hasNext(); ) {

				Xri xri = i.next();
				if (xri.hasXriAttribute(ATTRIBUTE_KEY_INVISIBLE)) i.remove();
			}
		} catch (XriStoreException ex) {

			log.error("Cannot read xri attribute: " + ex.getMessage(), ex);
			throw new RuntimeException(ex);
		}

		// done

		log.debug("Done.");
		return(xris);
	}

	public Object getDisplayValue(Object object) {

		return(((Xri) object).toString());
	}

	public String getIdValue(Object object, int index) {

		return(Integer.toString(index));
	}
}
