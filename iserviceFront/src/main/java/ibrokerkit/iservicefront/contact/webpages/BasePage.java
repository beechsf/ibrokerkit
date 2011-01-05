package ibrokerkit.iservicefront.contact.webpages;

import ibrokerkit.iservicefront.components.MyVelocityContributor;
import ibrokerkit.iservicefront.components.MyVelocityPanel;
import ibrokerkit.iservicefront.contact.webapplication.ContactApplication;
import ibrokerkit.iservicefront.velocity.Encoder;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.util.resource.IStringResourceStream;

public abstract class BasePage extends WebPage {

	protected Map<String, Object> velocityMap;

	public BasePage() {

		// create velocity map

		Properties properties = ((ContactApplication) this.getApplication()).getProperties();

		this.velocityMap = new HashMap<String, Object> ();
		for (Map.Entry<Object, Object> entry : properties.entrySet()) this.velocityMap.put((String) entry.getKey(), entry.getValue());
		this.velocityMap.put("encoder", Encoder.getInstance());
	}

	protected void addVelocity(final MyVelocityPanel subVelocityPanel) {

		// create and add components

		this.add(new MyVelocityContributor(Model.valueOf(this.velocityMap)) {

			private static final long serialVersionUID = -8434169146399445954L;

			@Override
			protected IStringResourceStream getTemplateResource() {

				String file = "velocity/contact-head.vm";
				String path = ((WebApplication) BasePage.this.getApplication()).getWicketFilter().getFilterConfig().getServletContext().getRealPath(file);
				return(new FileResourceStream(new File(path)));
			}
		});

		this.add(new MyVelocityPanel("velocity", Model.valueOf(this.velocityMap)) {

			private static final long serialVersionUID = 2387469837463456L;

			@Override
			protected void addComponents() {

				this.add(subVelocityPanel);
			}

			@Override
			protected String getFilename() {

				return("velocity/contact-base.vm");
			}
		});
	}
}
