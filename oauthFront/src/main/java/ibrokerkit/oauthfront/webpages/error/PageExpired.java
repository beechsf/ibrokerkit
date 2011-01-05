package ibrokerkit.oauthfront.webpages.error;

import ibrokerkit.oauthfront.components.MyVelocityPanel;
import ibrokerkit.oauthfront.webpages.BasePage;

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

				return("velocity/oauth-error.vm");
			}
		});
	}

	@Override
	public boolean isErrorPage() {
		
		return(true);
	}
}
