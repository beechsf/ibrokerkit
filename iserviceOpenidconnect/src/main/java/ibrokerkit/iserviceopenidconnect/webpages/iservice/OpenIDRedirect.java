package ibrokerkit.iserviceopenidconnect.webpages.iservice;

import ibrokerkit.iservicefront.authentication.webpages.BasePage;
import ibrokerkit.iservicefront.components.MyVelocityPanel;

import org.apache.wicket.model.Model;
import org.openid4java.message.Message;


public class OpenIDRedirect extends BasePage {

	private static final long serialVersionUID = 4616482566114455444L;

	private Message message;

	public OpenIDRedirect(Message message) {

		this.message = message;

		// extend velocity map

		this.velocityMap.put("message", this.message);

		this.addVelocity(new MyVelocityPanel("velocity", Model.valueOf(this.velocityMap)) {

			private static final long serialVersionUID = 2387469837463456L;

			@Override
			protected void addComponents() {

			}

			@Override
			protected String getFilename() {

				return("velocity/authentication-redirect.vm");
			}
		});
	}
}
