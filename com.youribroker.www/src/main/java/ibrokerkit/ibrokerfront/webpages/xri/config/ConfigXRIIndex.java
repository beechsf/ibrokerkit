package ibrokerkit.ibrokerfront.webpages.xri.config;

import ibrokerkit.ibrokerfront.models.XriEquivIDsModel;
import ibrokerkit.iname4java.store.Xri;
import ibrokerkit.iname4java.store.XriStoreException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.openxri.xml.CanonicalEquivID;
import org.openxri.xml.EquivID;



public class ConfigXRIIndex extends ConfigXRI {

	private static final long serialVersionUID = 2341784575702244317L;

	private static Log log = LogFactory.getLog(ConfigXRIIndex.class.getName());

	private EquivID equivId;
	private CanonicalEquivID canonicalEquivId;
	private String extension;

	private XriEquivIDsModel authorityEquivIDsModel;
	private ListView authorityEquivIDsListView;
	private MyForm form;
	private MyForm2 form2;
	private MyForm3 form3;

	public ConfigXRIIndex(Xri xri) {

		this(xri, true);
	}

	protected ConfigXRIIndex(Xri xri, boolean createPages) {

		super(xri, createPages);

		this.authorityEquivIDsModel = new XriEquivIDsModel(this.xri);

		// create and add components

		try {

			this.equivId = new EquivID("xri://");
			this.canonicalEquivId = this.xri.getCanonicalEquivID() != null ? new CanonicalEquivID(this.xri.getCanonicalEquivID()) : new CanonicalEquivID();
			this.extension = this.xri.getExtension();

			String canonicalId = this.xri.getCanonicalID() != null ? this.xri.getCanonicalID().getValue() : "";
			int serviceCount = this.xri.getServices().size();
			int refCount = this.xri.getRefs().size();
			int redirectCount = this.xri.getRedirects().size();

			this.add(new Label("canonicalId", canonicalId));
			this.add(new Label("serviceCount", Integer.toString(serviceCount)));
			this.add(new Label("refCount", Integer.toString(refCount)));
			this.add(new Label("redirectCount", Integer.toString(redirectCount)));
		} catch (XriStoreException ex) {

			throw new RuntimeException("Cannot read XRI details: " + ex.getMessage(), ex);
		}

		this.add(new ListView("aliases", xri.getAliases()) {

			private static final long serialVersionUID = -678931105770312211L;

			@Override
			protected void populateItem(ListItem item) {

				item.add(new Label("alias", item.getModelObjectAsString()));
			}
		});

		this.authorityEquivIDsListView = new ListView("equivIds", this.authorityEquivIDsModel) {

			private static final long serialVersionUID = -8849142156157353378L;

			@Override
			protected void populateItem(ListItem item) {

				final EquivID equivId = (EquivID) item.getModelObject();
				String value = equivId.getValue() != null ? equivId.getValue() : "-";
				String priority = equivId.getPriority() == null ? "" : "[PRIORITY=" + equivId.getPriority().toString() + "]";

				item.add(new DeleteLink("deleteButton", equivId));
				item.add(new Label("nr", Integer.toString(item.getIndex() + 1)));
				item.add(new Label("value", value));
				item.add(new Label("priority", priority));
			}
		};
		this.authorityEquivIDsListView.setOutputMarkupId(true);
		this.add(this.authorityEquivIDsListView);

		this.form = new MyForm("form", new CompoundPropertyModel(this.equivId));
		this.form.setOutputMarkupId(true);
		this.add(this.form);

		this.form2 = new MyForm2("form2", new CompoundPropertyModel(this.canonicalEquivId));
		this.add(this.form2);

		this.form3 = new MyForm3("form3", new CompoundPropertyModel(this));
		this.add(this.form3);

		PageParameters parameters = new PageParameters();

		parameters.put("xri", xri.toString());
	}

	private class DeleteLink extends Link {

		private static final long serialVersionUID = 616746135857570401L;

		private EquivID equivId;

		private DeleteLink(String id, EquivID equivId) {

			super(id);

			this.equivId = equivId;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void onClick() {

			// delete EquivID

			try {

				ConfigXRIIndex.this.xri.deleteEquivID(this.equivId);
			} catch (XriStoreException ex) {

				ConfigXRIIndex.log.error(ex);
				error(getString("deletefail") + ex.getLocalizedMessage());
				return;
			}

			this.info(getString("deletesuccess"));

			// update model

			ConfigXRIIndex.this.authorityEquivIDsModel.detach();
		}
	}

	private class MyForm extends Form {

		private static final long serialVersionUID = 4613989919162742883L;

		private TextField valueTextField;
		private TextField priorityTextField;
		private MySubmitLink submitLink;

		private MyForm(String id, IModel model) {

			super(id, model);

			// create components

			this.valueTextField = new TextField("value");
			this.priorityTextField = new TextField("priority");
			this.submitLink = new MySubmitLink("submitLink");

			// add components

			this.add(this.valueTextField);
			this.add(this.priorityTextField);
			this.add(this.submitLink);
			this.add(new FormComponentLabel("valueLabel", this.valueTextField));
			this.add(new FormComponentLabel("priorityLabel", this.priorityTextField));
		}

		private class MySubmitLink extends SubmitLink {

			private static final long serialVersionUID = 608927734460210570L;

			private MySubmitLink(String id) {

				super(id);
			}

			@Override
			public void onSubmit() {

				try {

					// add the EquivID

					ConfigXRIIndex.this.xri.addEquivID(ConfigXRIIndex.this.equivId);

					ConfigXRIIndex.this.equivId = new EquivID("xri://");
					MyForm.this.setModelObject(ConfigXRIIndex.this.equivId);
				} catch (Exception ex) {

					ConfigXRIIndex.log.error(ex);
					error(getString("addfail") + ex.getLocalizedMessage());
					return;
				}

				info(ConfigXRIIndex.this.getString("success"));
				return;
			}
		}
	}

	private class MyForm2 extends Form {

		private static final long serialVersionUID = -7263870079696373898L;

		private TextField valueTextField;
		private MySubmitLink submitLink;

		private MyForm2(String id, IModel model) {

			super(id, model);

			// create components

			this.valueTextField = new TextField("value");
			this.submitLink = new MySubmitLink("submitLink");

			// add components

			this.add(this.valueTextField);
			this.add(this.submitLink);
			this.add(new FormComponentLabel("valueLabel", this.valueTextField));
		}

		private class MySubmitLink extends SubmitLink {

			private static final long serialVersionUID = 608927734460210570L;

			private MySubmitLink(String id) {

				super(id);
			}

			@Override
			public void onSubmit() {

				try {

					// update the CanonicalEquivID

					if (ConfigXRIIndex.this.canonicalEquivId.getValue() != null && ! ConfigXRIIndex.this.canonicalEquivId.getValue().equals("")) {

						ConfigXRIIndex.this.xri.setCanonicalEquivID(ConfigXRIIndex.this.canonicalEquivId);
					} else {

						ConfigXRIIndex.this.xri.deleteCanonicalEquivID();
					}
				} catch (Exception ex) {

					ConfigXRIIndex.log.error(ex);
					error(getString("changefail") + ex.getLocalizedMessage());
					return;
				}

				info(ConfigXRIIndex.this.getString("changesuccess"));
				return;
			}
		}
	}

	private class MyForm3 extends Form {

		private static final long serialVersionUID = -7268933119696373898L;

		private TextArea extensionTextArea;
		private MySubmitLink submitLink;

		private MyForm3(String id, IModel model) {

			super(id, model);

			// create components

			this.extensionTextArea = new TextArea("extension");
			this.submitLink = new MySubmitLink("submitLink");

			// add components

			this.add(this.extensionTextArea);
			this.add(this.submitLink);
			this.add(new FormComponentLabel("extensionLabel", this.extensionTextArea));
		}

		private class MySubmitLink extends SubmitLink {

			private static final long serialVersionUID = 123457734460210571L;

			private MySubmitLink(String id) {

				super(id);
			}

			@Override
			public void onSubmit() {

				try {

					// update the extension

					if (ConfigXRIIndex.this.extension != null && ! ConfigXRIIndex.this.extension.equals("")) {

						ConfigXRIIndex.this.xri.setExtension(ConfigXRIIndex.this.extension);
					} else {

						ConfigXRIIndex.this.xri.deleteExtension();
					}
				} catch (Exception ex) {

					ConfigXRIIndex.log.error(ex);
					error(getString("changefail3") + ex.getLocalizedMessage());
					return;
				}

				info(ConfigXRIIndex.this.getString("changesuccess3"));
				return;
			}
		}
	}

	public String getExtension() {

		return(this.extension);
	}

	public void setExtension(String extension) {

		this.extension = extension;
	}
}
