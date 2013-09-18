package ibrokerkit.iservicefront.iservice.webpages;

import ibrokerkit.iservicefront.components.MyVelocityPanel;
import ibrokerkit.iservicefront.contact.webpages.ContactBasePage;

import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;


public class CustomPage extends ContactBasePage {

	private static final long serialVersionUID = 2356737581478887832L;

	private String name;

	public CustomPage(PageParameters pageParameters) {

		// read custom velocity name from page parameters

		this.name = pageParameters.get("name").toString();

		this.addVelocity(new MyVelocityPanel("velocity", Model.of(this.velocityMap)) {

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
