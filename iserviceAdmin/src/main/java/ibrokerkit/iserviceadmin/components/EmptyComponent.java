package ibrokerkit.iserviceadmin.components;

import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.IModel;

public class EmptyComponent extends WebComponent {

	private static final long serialVersionUID = -4201991723688083091L;

	public EmptyComponent(String id, IModel model) {

		super(id, model);
		this.setVisible(false);
	}

	public EmptyComponent(String id) {

		super(id);
		this.setVisible(false);
	}
}
