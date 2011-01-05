package ibrokerkit.ibrokerfront.models;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.AbstractReadOnlyModel;

public class IServiceTypeModel extends AbstractReadOnlyModel implements IChoiceRenderer {

	private static final long serialVersionUID = 8704832173838289541L;

	public static List<String> list = Arrays.asList(new String[] {
			"Authentication",
			"Contact Page",
			"Forwarding",
			"Locator"
		}); 
	
	public IServiceTypeModel() {

	}

	@Override
	public Object getObject() {
		
		return(list);
	}
	
	public Object getDisplayValue(Object object) {

		return(object.toString());
	}

	public String getIdValue(Object object, int index) {

		return(object.toString());
	}
}
