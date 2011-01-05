package ibrokerkit.ibrokerfront.models;

import ibrokerkit.iname4java.store.Xri;
import ibrokerkit.iname4java.store.XriStoreException;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.model.LoadableDetachableModel;
import org.openxri.xml.Service;

public class XriServicesModel extends LoadableDetachableModel {

	private static final long serialVersionUID = -9081194131956793321L;

	private static Log log = LogFactory.getLog(XriServicesModel.class.getName());

	private Xri xri;

	public XriServicesModel(Xri xri) {

		this.xri = xri;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object load() {

		List<Service> services;

		// list services that belong to the xri

		log.debug("Listing services for an xri.");

		try {
			
			services = this.xri.getServices();
			if (services == null) throw new NullPointerException("services is null");
		} catch (XriStoreException ex) {
			
			throw new RuntimeException("Cannot get services: " + ex.getMessage(), ex);
		}

		log.debug("Done.");
		return(services);
	}

	public int getSize() {

		return(((List<?>) this.getObject()).size());
	}
}
