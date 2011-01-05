package ibrokerkit.oauthfront.webpages.error;

import ibrokerkit.oauthfront.components.MyVelocityPanel;
import ibrokerkit.oauthfront.webpages.BasePage;

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

				return("velocity/oauth-error.vm");
			}
		});
	}

	@Override
	public boolean isErrorPage() {
		
		return(true);
	}
}
