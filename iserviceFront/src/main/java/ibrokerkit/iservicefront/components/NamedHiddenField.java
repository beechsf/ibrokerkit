package ibrokerkit.iservicefront.components;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public class NamedHiddenField extends HiddenField {

	private static final long serialVersionUID = -7049370460888002675L;

	protected String name;
	
	public NamedHiddenField(String id, IModel model, String name) {

		super(id, model);
		
		this.name = name;
	}

	public NamedHiddenField(String id, String value, String name) {

		this(id, new Model(value), name);
	}

	public NamedHiddenField(String id, String value) {

		this(id, new Model(value), id);
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {

		super.onComponentTag(tag);

		// add our own name

		tag.put("name", this.name);
	}
}
