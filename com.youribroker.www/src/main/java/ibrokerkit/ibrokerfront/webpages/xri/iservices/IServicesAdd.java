package ibrokerkit.ibrokerfront.webpages.xri.iservices;

import ibrokerkit.ibrokerfront.models.IServiceTypeModel;
import ibrokerkit.ibrokerfront.webapplication.IbrokerApplication;
import ibrokerkit.ibrokerfront.webapplication.IbrokerSession;
import ibrokerkit.ibrokerstore.store.User;
import ibrokerkit.iname4java.store.Xri;
import ibrokerkit.iservicestore.store.Authentication;
import ibrokerkit.iservicestore.store.Contact;
import ibrokerkit.iservicestore.store.Forwarding;

import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.openxri.xml.AuthenticationService;
import org.openxri.xml.ContactService;
import org.openxri.xml.ForwardingService;

public class IServicesAdd extends IServices {

	private static final long serialVersionUID = -1324682475165386851L;

	private static Log log = LogFactory.getLog(IServicesAdd.class.getName());

	private FormData formData;

	public IServicesAdd(Xri xri) {

		this(xri, true);
	}

	protected IServicesAdd(Xri xri, boolean createPages) {

		super(xri, createPages);

		this.formData = new FormData();

		// create and add components

		this.add(new MyForm("form", new CompoundPropertyModel(this.formData)));
	}

	private static class FormData implements Serializable {

		private static final long serialVersionUID = -5375644378101122694L;

		private String iserviceType;
		private Boolean doXriBind;
		private Boolean doCreateSep = Boolean.TRUE;
		private Boolean doEnable = Boolean.TRUE;

		public Boolean getDoCreateSep() {
			return (this.doCreateSep);
		}
		public void setDoCreateSep(Boolean doCreateSep) {
			this.doCreateSep = doCreateSep;
		}
		public Boolean getDoEnable() {
			return (this.doEnable);
		}
		public void setDoEnable(Boolean doEnable) {
			this.doEnable = doEnable;
		}
		public Boolean getDoXriBind() {
			return (this.doXriBind);
		}
		public void setDoXriBind(Boolean doXriBind) {
			this.doXriBind = doXriBind;
		}
		public String getIserviceType() {
			return (this.iserviceType);
		}
		public void setIserviceType(String iserviceType) {
			this.iserviceType = iserviceType;
		}
	}

	private class MyForm extends Form {

		private static final long serialVersionUID = -5808951832232824837L;

		private DropDownChoice iserviceTypeDropDownChoice;
		private CheckBox doXriBindCheckBox;
		private CheckBox doCreateSepCheckBox;
		private CheckBox doEnableCheckBox;

		private MyForm(String id, IModel model) {

			super(id, model);

			// create components

			this.iserviceTypeDropDownChoice = new DropDownChoice("iserviceType", new IServiceTypeModel());
			this.iserviceTypeDropDownChoice.setRequired(true);
			this.doXriBindCheckBox = new CheckBox("doXriBind");
			this.doCreateSepCheckBox = new CheckBox("doCreateSep");
			this.doEnableCheckBox = new CheckBox("doEnable");

			// add components

			this.add(this.iserviceTypeDropDownChoice);
			this.add(this.doXriBindCheckBox);
			this.add(this.doCreateSepCheckBox);
			this.add(this.doEnableCheckBox);
		}

		@Override
		protected void onSubmit() {

			FormData formData = (FormData) this.getModelObject();

			// create which kind of i-service ?

			try {

				if (formData.iserviceType.equals("Authentication")) {

					createAuthentication(formData);
				} else if (formData.iserviceType.equals("Contact Page")) {

					createContact(formData);
				} else if (formData.iserviceType.equals("Forwarding")) {

					createForwarding(formData);
				} else {

					throw new IllegalStateException("Invalid i-service type.");
				}
			} catch (Exception ex) {

				throw new RuntimeException("Problem while creating the i-service.", ex);
			}

			// done

			IServicesAdd.this.info(IServicesAdd.this.getString("success"));
		}

		/**
		 * Creates an Authentication i-service (and SEP) according to the user input.
		 * @param formData
		 * @throws Exception
		 */
		protected void createAuthentication(FormData formData) throws Exception {

			Properties properties = ((IbrokerApplication) this.getApplication()).getProperties();
			ibrokerkit.iservicestore.store.Store iserviceStore = ((IbrokerApplication) this.getApplication()).getIserviceStore();
			User user = ((IbrokerSession) this.getSession()).getUser();

			// first the i-service

			IServicesAdd.log.debug("Creating Authentication i-service for " + IServicesAdd.this.xri.toString());

			Authentication authentication = iserviceStore.createAuthentication();
			authentication.setQxri(formData.doXriBind.equals(Boolean.TRUE) ? IServicesAdd.this.xri.toString() : IServicesAdd.this.xri.getAuthorityId()); 
			authentication.setName(IServicesAdd.this.getString("newAuthenticationName"));
			authentication.setEnabled(formData.doEnable); 
			authentication.setIndx(user.getIdentifier()); 
			authentication.setPass(user.getPass());
			iserviceStore.updateObject(authentication);

			// and then the SEP

			if (formData.doCreateSep.equals(Boolean.TRUE)) {

				IServicesAdd.log.debug("Creating Authentication SEP for " + IServicesAdd.this.xri.getAuthorityId());

				IServicesAdd.this.xri.addService(
						new AuthenticationService(
								new URI[] { new URI(properties.getProperty("authentication-service")), new URI(properties.getProperty("authentication-service-https")) },
								properties.getProperty("providerid"),
								null,
								true));
			}
		}

		/**
		 * Creates a Contact i-service (and SEP) according to the user input.
		 * @param formData
		 * @throws Exception
		 */
		protected void createContact(FormData formData) throws Exception {

			Properties properties = ((IbrokerApplication) this.getApplication()).getProperties();
			ibrokerkit.iservicestore.store.Store iserviceStore = ((IbrokerApplication) this.getApplication()).getIserviceStore();
			User user = ((IbrokerSession) this.getSession()).getUser();

			// first the i-service

			String description = IServicesAdd.this.getString("newContactDescription") + " " + user.getName();
			String forward = user.getEmail();

			IServicesAdd.log.debug("Creating Contact i-service for " + IServicesAdd.this.xri.toString() + "/" + IServicesAdd.this.xri.getAuthorityId());

			Contact contact = iserviceStore.createContact();
			contact.setQxri(formData.doXriBind.equals(Boolean.TRUE) ? IServicesAdd.this.xri.toString() : IServicesAdd.this.xri.getAuthorityId()); 
			contact.setName(IServicesAdd.this.getString("newContactName"));
			contact.setEnabled(formData.doEnable);
			contact.setIndx(user.getIdentifier()); 
			contact.setDescription(description); 
			contact.setForward(forward);
			iserviceStore.updateObject(contact);

			// and then the SEP

			if (formData.doCreateSep.equals(Boolean.TRUE)) {

				IServicesAdd.log.debug("Creating Contact SEP for " + IServicesAdd.this.xri.getAuthorityId());

				IServicesAdd.this.xri.addService(
						new ContactService(
								new URI(properties.getProperty("contact-service")),
								properties.getProperty("providerid"),
								false));
			}
		}

		/**
		 * Creates a Forwarding i-service (and SEP) according to the user input.
		 * @param formData
		 * @throws Exception
		 */
		protected void createForwarding(FormData formData) throws Exception {

			Properties properties = ((IbrokerApplication) this.getApplication()).getProperties();
			ibrokerkit.iservicestore.store.Store iserviceStore = ((IbrokerApplication) this.getApplication()).getIserviceStore();
			User user = ((IbrokerSession) this.getSession()).getUser();

			// first the i-service

			Map<String, String> mappings = new HashMap<String, String> ();

			IServicesAdd.log.debug("Creating Forwarding i-service for " + IServicesAdd.this.xri.toString() + "/" + IServicesAdd.this.xri.getAuthorityId());

			Forwarding forwarding = iserviceStore.createForwarding();
			forwarding.setQxri(formData.doXriBind.equals(Boolean.TRUE) ? IServicesAdd.this.xri.toString() : IServicesAdd.this.xri.getAuthorityId()); 
			forwarding.setName(IServicesAdd.this.getString("newForwardingName"));
			forwarding.setEnabled(formData.doEnable);
			forwarding.setIndx(user.getIdentifier()); 
			forwarding.setMappings(mappings);
			forwarding.setIndexPage(Boolean.TRUE);
			forwarding.setErrorPage(Boolean.TRUE);
			iserviceStore.updateObject(forwarding);

			// and then the SEP

			if (formData.doCreateSep.equals(Boolean.TRUE)) {

				IServicesAdd.log.debug("Creating Forwarding SEP for " + IServicesAdd.this.xri.getAuthorityId());

				IServicesAdd.this.xri.addService(
						new ForwardingService(
								new URI(properties.getProperty("forwarding-service")),
								properties.getProperty("providerid"),
								false,
								true));
			}
		}
	}
}
