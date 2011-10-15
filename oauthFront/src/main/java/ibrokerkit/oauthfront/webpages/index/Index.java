package ibrokerkit.oauthfront.webpages.index;

import ibrokerkit.oauthfront.components.MyVelocityPanel;
import ibrokerkit.oauthfront.webpages.BasePage;

import org.apache.wicket.model.Model;

public class Index extends BasePage {

	private static final long serialVersionUID = 1234135715314322L;

	public Index() {

		this.addVelocity(new MyVelocityPanel("velocity", Model.valueOf(this.velocityMap)) {

			private static final long serialVersionUID = 2387469837463456L;

			@Override
			protected void addComponents() {
			}

			@Override
			protected String getFilename() {

				return("velocity/oauth-index.vm");
			}
		});
	}
}