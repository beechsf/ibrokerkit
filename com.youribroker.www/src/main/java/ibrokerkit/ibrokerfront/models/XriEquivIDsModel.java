package ibrokerkit.ibrokerfront.models;

import ibrokerkit.iname4java.store.Xri;
import ibrokerkit.iname4java.store.XriStoreException;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.model.LoadableDetachableModel;
import org.openxri.xml.EquivID;

public class XriEquivIDsModel extends LoadableDetachableModel {

	private static final long serialVersionUID = 1456709299458515350L;

	private static Log log = LogFactory.getLog(XriEquivIDsModel.class.getName());

	private Xri xri;

	public XriEquivIDsModel(Xri xri) {

		this.xri = xri;
	}

	@Override
	public Object load() {

		List<EquivID> equivIds;

		// list equivIds that belong to the xri

		log.debug("Listing equivIds for an xri.");

		try {
			
			equivIds = this.xri.getEquivIDs();
			if (equivIds == null) throw new NullPointerException("equivIds is null");
		} catch (XriStoreException ex) {
			
			throw new RuntimeException("Cannot get equivIds: " + ex.getMessage(), ex);
		}

		log.debug("Done.");
		return(equivIds);
	}

	public int getSize() {

		return(((List<?>) this.getObject()).size());
	}
}
