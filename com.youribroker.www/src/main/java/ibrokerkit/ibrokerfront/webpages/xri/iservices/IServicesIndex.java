package ibrokerkit.ibrokerfront.webpages.xri.iservices;

import ibrokerkit.iname4java.store.Xri;

public class IServicesIndex extends IServices {

	private static final long serialVersionUID = -591774076524134976L;

	public IServicesIndex(Xri xri) {

		this(xri, true);
	}

	protected IServicesIndex(Xri xri, boolean createPages) {

		super(xri, createPages);
	}
}
