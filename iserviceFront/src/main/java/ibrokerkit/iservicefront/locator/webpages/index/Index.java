package ibrokerkit.iservicefront.locator.webpages.index;

import ibrokerkit.iservicefront.components.MyVelocityPanel;
import ibrokerkit.iservicefront.locator.webpages.BasePage;

import org.apache.wicket.model.Model;

public class Index extends BasePage {

	private static final long serialVersionUID = 4142812570268738L;

	public Index() {

		this.addVelocity(new MyVelocityPanel("velocity", Model.valueOf(this.velocityMap)) {

			private static final long serialVersionUID = 2387469837463456L;

			@Override
			protected void addComponents() {

			}

			@Override
			protected String getFilename() {

				return("velocity/locator-index.vm");
			}
		});
	}
}
