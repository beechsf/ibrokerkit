package ibrokerkit.ibrokerfront.models;

import ibrokerkit.ibrokerfront.webapplication.IbrokerApplication;
import ibrokerkit.iname4java.store.Xri;
import ibrokerkit.iservicestore.store.Authentication;
import ibrokerkit.iservicestore.store.Store;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Application;
import org.apache.wicket.model.LoadableDetachableModel;


public class InameAuthenticationsModel extends LoadableDetachableModel {

	private static final long serialVersionUID = -6165620767050865488L;

	private static Log log = LogFactory.getLog(InameAuthenticationsModel.class.getName());

	private Xri iname;

	public InameAuthenticationsModel(Xri iname) {

		this.iname = iname;
	}

	@Override
	public Object load() {

		List<Authentication> list;

		Store store = ((IbrokerApplication) Application.get()).getIserviceStore();

		// list authentications that belong to this qxri

		log.debug("Listing authentications.");

		try {

			list = new ArrayList<Authentication> ();

			list.addAll(Arrays.asList(store.findAuthentications(this.iname.getAuthorityId())));

			List<String> fullNames = this.iname.getFullNames();
			if (fullNames != null) {

				for (String fullName : fullNames) {

					list.addAll(Arrays.asList(store.findAuthentications(fullName)));
				}
			}
		} catch (Exception ex) {

			log.error("Failed.", ex);
			return(new ArrayList<Authentication> ());
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
