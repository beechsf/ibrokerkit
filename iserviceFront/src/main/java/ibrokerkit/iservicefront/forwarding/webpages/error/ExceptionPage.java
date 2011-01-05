package ibrokerkit.iservicefront.forwarding.webpages.error;

import ibrokerkit.iservicefront.components.MyVelocityPanel;
import ibrokerkit.iservicefront.forwarding.webpages.BasePage;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.model.Model;


public class ExceptionPage extends BasePage {

	private static final long serialVersionUID = -4234514867813143871L;

	public ExceptionPage(RequestCycle requestCycle, Exception ex) {

		// extend velocity map

		this.velocityMap.put("exexception", "1");
		
		StringWriter writer = new StringWriter();
		ex.printStackTrace(new PrintWriter(writer));

		this.velocityMap.put("exclass", ex.getClass().getSimpleName());
		this.velocityMap.put("extime", new Date(requestCycle.getStartTime()).toString());
		this.velocityMap.put("expath", requestCycle.getRequest().getPath());
		this.velocityMap.put("exmessage", ex.getMessage());
		this.velocityMap.put("extrace", writer.toString());

		this.addVelocity(new MyVelocityPanel("velocity", Model.valueOf(this.velocityMap)) {

			private static final long serialVersionUID = 2387469837463456L;

			@Override
			protected void addComponents() {

			}

			@Override
			protected String getFilename() {

				return("velocity/forwarding-error.vm");
			}
		});
	}

	@Override
	public boolean isErrorPage() {

		return(true);
	}
}
