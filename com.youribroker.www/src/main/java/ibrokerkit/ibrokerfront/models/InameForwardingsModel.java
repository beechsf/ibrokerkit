package ibrokerkit.ibrokerfront.models;

import ibrokerkit.ibrokerfront.webapplication.IbrokerApplication;
import ibrokerkit.iname4java.store.Xri;
import ibrokerkit.iservicestore.store.Forwarding;
import ibrokerkit.iservicestore.store.Store;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Application;
import org.apache.wicket.model.LoadableDetachableModel;


public class InameForwardingsModel extends LoadableDetachableModel {

	private static final long serialVersionUID = 5974564867027293845L;

	private static Log log = LogFactory.getLog(InameForwardingsModel.class.getName());

	private Xri iname;

	public InameForwardingsModel(Xri iname) {

		this.iname = iname;
	}

	@Override
	public Object load() {

		List<Forwarding> list;

		Store store = ((IbrokerApplication) Application.get()).getIserviceStore();

		// list forwardings that belong to this qxri

		log.debug("Listing forwardings.");

		try {

			list = new ArrayList<Forwarding> ();

			list.addAll(Arrays.asList(store.findForwardings(this.iname.getAuthorityId())));

			List<String> fullNames = this.iname.getFullNames();
			if (fullNames != null) {

				for (String fullName : fullNames) {

					list.addAll(Arrays.asList(store.findForwardings(fullName)));
				}
			}
		} catch (Exception ex) {

			log.error("Failed.", ex);
			return(new ArrayList<Forwarding> ());
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
