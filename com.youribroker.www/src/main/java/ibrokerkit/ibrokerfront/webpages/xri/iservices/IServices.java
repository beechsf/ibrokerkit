package ibrokerkit.ibrokerfront.webpages.xri.iservices;

import ibrokerkit.ibrokerfront.webpages.BasePage;
import ibrokerkit.iname4java.store.Xri;

import org.apache.wicket.Page;


public abstract class IServices extends BasePage {

	protected Xri xri;

	protected IServices(Xri xri, boolean createPages) {

		this.xri = xri;

		this.setTitle(this.getString("title") + ": " + xri);

		// create and add components

		if (createPages) {

			Page[] pages = new Page[6];

			if (this instanceof IServicesIndex) pages[0] = this; else pages[0] = new IServicesIndex(xri, false);
			if (this instanceof IServicesListAuthentications) pages[1] = this; else pages[1] = new IServicesListAuthentications(xri, false);
			if (this instanceof IServicesListContacts) pages[2] = this; else pages[2]  = new IServicesListContacts(xri, false);
			if (this instanceof IServicesListForwardings) pages[3] = this; else pages[3]  = new IServicesListForwardings(xri, false);
			if (this instanceof IServicesListLocators) pages[4] = this; else pages[4]  = new IServicesListLocators(xri, false);
			if (this instanceof IServicesAdd) pages[5] = this; else pages[5]  = new IServicesAdd(xri, false);

			for (Page page : pages) page.add(new IServicesMenuPanel("configMenuPanel", pages));

			// this is necessary so that the "edit" and "delete" pages also get a menu panel,
			// even though they are not one of the menu items

			if (! (this instanceof IServicesIndex) &&
					! (this instanceof IServicesListAuthentications) &&
					! (this instanceof IServicesListContacts) &&
					! (this instanceof IServicesListForwardings) &&
					! (this instanceof IServicesAdd)) this.add(new IServicesMenuPanel("configMenuPanel", pages));
		}
	}

	public Xri getQxri() {
		return (this.xri);
	}

	public void setQxri(Xri xri) {
		this.xri = xri;
	}
}
