package ibrokerkit.iservicefront.contact.webpages.iservice;

import ibrokerkit.iservicefront.behaviors.DefaultFocusBehavior;
import ibrokerkit.iservicefront.components.MyVelocityPanel;
import ibrokerkit.iservicefront.components.openid.OpenIDPanel;
import ibrokerkit.iservicefront.contact.email.Email;
import ibrokerkit.iservicefront.contact.webapplication.ContactApplication;
import ibrokerkit.iservicefront.contact.webpages.BasePage;
import ibrokerkit.iservicestore.store.Contact;

import java.util.Properties;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Application;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebResponse;
import org.openxri.XRI;
import org.openxri.xml.AuthenticationService;
import org.openxri.xml.Service;
import org.openxri.xml.XRD;

public class ContactPage extends BasePage implements IHeaderContributor {

	private static final long serialVersionUID = 896701525596879770L;

	private static Log log = LogFactory.getLog(ContactPage.class.getName());

	private Contact contact;
	private XRI qxri;
	private XRD xrd;

	public ContactPage(Contact contact, XRI qxri, XRD xrd) {

		this.contact = contact;
		this.qxri = qxri;
		this.xrd = xrd;

		// extend velocity map

		this.velocityMap.put("contact", this.contact);
		this.velocityMap.put("qxri", this.qxri.getAuthorityPath().toString());
		
		if (this.xrd != null && this.xrd.getCanonicalID() != null)
			this.velocityMap.put("inumber", xrd.getCanonicalID().getValue());

		this.addVelocity(new MyVelocityPanel("velocity", Model.valueOf(this.velocityMap)) {

			private static final long serialVersionUID = 2387469837463456L;

			@Override
			protected void addComponents() {

				this.add(new FeedbackPanel("feedbackPanel"));
				this.add(new ContactForm("contactForm"));
			}

			@Override
			protected String getFilename() {

				return("velocity/contact-contact.vm");
			}
		});
	}

	@Override
	protected void setHeaders(WebResponse response) {

		super.setHeaders(response);

		String xri;
		
		if (this.xrd != null && this.xrd.getCanonicalID() != null)
			xri = this.xrd.getCanonicalID().getValue();
		else
			xri = this.qxri.toString();

		response.setHeader("Link", "<http://xri2xrd.net/" + xri + ">; rel=\"lrdd\"; type=\"application/xrd+xml\"");
		response.setHeader("X-XRDS-Location", "http://xri.net/" + xri + "?_xrd_r=application/xrds+xml;sep=false");
	}

	@SuppressWarnings("unchecked")
	@Override
	public void renderHead(IHeaderResponse response) {

		Properties properties = ((ContactApplication) Application.get()).getProperties();

		// check OpenID delegation

		String openidServer = null;
		String openidDelegate = null;

		if (this.xrd != null) {

			Vector<Service> services = this.xrd.getServices();

			for (Service service : services) {

				if (AuthenticationService.isInstance(service) && service.getNumLocalIDs() > 0 && service.getNumURIs() > 0) {

					openidServer = service.getURIAt(0).getUriString();
					openidDelegate = service.getLocalIDAt(0).getValue();
					break;
				}
			}
		}

		if (openidServer == null ) openidServer = properties.getProperty("authentication-endpoint-url");
		if (openidDelegate == null) openidDelegate = (this.xrd != null && this.xrd.getCanonicalID() != null) ? this.xrd.getCanonicalID().getValue() : this.qxri.getAuthorityPath().toString();

		// insert OpenID delegation tags

		response.renderString("<title>" + this.qxri.getAuthorityPath().toString() + "</title>\n");
		response.renderString("<link rel=\"openid.server\" href=\"" + openidServer + "\" />\n");
		response.renderString("<link rel=\"openid2.provider\" href=\"" + openidServer + "\" />\n");
		response.renderString("<link rel=\"openid.delegate\" href=\"http://xri.net/" + openidDelegate + "\" />\n");
		response.renderString("<link rel=\"openid2.local_id\" href=\"http://xri.net/" + openidDelegate + "\" />\n");
	}

	private class ContactForm extends Form {

		private static final long serialVersionUID = -6751587043037991248L;

		private String sender;
		private String openid;
		private String senderEmail;
		private String message;

		private TextField senderTextField;
		private OpenIDPanel openidPanel;
		private TextField senderEmailTextField;
		private TextArea messageTextArea;
		private Button sendButton;

		private ContactForm(String id) {

			super(id);

			this.setModel(new CompoundPropertyModel(this));

			// create components

			this.senderTextField = new TextField("sender");
			this.senderTextField.setLabel(new Model("Sender"));
			this.senderTextField.setRequired(true);
			this.senderTextField.add(new DefaultFocusBehavior());
			this.openidPanel = new OpenIDPanel("openid");
			this.senderEmailTextField = new TextField("senderEmail");
			this.senderEmailTextField.setLabel(new Model("E-Mail"));
			this.messageTextArea = new TextArea("message");
			this.messageTextArea.setLabel(new Model("Message"));
			this.sendButton = new SendButton("sendButton");

			// add components

			this.add(this.senderTextField);
			this.add(this.openidPanel);
			this.add(this.senderEmailTextField);
			this.add(this.messageTextArea);
			this.add(this.sendButton);
		}

		private class SendButton extends Button {

			private static final long serialVersionUID = 2873648932764932L;

			public SendButton(String id) {

				super(id);
			}

			@Override
			public void onSubmit() {

				// check if sender is authenticated

				if (ContactForm.this.openid != null && ! ContactForm.this.openidPanel.isAuthenticated()) {

					ContactPage.this.error(ContactPage.this.getString("authenticate"));
					return;
				}

				// send mail

				Properties properties = ((ContactApplication) Application.get()).getProperties();

				String subject = "Contact Page: Message from " + ContactForm.this.sender;
				String from = ContactForm.this.senderEmail != null ? ContactForm.this.senderEmail : properties.getProperty("contact-from");
				String to = ContactPage.this.contact.getForward();
				String server = properties.getProperty("contact-server");

				if (to == null || to.trim().equals("")) {

					this.error(ContactPage.this.getString("no-forward", null));
					return;
				}

				try {

					Email email = new Email(subject, from, to, server);

					if (ContactForm.this.senderEmail != null) email.println("Sender E-Mail: " + ContactForm.this.senderEmail);
					if (ContactForm.this.openid != null) email.println("Sender I-Name: " + ContactForm.this.openid);
					if (ContactForm.this.openid != null) email.println("Sender Contact Page: http://xri.net/" + ContactForm.this.openid + "/(+contact)");
					email.println();
					email.println(ContactForm.this.message);

					email.send();

					this.info(ContactPage.this.getString("success", null));
				} catch (Exception ex) {

					log.fatal(ex);
					this.error(ContactPage.this.getString("sorry", null));
					return;
				}
			}
		}

		public String getMessage() {
			return (this.message);
		}
		public void setMessage(String message) {
			this.message = message;
		}
		public String getSender() {
			return (this.sender);
		}
		public void setSender(String sender) {
			this.sender = sender;
		}
		public String getSenderEmail() {
			return (this.senderEmail);
		}
		public void setSenderEmail(String senderEmail) {
			this.senderEmail = senderEmail;
		}
		public String getOpenid() {
			return (this.openid);
		}
		public void setOpenid(String openid) {
			this.openid = openid;
		}
	}
}
