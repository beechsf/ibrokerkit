package ibrokerkit.iserviceadmin.models;

import ibrokerkit.iserviceadmin.webapplication.IServiceAdminApplication;
import ibrokerkit.iservicestore.store.Contact;
import ibrokerkit.iservicestore.store.IService;
import ibrokerkit.iservicestore.store.Locator;
import ibrokerkit.iservicestore.store.Store;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Application;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.LoadableDetachableModel;

public class AllLocatorsModel extends LoadableDetachableModel implements IChoiceRenderer {

	private static final long serialVersionUID = -605535688450064767L;

	private static Log log = LogFactory.getLog(AllLocatorsModel.class.getName());

	@Override
	public Object load() {

		final Store iservice4javaStore = ((IServiceAdminApplication) Application.get()).getIserviceStore();

		List<Locator> list;

		// list all locators

		log.debug("Listing all locators.");

		try {

			list = Arrays.asList(iservice4javaStore.listLocators());
		} catch (Exception ex) {

			log.error("Failed.", ex);
			return(new ArrayList<Locator> ());
		}

		log.debug("Done.");
		return(list);
	}

	public Object getDisplayValue(Object object) {

		IService iservice = (IService) object;

		return(iservice.getName());
	}

	public String getIdValue(Object object, int index) {

		return(((Contact) object).getId().toString());
	}
}
