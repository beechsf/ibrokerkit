package ibrokerkit.iservicefront.behaviors;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnLoadHeaderItem;

public class DefaultFocusBehavior extends Behavior {

	private static final long serialVersionUID = -4890211300725227966L;

	@Override
	public void bind(Component component) {

		component.setOutputMarkupId(true);
	}

	@Override
	public void renderHead(Component component, IHeaderResponse headerResponse) {

		super.renderHead(component, headerResponse);

		headerResponse.render(new OnLoadHeaderItem("document.getElementById('" + component.getMarkupId() + "').focus();"));
	}
} 