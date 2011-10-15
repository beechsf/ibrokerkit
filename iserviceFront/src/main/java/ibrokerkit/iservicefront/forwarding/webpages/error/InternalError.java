package ibrokerkit.iservicefront.forwarding.webpages.error;

import ibrokerkit.iservicefront.components.MyVelocityPanel;
import ibrokerkit.iservicefront.forwarding.webpages.BasePage;

import org.apache.wicket.model.Model;

public class InternalError extends BasePage {

	public InternalError() {

		// extend velocity map

		this.velocityMap.put("exinternal", "1");

		this.addVelocity(new MyVelocityPanel("velocity", Model.valueOf(this.velocityMap)) {

			private static final long serialVersionUID = 2387469837463456L;

			@Override
			protected void addComponents() {

			}

			@Override
			protected String getFilename() {

				return("velocity/forwarding-error.vm");
			}
		});
	}

	@Override
	public boolean isErrorPage() {
		
		return(true);
	}
}