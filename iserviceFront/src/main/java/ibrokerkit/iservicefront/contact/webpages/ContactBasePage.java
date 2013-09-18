package ibrokerkit.iservicefront.contact.webpages;

import ibrokerkit.iservicefront.IserviceApplication;
import ibrokerkit.iservicefront.components.MyVelocityContributor;
import ibrokerkit.iservicefront.components.MyVelocityPanel;
import ibrokerkit.iservicefront.velocity.Encoder;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebApplication;

public abstract class ContactBasePage extends WebPage {

	private static final long serialVersionUID = -979014773165248071L;

	protected HashMap<String, Object> velocityMap;

	public ContactBasePage() {

		// create velocity map

		Properties properties = ((IserviceApplication) this.getApplication()).getProperties();

		this.velocityMap = new HashMap<String, Object> ();
		for (Map.Entry<Object, Object> entry : properties.entrySet()) this.velocityMap.put((String) entry.getKey(), entry.getValue());
		this.velocityMap.put("encoder", Encoder.getInstance());
	}

	protected void addVelocity(final MyVelocityPanel subVelocityPanel) {

		// create and add components

		this.add(new MyVelocityContributor(Model.of(this.velocityMap)) {

			private static final long serialVersionUID = -8434169146399445954L;

			@Override
			protected Reader getTemplateReader() throws IOException {

				String file = "velocity/contact-head.vm";
				String path = ((WebApplication) ContactBasePage.this.getApplication()).getWicketFilter().getFilterConfig().getServletContext().getRealPath(file);
				return(new FileReader(new File(path)));
			}
		});

		this.add(new MyVelocityPanel("velocity", Model.of(this.velocityMap)) {

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
