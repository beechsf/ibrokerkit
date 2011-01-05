package ibrokerkit.iservicefront.forwarding.webpages.iservice;

import ibrokerkit.iservicefront.components.MyVelocityPanel;
import ibrokerkit.iservicefront.forwarding.webpages.BasePage;

import org.apache.wicket.PageParameters;
import org.apache.wicket.model.Model;


public class CustomPage extends BasePage {

	private static final long serialVersionUID = 2356737581478887832L;

	private String name;
	
	public CustomPage(PageParameters pageParameters) {

		// read custom velocity name from page parameters
		
		this.name = pageParameters.getString("name");

		this.addVelocity(new MyVelocityPanel("velocity", Model.valueOf(this.velocityMap)) {

			private static final long serialVersionUID = 2387469837463456L;

			@Override
			protected void addComponents() {

			}

			@Override
			protected String getFilename() {

				return("velocity/" + CustomPage.this.name + ".vm");
			}
		});
	}
}
