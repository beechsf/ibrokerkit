package ibrokerkit.iserviceadmin.webpages.authentications;

import ibrokerkit.iserviceadmin.components.MapPanel;
import ibrokerkit.iserviceadmin.webapplication.IServiceAdminApplication;
import ibrokerkit.iserviceadmin.webpages.BasePage;
import ibrokerkit.iservicestore.store.Authentication;
import ibrokerkit.iservicestore.store.Store;
import ibrokerkit.iservicestore.store.StoreException;
import ibrokerkit.iservicestore.store.StoreUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Application;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.PageLink;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;

public class EditAuthentication extends BasePage {

	private static final long serialVersionUID = 1209589157235509150L;

	private static final Log log = LogFactory.getLog(EditAuthentication.class);

	private Authentication authentication;

	public EditAuthentication(PageParameters parameters) {

		this.setTitle(this.getString("title"));

		final Store iservice4javaStore = ((IServiceAdminApplication) Application.get()).getIserviceStore();

		try {

			this.authentication = iservice4javaStore.getAuthentication(new Long(parameters.getLong("id")));
		} catch (Exception ex) {

			EditAuthentication.log.error(ex);
			EditAuthentication.this.error(EditAuthentication.this.getString("storefail") + ex.getLocalizedMessage());
			throw new RuntimeException(ex);
		}

		// create and add components

		this.add(new EditForm("editForm", new CompoundPropertyModel(this.authentication)));
		this.add(new PageLink("backLink", ListAuthentications.class));
	}

	private class EditForm extends Form {

		private static final long serialVersionUID = 2622570754273118524L;

		public EditForm(String id, IModel model) {

			super(id, model);

			// create and add components

			this.add(new Label("id"));
			this.add(new TextField("qxri"));
			this.add(new TextField("name"));
			this.add(new CheckBox("enabled"));
			this.add(new MapPanel("attributes"));
			this.add(new TextField("indx"));
			this.add(new TextField("pass"));
			this.add(new HashPassButton("hashPass", this));
			this.add(new MySubmitButton("submit"));
			
			this.setOutputMarkupId(true);
		}

		private class MySubmitButton extends Button {
			
			private static final long serialVersionUID = -7400825981616897313L;

			private MySubmitButton(String id) {
				
				super(id);
			}

			@Override
			public void onSubmit() {

				final Store iservice4javaStore = ((IServiceAdminApplication) this.getApplication()).getIserviceStore();

				// update authentication

				try {

					iservice4javaStore.updateObject(EditAuthentication.this.authentication);
				} catch (StoreException ex) {

					EditAuthentication.log.error(ex);
					EditAuthentication.this.error(EditAuthentication.this.getString("storefail") + ex.getLocalizedMessage());
					return;
				}

				// go to list page

				this.setResponsePage(ListAuthentications.class);
			}
		}
		
		private class HashPassButton extends AjaxSubmitLink {

			private static final long serialVersionUID = -6865838104330285029L;

			private HashPassButton(String id, Form form) {

				super(id, form);
			}

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form form) {

				String pass = EditAuthentication.this.authentication.getPass();

				EditAuthentication.this.authentication.setPass(StoreUtil.hashPass(pass));

				if (target != null) target.addComponent(EditForm.this);
			}
		}
	}
}
