package ibrokerkit.ibrokerfront.models;

import ibrokerkit.ibrokerfront.webapplication.IbrokerSession;
import ibrokerkit.ibrokerfront.webapplication.IbrokerApplication;
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


public class UserInamesModel extends LoadableDetachableModel implements IChoiceRenderer {

	private static final long serialVersionUID = 3248872083550223139L;

	private static Log log = LogFactory.getLog(UserInamesModel.class.getName());

	private static final String ATTRIBUTE_KEY_INVISIBLE = "invisible";

	@Override
	public Object load() {

		List<Xri> xris = new ArrayList<Xri> ();

		if (! ((IbrokerSession) Session.get()).isLoggedIn()) return(xris);

		XriStore xriStore = ((IbrokerApplication) Application.get()).getXriStore();
		String userIdentifier = ((IbrokerSession) Session.get()).getUser().getIdentifier();

		// list user xris

		log.debug("Listing user xris.");

		try {

			xris = xriStore.listUserXris(userIdentifier);
		} catch (XriStoreException ex) {

			log.error("Cannot list user xris: " + ex.getMessage(), ex);
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
