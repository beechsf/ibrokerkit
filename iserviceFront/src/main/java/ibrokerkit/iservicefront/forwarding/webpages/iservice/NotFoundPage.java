package ibrokerkit.iservicefront.forwarding.webpages.iservice;

import ibrokerkit.iservicefront.components.MyVelocityPanel;
import ibrokerkit.iservicefront.forwarding.webpages.ForwardingBasePage;

import org.apache.wicket.model.Model;
import org.openxri.XRI;


public class NotFoundPage extends ForwardingBasePage {

	private static final long serialVersionUID = 2356737581478887832L;

	public NotFoundPage(XRI qxri) {

		// extend velocity map

		this.velocityMap.put("qxri", qxri.getAuthorityPath().toString());

		this.addVelocity(new MyVelocityPanel("velocity", Model.of(this.velocityMap)) {

			private static final long serialVersionUID = 2387469837463456L;

			@Override
			protected void addComponents() {

			}

			@Override
			protected String getFilename() {

				return("velocity/forwarding-notfound.vm");
			}
		});
	}
}
