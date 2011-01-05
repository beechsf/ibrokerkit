package ibrokerkit.ibrokerfront.models;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.model.LoadableDetachableModel;
import org.openxri.store.Authority;
import org.openxri.xml.Ref;
import org.openxri.xml.XRD;

public class AuthorityRefsModel extends LoadableDetachableModel {

	private static final long serialVersionUID = 1752610136552362998L;

	private static Log log = LogFactory.getLog(AuthorityRefsModel.class.getName());

	private Authority authority;

	public AuthorityRefsModel(Authority authority) {

		this.authority = authority;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object load() {

		List<Ref> list;

		// list refs that belong to the authority

		log.debug("Listing refs for an authority.");

		XRD xrd = this.authority.getXrd();
		list = xrd.getRefs();

		log.debug("Done.");
		return(list);
	}

	public int getSize() {

		return(((List<?>) this.getObject()).size());
	}
}
