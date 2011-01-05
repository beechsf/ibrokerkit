package ibrokerkit.ibrokerfront.models;

import ibrokerkit.iname4java.store.Xri;
import ibrokerkit.iname4java.store.XriStoreException;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.model.LoadableDetachableModel;
import org.openxri.xml.Redirect;

public class XriRedirectsModel extends LoadableDetachableModel {

	private static final long serialVersionUID = 7433057350139081074L;

	private static Log log = LogFactory.getLog(XriRedirectsModel.class.getName());

	private Xri xri;

	public XriRedirectsModel(Xri xri ) {

		this.xri = xri ;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object load() {

		List<Redirect> list;

		// list redirects that belong to the xri

		log.debug("Listing redirects for an xri.");

		try {
			
			list = this.xri.getRedirects();
			if (list == null) throw new NullPointerException("redirects is null");
		} catch (XriStoreException ex) {
			
			throw new RuntimeException("Cannot get redirects: " + ex.getMessage(), ex);
		}

		log.debug("Done.");
		return(list);
	}

	public int getSize() {

		return(((List<?>) this.getObject()).size());
	}
}
