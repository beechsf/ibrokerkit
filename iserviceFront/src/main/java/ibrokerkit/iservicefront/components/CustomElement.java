package ibrokerkit.iservicefront.components;

import java.util.Map;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;


public class CustomElement extends WebComponent {

	private static final long serialVersionUID = 3356691527948946615L;

	private String content;
	private Map<String, String> attributes;

	public CustomElement(String id, String content, Map<String, String> attributes) {

		super(id);

		this.content = content;
		this.attributes = attributes;
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {

		if (this.attributes != null) for (String key : this.attributes.keySet()) tag.getAttributes().put(key, this.attributes.get(key));
	}

	@Override
	protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {

		if (this.content != null) this.replaceComponentTagBody(markupStream, openTag, this.content);
	}
}
