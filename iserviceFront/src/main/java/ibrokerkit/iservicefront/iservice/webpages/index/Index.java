package ibrokerkit.iservicefront.iservice.webpages.index;

import ibrokerkit.iservicefront.components.MyVelocityPanel;
import ibrokerkit.iservicefront.iservice.webpages.BasePage;

import org.apache.wicket.model.Model;

public class Index extends BasePage {

	private static final long serialVersionUID = 1234135715314322L;

	public Index() {

		this.addVelocity(new MyVelocityPanel("velocity", Model.of(this.velocityMap)) {

			private static final long serialVersionUID = 2387469837463456L;

			@Override
			protected void addComponents() {
			}

			@Override
			protected String getFilename() {

				return("velocity/iservice-index.vm");
			}
		});
	}
}
