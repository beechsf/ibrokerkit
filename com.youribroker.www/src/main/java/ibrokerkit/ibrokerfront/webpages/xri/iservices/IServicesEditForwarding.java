package ibrokerkit.ibrokerfront.webpages.xri.iservices;

import ibrokerkit.ibrokerfront.webapplication.IbrokerApplication;
import ibrokerkit.iname4java.store.Xri;
import ibrokerkit.iservicestore.store.Forwarding;
import ibrokerkit.iservicestore.store.Store;
import ibrokerkit.iservicestore.store.StoreException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;


public class IServicesEditForwarding extends IServices {

	private static final long serialVersionUID = -6375273237102206350L;

	public IServicesEditForwarding(Xri xri, Forwarding forwarding) {

		this(xri, true, forwarding);
	}

	protected IServicesEditForwarding(Xri xri, boolean createPages, Forwarding forwarding) {

		super(xri, createPages);

		// create and add components

		this.add(new EditForm("editForm", new CompoundPropertyModel(forwarding)));
	}

	private class EditForm extends Form {

		private static final long serialVersionUID = -4581198296980324816L;

		private TextField nameTextField;
		private CheckBox enabledCheckBox;
		private CheckBox indexPageCheckBox;
		private CheckBox errorPageCheckBox;
		private WebMarkupContainer mappingsContainer;
		private ListView mappingsListView;
		private AjaxSubmitLink addMappingLink;
		private MySubmitButton submitButton;

		private EditForm(String id, IModel model) {

			super(id, model);

			// create components

			final Forwarding forwarding = (Forwarding) this.getModelObject();

			this.nameTextField = new TextField("name");
			this.enabledCheckBox = new CheckBox("enabled");
			this.indexPageCheckBox = new CheckBox("indexPage");
			this.errorPageCheckBox = new CheckBox("errorPage");
			this.mappingsContainer = new WebMarkupContainer("mappingsContainer");
			this.mappingsContainer.setOutputMarkupId(true);
			this.mappingsListView = new ListView("mappings", Mapping.fromMap(forwarding.getMappings())) {

				private static final long serialVersionUID = 8315028903141005038L;

				@SuppressWarnings("unchecked")
				@Override
				protected void populateItem(final ListItem item) {

					final List<Mapping> mappingsList = (List<Mapping>) EditForm.this.mappingsListView.getModelObject();
					final Mapping mapping = (Mapping) item.getModelObject();

					item.setModel(new CompoundPropertyModel(mapping));

					item.add(new TextField("key"));
					item.add(new TextField("value"));
					item.add(new AjaxSubmitLink("delete", EditForm.this) {

						private static final long serialVersionUID = 3915770117216790715L;

						@Override
						public void onSubmit(AjaxRequestTarget target, Form form) {

							mappingsList.remove(mapping);
							if (target != null) target.addComponent(EditForm.this.mappingsContainer);
						}
					});
				}
			};
			this.addMappingLink = new AjaxSubmitLink("addMapping", this) {

				private static final long serialVersionUID = -169650385945940755L;

				@SuppressWarnings("unchecked")
				@Override
				public void onSubmit(AjaxRequestTarget target, Form form) {

					final List<Mapping> mappingsList = (List<Mapping>) EditForm.this.mappingsListView.getModelObject();

					mappingsList.add(new Mapping());
					if (target != null) target.addComponent(EditForm.this.mappingsContainer);
				}
			};
			this.submitButton = new MySubmitButton("submitButton");

			this.mappingsContainer.add(this.mappingsListView);
			this.mappingsContainer.add(this.addMappingLink);

			// add components

			this.add(this.nameTextField);
			this.add(this.enabledCheckBox);
			this.add(this.indexPageCheckBox);
			this.add(this.errorPageCheckBox);
			this.add(this.mappingsContainer);
			this.add(this.submitButton);
		}

		private class MySubmitButton extends Button {

			private static final long serialVersionUID = -521489352056776236L;

			private MySubmitButton(String id) {

				super(id);
			}

			@Override
			@SuppressWarnings("unchecked")
			public void onSubmit() {

				Store store = ((IbrokerApplication) Application.get()).getIserviceStore();
				Forwarding forwarding = (Forwarding) EditForm.this.getModelObject();

				try {

					// update the forwarding i-service

					forwarding.setMappings(Mapping.toMap((List<Mapping>) EditForm.this.mappingsListView.getModelObject()));
					store.updateObject(forwarding);

					// and go back to the i-services list

					Page page = new IServicesListForwardings(IServicesEditForwarding.this.xri);
					page.info(IServicesEditForwarding.this.getString("success"));
					this.setResponsePage(page);
				} catch (StoreException ex) {

					throw new RuntimeException("Problem while updating the Forwarding i-service.", ex);
				}
			}
		}
	}

	private static class Mapping implements Serializable {

		private static final long serialVersionUID = 3600765882640037083L;

		private String key;
		private String value;

		public Mapping() {
			this.key = "";
			this.value = "";
		}

		public Mapping(String path, String uri) {
			this.key = path;
			this.value = uri;
		}

		public String getKey() {
			return (this.key);
		}
		public void setKey(String key) {
			this.key = key;
			if (this.key != null) while (this.key.startsWith("/")) this.key = this.key.substring(1);
			if (this.key == null) this.key = "";
		}
		public String getValue() {
			return (this.value);
		}
		public void setValue(String value) {
			this.value = value;
		}

		@Override
		public boolean equals(Object o) {

			if (o == null) return(false);
			if (o == this) return(true);

			return(this.key.equals(((Mapping) o).key) && this.value.equals(((Mapping) o).value));
		}

		@Override
		public int hashCode() {

			return(this.key.hashCode() * this.value.hashCode());
		}

		public static Map<String, String> toMap(List<Mapping> mappingsList) {

			Map<String, String> mappings = new HashMap<String, String> ();
			for (Mapping mapping : mappingsList) mappings.put(mapping.getKey(), mapping.getValue());
			return(mappings);
		}

		public static List<Mapping> fromMap(Map<String, String> mappings) {

			List<Mapping> mappingsList = new ArrayList<Mapping> ();
			for (String key : mappings.keySet()) mappingsList.add(new Mapping(key, mappings.get(key)));
			return(mappingsList);
		}
	}
}
