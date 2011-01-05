package ibrokerkit.ibrokerfront.models;

import ibrokerkit.iname4java.store.Xri;
import ibrokerkit.iname4java.store.XriStoreException;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.model.LoadableDetachableModel;
import org.openxri.xml.Ref;

public class XriRefsModel extends LoadableDetachableModel {

	private static final long serialVersionUID = 1752610136552362998L;

	private static Log log = LogFactory.getLog(XriRefsModel.class.getName());

	private Xri xri;

	public XriRefsModel(Xri xri) {

		this.xri = xri;
	}

	@Override
	public Object load() {

		List<Ref> refs;

		// list refs that belong to the xri

		log.debug("Listing refs for an xri.");

		try {
			
			refs = this.xri.getRefs();
			if (refs == null) throw new NullPointerException("refs is null");
		} catch (XriStoreException ex) {
			
			throw new RuntimeException("Cannot get refs: " + ex.getMessage(), ex);
		}

		log.debug("Done.");
		return(refs);
	}

	public int getSize() {

		return(((List<?>) this.getObject()).size());
	}
}
