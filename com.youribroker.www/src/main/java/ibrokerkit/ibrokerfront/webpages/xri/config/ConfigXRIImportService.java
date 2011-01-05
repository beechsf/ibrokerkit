package ibrokerkit.ibrokerfront.webpages.xri.config;

import ibrokerkit.ibrokerfront.models.SEPTemplatesModel;
import ibrokerkit.iname4java.store.Xri;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.StringValidator;
import org.openxri.xml.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class ConfigXRIImportService extends ConfigXRI {

	private static final long serialVersionUID = 6500296899281528276L;

	private static Log log = LogFactory.getLog(ConfigXRIImportService.class.getName());

	private static final int MAX_SEP_LENGTH = 2048;

	private MyForm form;

	public ConfigXRIImportService(Xri xri) {

		this(xri, true);
	}

	protected ConfigXRIImportService(Xri xri, boolean createPages) {

		super(xri, createPages);

		// create components

		this.form = new MyForm("form");

		// add components

		this.add(this.form);
	}

	private class MyForm extends Form {

		private static final long serialVersionUID = -4209789374800706144L;

		private String service;

		private DropDownChoice templateDropDownChoice;
		private AjaxSubmitLink loadTemplateButton;
		private TextArea sepTextArea;
		private MySubmitButton submitButton;

		private MyForm(String id) {

			super(id);
			this.setModel(new CompoundPropertyModel(this));

			// create components

			SEPTemplatesModel importSEPTemplatesModel = new SEPTemplatesModel();

			this.sepTextArea = new TextArea("service");
			this.sepTextArea.add(StringValidator.maximumLength(MAX_SEP_LENGTH));
			this.sepTextArea.setOutputMarkupId(true);
			this.templateDropDownChoice = new DropDownChoice("template", new Model(new Service()), importSEPTemplatesModel, importSEPTemplatesModel); 
			this.loadTemplateButton = new AjaxSubmitLink("loadTemplateButton", this) {

				private static final long serialVersionUID = 5452841357720755860L;

				@Override
				protected void onSubmit(AjaxRequestTarget target, Form form) {

					if (MyForm.this.templateDropDownChoice.getModelObject() == null) return;

					MyForm.this.sepTextArea.setModelObject(MyForm.this.templateDropDownChoice.getModelObject().toString());

					target.addComponent(MyForm.this.sepTextArea);
				}
			};
			this.submitButton = new MySubmitButton("submitButton");

			// add components

			this.add(this.sepTextArea);
			this.add(this.loadTemplateButton);
			this.add(this.templateDropDownChoice);
			this.add(this.submitButton);
		}

		public String getService() {
			return (this.service);
		}
		public void setService(String service) {
			this.service = service;
		}
		
		private class MySubmitButton extends Button {

			private static final long serialVersionUID = 4041036880021717274L;

			private MySubmitButton(String id) {

				super(id);
			}

			@Override
			public void onSubmit() {

				try {

					// parse service

					DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
					ByteArrayInputStream stream = new ByteArrayInputStream(MyForm.this.service.getBytes());
					Document document = builder.parse(stream);
					Element element = document.getDocumentElement();

					Service service = new Service(element);

					// add the service

					ConfigXRIImportService.this.xri.addService(service);

					MyForm.this.service = null;
				} catch (Exception ex) {

					ConfigXRIImportService.log.error(ex);
					error(getString("addfail") + ex.getLocalizedMessage());
					return;
				}

				info(ConfigXRIImportService.this.getString("success"));
				return;
			}
		}
	}
}
