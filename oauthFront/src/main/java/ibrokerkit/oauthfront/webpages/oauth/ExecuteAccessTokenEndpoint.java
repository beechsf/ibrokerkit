package ibrokerkit.oauthfront.webpages.oauth;


import ibrokerkit.ibrokerstore.store.User;
import ibrokerkit.iname4java.store.Xri;
import ibrokerkit.oauthfront.email.Email;
import ibrokerkit.oauthfront.oauth.OauthSupport;
import ibrokerkit.oauthfront.oauth.UserInput;
import ibrokerkit.oauthfront.oauth.UserInputAddService;
import ibrokerkit.oauthfront.webapplication.OauthApplication;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.oauth.OAuthAccessor;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import net.oauth.server.OAuthServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.wicket.Application;
import org.apache.wicket.IRequestTarget;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;
import org.openxri.xml.Service;

public class ExecuteAccessTokenEndpoint implements IRequestTarget {

	private static Log log = LogFactory.getLog(ExecuteAccessTokenEndpoint.class.getName());

	private Map<String, String> parameters;

	public ExecuteAccessTokenEndpoint(Map<String, String> parameters) {

		this.parameters = parameters;
	}

	public void respond(RequestCycle requestCycle) {

		Properties properties = ((OauthApplication) Application.get()).getProperties();

		try {

			// read and validate the OAuth message

			String url = properties.getProperty("executerequesttoken-endpoint-url");
			OAuthMessage requestMessage = OAuthServlet.getMessage(((WebRequest) requestCycle.getRequest()).getHttpServletRequest(), url);
			OAuthAccessor oAuthAccessor = OauthSupport.getAccessor(requestMessage);

			log.info("ExecuteAccessTokenEndpoint: Got oAuth message: " + requestMessage.toString());

			OauthSupport.getValidator().validateMessage(requestMessage, oAuthAccessor);

			// make sure token is authorized

			if (! Boolean.TRUE.equals(oAuthAccessor.getProperty("authorized"))) {

				throw new OAuthProblemException("permission_denied");
			}

			// execute the operation

			String xriOperation = (String) oAuthAccessor.getProperty(OauthSupport.PROPERTY_XRI_OPERATION);
			String xriIname = (String) oAuthAccessor.getProperty(OauthSupport.PROPERTY_XRI_INAME);
			Service xriService = (Service) oAuthAccessor.getProperty(OauthSupport.PROPERTY_XRI_SERVICE);

			Xri xri = (Xri) oAuthAccessor.getProperty(OauthSupport.PROPERTY_XRI);
			User user = (User) oAuthAccessor.getProperty(OauthSupport.PROPERTY_USER);
			UserInput userInput = (UserInput) oAuthAccessor.getProperty(OauthSupport.PROPERTY_USERINPUT);

			if (xriOperation.equals(OauthSupport.XRI_OPERATION_ADD_SERVICE)) {

				log.info("Executing " + OauthSupport.XRI_OPERATION_ADD_SERVICE + " operation.");

				UserInputAddService userInputAddService = (UserInputAddService) userInput;
				List<Service> removeConflictingServices = userInputAddService.getRemoveConflictingServices();

				if (removeConflictingServices != null && removeConflictingServices.size() > 0) {

					for (Service removeConflictingService : removeConflictingServices) {

						xri.deleteService(removeConflictingService);
					}
				}

				xri.addService(xriService);
			}

			// invalidate this accessor

			OauthSupport.invalidateAccessor(oAuthAccessor);

			// send e-mail

			this.sendEmail(user.getEmail(), xriOperation, xriIname, xriService);
		} catch (Exception ex) {

			log.error(ex.getMessage(), ex);

			try {

				OauthSupport.handleException(ex, ((WebRequest) requestCycle.getRequest()).getHttpServletRequest(), ((WebResponse) requestCycle.getResponse()).getHttpServletResponse(), true);
			} catch (Exception ex2) {

				throw new RuntimeException(ex2);
			}
		}
	}

	public Map<String, String> getParameters() {
		return (this.parameters);
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	public void detach(RequestCycle requestCycle) {

	}

	private void sendEmail(String to, String xriOperation, String xriIname, Service xriService) {

		Properties properties = ((OauthApplication) Application.get()).getProperties();

		// send e-mail

		String subject = "I-Name configuration updated";
		String from = properties.getProperty("email-from");
		String server = properties.getProperty("email-server");

		try {

			StringWriter writer = new StringWriter();
			StringBuffer buffer;

			VelocityContext context = new VelocityContext(((OauthApplication) Application.get()).getProperties());
			context.put("operation", xriOperation);
			context.put("iname", xriIname);
			context.put("service", xriService);

			Reader templateReader = new FileReader(new File(((WebApplication) Application.get()).getServletContext().getRealPath("WEB-INF/doupdate.vm")));

			Velocity.evaluate(context, writer, null, templateReader);
			templateReader.close();
			buffer = writer.getBuffer();

			Email email = new Email(subject, from, to, server);
			email.println(buffer.toString());
			email.send();
		} catch (Exception ex) {

			log.error(ex.getMessage(), ex);
		}			
	}
}
