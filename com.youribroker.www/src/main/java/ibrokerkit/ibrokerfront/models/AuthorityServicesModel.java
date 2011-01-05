package ibrokerkit.ibrokerfront.models;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.model.LoadableDetachableModel;
import org.openxri.store.Authority;
import org.openxri.xml.Service;
import org.openxri.xml.XRD;

public class AuthorityServicesModel extends LoadableDetachableModel {

	private static final long serialVersionUID = -9081194131956793321L;

	private static Log log = LogFactory.getLog(AuthorityServicesModel.class.getName());

	private Authority authority;

	public AuthorityServicesModel(Authority authority) {

		this.authority = authority;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object load() {

		List<Service> list;

		// list services that belong to the authority

		log.debug("Listing services for an authority.");

		XRD xrd = this.authority.getXrd();
		list = xrd.getServices();

		log.debug("Done.");
		return(list);
	}

	public int getSize() {

		return(((List<?>) this.getObject()).size());
	}
}
