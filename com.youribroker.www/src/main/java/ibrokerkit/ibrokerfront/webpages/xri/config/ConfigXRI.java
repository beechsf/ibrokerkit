package ibrokerkit.ibrokerfront.webpages.xri.config;

import ibrokerkit.ibrokerfront.webpages.BasePage;
import ibrokerkit.iname4java.store.Xri;

import org.apache.wicket.Page;


public abstract class ConfigXRI extends BasePage {

	protected Xri xri;
	
	protected ConfigXRI(Xri xri, boolean createPages) {
		
		this.xri = xri;
	
		this.setTitle(this.getString("title") + ": " + xri);
		
		// create and add components

		if (createPages) {

			Page[] pages = new Page[7];
			
			if (this instanceof ConfigXRIIndex) pages[0] = this; else pages[0] = new ConfigXRIIndex(xri, false);
			if (this instanceof ConfigXRIServices) pages[1] = this; else pages[1] = new ConfigXRIServices(xri, false);
			if (this instanceof ConfigXRIImportService) pages[2] = this; else pages[2]  = new ConfigXRIImportService(xri, false);
			if (this instanceof ConfigXRIRefs) pages[3] = this; else pages[3] = new ConfigXRIRefs(xri, false);
			if (this instanceof ConfigXRIAddRef) pages[4] = this; else pages[4]  = new ConfigXRIAddRef(xri, false);
			if (this instanceof ConfigXRIRefs) pages[5] = this; else pages[5] = new ConfigXRIRedirects(xri, false);
			if (this instanceof ConfigXRIAddRef) pages[6] = this; else pages[6]  = new ConfigXRIAddRedirect(xri, false);
			
			for (Page page : pages) page.add(new ConfigXRIMenuPanel("configMenuPanel", pages));
		}
	}

	public Xri getQxri() {
		return (this.xri);
	}

	public void setQxri(Xri xri) {
		this.xri = xri;
	}
}
