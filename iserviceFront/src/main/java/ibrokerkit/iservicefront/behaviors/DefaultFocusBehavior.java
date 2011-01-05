package ibrokerkit.iservicefront.behaviors;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.markup.html.IHeaderResponse;

public class DefaultFocusBehavior extends AbstractBehavior {

	private static final long serialVersionUID = -4890211300725227966L;

	private Component component;

	@Override
	public void bind( Component component ) {

		this.component = component;
		component.setOutputMarkupId(true);
	}

	@Override
	public void renderHead( IHeaderResponse iHeaderResponse ) {

		super.renderHead(iHeaderResponse);
		iHeaderResponse.renderOnLoadJavascript("document.getElementById('" + this.component.getMarkupId() + "').focus();");
	}
} 