package ibrokerkit.iserviceadmin.components;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public class MapPanel extends Panel {

	private static final long serialVersionUID = 8430803488175700401L;

	private ListView listView;
	private TextField keyTextField;
	private TextField valueTextField;

	@SuppressWarnings("unchecked")
	public MapPanel(String id) {

		super(id);

		// create and add components

		IModel listViewModel = new AbstractReadOnlyModel() {

			private static final long serialVersionUID = 7397553165530168752L;

			@Override
			public Object getObject() {

				final Map<String, String> map = (Map<String, String>) MapPanel.this.getModelObject();

				return(new ArrayList(map.entrySet()));
			}
		};

		this.listView = new ListView("map", listViewModel) {

			private static final long serialVersionUID = -6171507661587195670L;

			@SuppressWarnings("unchecked")
			@Override
			protected void populateItem(ListItem item) {

				Entry<String, String> entry = (Entry<String, String>) item.getModelObject();

				item.add(new Label("key", entry.getKey()));
				item.add(new Label("value", entry.getValue()));
				item.add(new DeleteLink("deleteLink", entry.getKey()));
			}
		};

		this.keyTextField = new TextField("key", new Model());
		this.valueTextField = new TextField("value", new Model());

		this.add(this.listView);
		this.add(this.keyTextField);
		this.add(this.valueTextField);
		this.add(new AttributeAddButton("addButton"));

		this.setOutputMarkupId(true);
	}

	private class AttributeAddButton extends AjaxSubmitLink {

		private static final long serialVersionUID = -3045724879221258490L;

		private AttributeAddButton(String id) {

			super(id);

			this.setDefaultFormProcessing(false);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void onSubmit(AjaxRequestTarget target, Form form) {

			final Map<String, String> map = (Map<String, String>) MapPanel.this.getModelObject();

			map.put(MapPanel.this.keyTextField.getValue(), MapPanel.this.valueTextField.getValue());

			if (target != null) target.addComponent(MapPanel.this);
		}
	}

	private class DeleteLink extends AjaxSubmitLink {

		private static final long serialVersionUID = -5645609067816663587L;

		private String key;

		private DeleteLink(String id, final String key) {

			super(id);

			this.key = key;
			
			this.setDefaultFormProcessing(false);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void onSubmit(AjaxRequestTarget target, Form form) {

			final Map<String, String> map = (Map<String, String>) MapPanel.this.getModelObject();

			map.remove(this.key);

			if (target != null) target.addComponent(MapPanel.this);
		}
	}
}
