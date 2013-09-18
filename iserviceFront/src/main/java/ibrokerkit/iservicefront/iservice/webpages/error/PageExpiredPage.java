package ibrokerkit.iservicefront.iservice.webpages.error;

import ibrokerkit.iservicefront.components.MyVelocityPanel;
import ibrokerkit.iservicefront.iservice.webpages.BasePage;

import org.apache.wicket.model.Model;

public class PageExpiredPage extends BasePage {

	private static final long serialVersionUID = 128147889278304791L;

	public PageExpiredPage() {

		// extend velocity map

		this.velocityMap.put("exexpired", "1");

		this.addVelocity(new MyVelocityPanel("velocity", Model.of(this.velocityMap)) {

			private static final long serialVersionUID = 2387469837463456L;

			@Override
			protected void addComponents() {

			}

			@Override
			protected String getFilename() {

				return("velocity/iservice-error.vm");
			}
		});
	}

	@Override
	public boolean isErrorPage() {
		
		return(true);
	}
}
