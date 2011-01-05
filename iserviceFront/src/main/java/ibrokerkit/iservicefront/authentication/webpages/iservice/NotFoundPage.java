package ibrokerkit.iservicefront.authentication.webpages.iservice;

import ibrokerkit.iservicefront.authentication.webpages.BasePage;
import ibrokerkit.iservicefront.components.MyVelocityPanel;

import org.apache.wicket.model.Model;
import org.openxri.XRI;


public class NotFoundPage extends BasePage {

	private static final long serialVersionUID = 2356737581478887832L;

	public NotFoundPage(XRI qxri) {

		// extend velocity map

		this.velocityMap.put("qxri", qxri.getAuthorityPath().toString());

		this.addVelocity(new MyVelocityPanel("velocity", Model.valueOf(this.velocityMap)) {

			private static final long serialVersionUID = 2387469837463456L;

			@Override
			protected void addComponents() {

			}

			@Override
			protected String getFilename() {

				return("velocity/authentication-notfound.vm");
			}
		});
	}
}
