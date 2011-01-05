package ibrokerkit.ibrokerfront.models;

import ibrokerkit.ibrokerfront.webapplication.IbrokerApplication;
import ibrokerkit.iname4java.store.Xri;
import ibrokerkit.iservicestore.store.Locator;
import ibrokerkit.iservicestore.store.Store;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Application;
import org.apache.wicket.model.LoadableDetachableModel;


public class InameLocatorsModel extends LoadableDetachableModel {

	private static final long serialVersionUID = 9148404846853381765L;

	private static Log log = LogFactory.getLog(InameLocatorsModel.class.getName());

	private Xri iname;

	public InameLocatorsModel(Xri iname) {

		this.iname = iname;
	}

	@Override
	public Object load() {

		List<Locator> list;

		Store store = ((IbrokerApplication) Application.get()).getIserviceStore();

		// list locators that belong to this qxri

		log.debug("Listing locators.");

		try {

			list = new ArrayList<Locator> ();

			list.addAll(Arrays.asList(store.findLocators(this.iname.getAuthorityId())));

			List<String> fullNames = this.iname.getFullNames();
			if (fullNames != null) {

				for (String fullName : fullNames) {

					list.addAll(Arrays.asList(store.findLocators(fullName)));
				}
			}
		} catch (Exception ex) {

			log.error("Failed.", ex);
			return(new ArrayList<Locator> ());
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
