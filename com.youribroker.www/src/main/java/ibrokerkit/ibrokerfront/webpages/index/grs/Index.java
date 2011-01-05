package ibrokerkit.ibrokerfront.webpages.index.grs;

import ibrokerkit.ibrokerfront.email.Email;
import ibrokerkit.ibrokerfront.models.GCSModel;
import ibrokerkit.ibrokerfront.models.UserInamesModel;
import ibrokerkit.ibrokerfront.validators.SubSegmentNamePatternValidator;
import ibrokerkit.ibrokerfront.webapplication.IbrokerApplication;
import ibrokerkit.ibrokerfront.webapplication.IbrokerSession;
import ibrokerkit.ibrokerfront.webpages.BasePage;
import ibrokerkit.ibrokerfront.webpages.xri.wizard.WizardXRI;
import ibrokerkit.ibrokerstore.store.User;
import ibrokerkit.iname4java.store.Xri;
import ibrokerkit.iname4java.store.XriStore;
import ibrokerkit.iname4java.store.XriStoreException;
import ibrokerkit.iname4java.store.impl.openxri.OpenxriXriData;

import java.io.StringWriter;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.wicket.Application;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public class Index extends BasePage {

	private static final long serialVersionUID = 2786725384239368435L;

	private static final int YEARS = 1;
	
	// form data

	private String gcs;
	private String iname;
	private Xri parentXri;
	private String localName;

	public Index() {

		this.setTitle(this.getString("title"));

		this.gcs = "=";

		// create and add components

		WebMarkupContainer userContainer = new WebMarkupContainer("userContainer");

		this.add(new MyForm("form", new CompoundPropertyModel(this)));
		userContainer.add(new MyForm2("form2", new CompoundPropertyModel(this)));
		this.add(userContainer);

		// if user is logged in, dont need user fields

		if (! ((IbrokerSession) this.getSession()).isLoggedIn()) {

			userContainer.setVisible(false);
		}
	}

	private class MyForm extends Form {

		private static final long serialVersionUID = -1976441444627103562L;

		// form components

		private DropDownChoice gcsDropDownChoice;
		private TextField inameTextField;

		private MyForm(String id, IModel model) {

			super(id, model);

			// create components

			GCSModel gcsModel = new GCSModel();

			this.gcsDropDownChoice = new DropDownChoice("gcs", gcsModel, (IChoiceRenderer) gcsModel);
			this.gcsDropDownChoice.setLabel(new Model("GCS"));
			this.gcsDropDownChoice.setRequired(true);
			this.inameTextField = new TextField("iname");
			this.inameTextField.setLabel(new Model("i-name"));
			this.inameTextField.setRequired(true);
			this.inameTextField.add(new SubSegmentNamePatternValidator());

			// add components

			this.add(this.gcsDropDownChoice);
			this.add(this.inameTextField);
		}

		@Override
		protected void onSubmit() {

			XriStore iname4javaStore = ((IbrokerApplication) Application.get()).getXriStore();

			Index.this.iname = Index.this.iname.toLowerCase();

			// check if the xri exists already

			try {

				boolean exists = iname4javaStore.existsXri(null, Index.this.gcs + Index.this.iname);

				if (exists) {

					this.error(Index.this.getString("exists"));
					return;
				}
			} catch (XriStoreException ex) {

				throw new RuntimeException("Problem while checking an XRI: " + ex.getMessage(), ex);
			}

			// take user to the buy page

			this.setResponsePage(new Register(Index.this.gcs + Index.this.iname));
			return;
		}
	}

	public class MyForm2 extends Form {

		private static final long serialVersionUID = -1982741444627103562L;

		// form components

		private DropDownChoice parentXriDropDownChoice;
		private TextField localNameTextField;

		public MyForm2(String id, IModel model) {

			super(id, model);

			// create components

			UserInamesModel userInamesModel = new UserInamesModel();

			this.parentXriDropDownChoice = new DropDownChoice("parentXri", userInamesModel, (IChoiceRenderer) userInamesModel);
			this.parentXriDropDownChoice.setLabel(new Model("Parent XRI"));
			this.parentXriDropDownChoice.setRequired(true);
			this.localNameTextField = new TextField("localName");
			this.localNameTextField.setLabel(new Model("i-name"));
			this.localNameTextField.setRequired(true);
			this.localNameTextField.add(new SubSegmentNamePatternValidator());

			// add components

			this.add(this.parentXriDropDownChoice);
			this.add(this.localNameTextField);
		}

		@Override
		protected void onSubmit() {

			XriStore iname4javaStore = ((IbrokerApplication) Application.get()).getXriStore();

			Index.this.localName = Index.this.localName.toLowerCase();

			// try to create the i-name

			Xri xri;
			User user;

			try {

				// check if the xri exists already

				boolean exists = iname4javaStore.existsXri(Index.this.parentXri, '*' + Index.this.localName);

				if (exists) {

					this.error(Index.this.getString("exists"));
					return;
				}

				// get logged in user

				user = ((IbrokerSession) this.getSession()).getUser();

				if (user == null) {

					throw new RuntimeException("User not logged in");
				}

				// create the xri

				OpenxriXriData xriData = new OpenxriXriData();
				xriData.setUserIdentifier(user.getIdentifier());

				xri = iname4javaStore.registerXri(Index.this.parentXri, '*' + Index.this.localName, xriData, YEARS);
			} catch (XriStoreException ex) {

				throw new RuntimeException("Problem while registering an XRI: " + ex.getMessage(), ex);
			}

			// send e-mail

			String subject = "I-Name registration successful: " + Index.this.parentXri.getFullName() + '*' + Index.this.localName;
			String to = user.getEmail();

			try {

				Email email = new Email(subject, to);
				StringWriter writer = new StringWriter();
				StringBuffer buffer;

				VelocityEngine velocity = new VelocityEngine();
				velocity.init();
				VelocityContext context = new VelocityContext(((IbrokerApplication) this.getApplication()).getProperties());
				context.put("iname", Index.this.parentXri.getFullName() + '*' + Index.this.localName);
				Template template = velocity.getTemplate(((IbrokerApplication) this.getApplication()).getWicketFilter().getFilterConfig().getServletContext().getRealPath("WEB-INF/doregister.vm"));
				template.merge(context, writer);
				buffer = writer.getBuffer();
				email.println(buffer.toString());
				email.send();
			} catch (Exception ex) {

				throw new RuntimeException("Problem while sending e-mail: " + ex.getMessage(), ex);
			}			

			// take user to the wizard

			this.setResponsePage(new WizardXRI(xri));
			return;
		}
	}

	public String getGcs() {
		return (this.gcs);
	}
	public void setGcs(String gcs) {
		this.gcs = gcs;
	}
	public String getIname() {
		return (this.iname);
	}
	public void setIname(String iname) {
		this.iname = iname;
	}
	public Xri getParentXri() {
		return (this.parentXri);
	}
	public void setParentXri(Xri parentXri) {
		this.parentXri = parentXri;
	}
	public String getLocalName() {
		return (this.localName);
	}
	public void setLocalName(String localName) {
		this.localName = localName;
	}
}
