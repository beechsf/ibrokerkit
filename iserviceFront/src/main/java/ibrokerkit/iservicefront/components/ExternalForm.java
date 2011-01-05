package ibrokerkit.iservicefront.components;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.form.Form;

public class ExternalForm extends Form {

	private static final long serialVersionUID = 6825182163390602637L;

	protected String actionUrl;

	public ExternalForm(String id, String actionUrl) {

		super(id);

		this.actionUrl = actionUrl;
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {

		super.onComponentTag(tag);

		// replace the wicket-generated ACTION attribute with our external one

		tag.put("action", this.actionUrl);
	}

	@Override
	protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {

		// just use the MarkupContainer's default behavior, not the Form's override

		this.renderComponentTagBody(markupStream, openTag);
	}
}
