package ibrokerkit.iservicefront.locator.webpages.error;

import ibrokerkit.iservicefront.components.MyVelocityPanel;
import ibrokerkit.iservicefront.locator.webpages.BasePage;

import org.apache.wicket.model.Model;

public class PageExpired extends BasePage {

	private static final long serialVersionUID = 128147889278304791L;

	public PageExpired() {

		// extend velocity map

		this.velocityMap.put("exexpired", "1");

		this.addVelocity(new MyVelocityPanel("velocity", Model.valueOf(this.velocityMap)) {

			private static final long serialVersionUID = 2387469837463456L;

			@Override
			protected void addComponents() {

			}

			@Override
			protected String getFilename() {

				return("velocity/locator-error.vm");
			}
		});
	}

	@Override
	public boolean isErrorPage() {
		
		return(true);
	}
}
