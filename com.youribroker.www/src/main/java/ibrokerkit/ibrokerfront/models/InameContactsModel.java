package ibrokerkit.ibrokerfront.models;

import ibrokerkit.ibrokerfront.webapplication.IbrokerApplication;
import ibrokerkit.iname4java.store.Xri;
import ibrokerkit.iservicestore.store.Contact;
import ibrokerkit.iservicestore.store.Store;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Application;
import org.apache.wicket.model.LoadableDetachableModel;


public class InameContactsModel extends LoadableDetachableModel {

	private static final long serialVersionUID = 7415280839801700678L;

	private static Log log = LogFactory.getLog(InameContactsModel.class.getName());

	private Xri iname;

	public InameContactsModel(Xri iname) {

		this.iname = iname;
	}

	@Override
	public Object load() {

		List<Contact> list;

		Store store = ((IbrokerApplication) Application.get()).getIserviceStore();

		// list contacts that belong to this qxri

		log.debug("Listing contacts.");

		try {

			list = new ArrayList<Contact> ();

			list.addAll(Arrays.asList(store.findContacts(this.iname.getAuthorityId())));

			List<String> fullNames = this.iname.getFullNames();
			if (fullNames != null) {

				for (String fullName : fullNames) {

					list.addAll(Arrays.asList(store.findContacts(fullName)));
				}
			}
		} catch (Exception ex) {

			log.error("Failed.", ex);
			return(new ArrayList<Contact> ());
		}

		// done

		log.debug("Done.");
		return(list);
	}

	public int getSize() {

		List<?> list = (List<?>) this.getObject();
		return(list == null ? 0 : list.size());
	}
}
