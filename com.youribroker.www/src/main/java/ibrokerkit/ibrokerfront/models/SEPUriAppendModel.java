package ibrokerkit.ibrokerfront.models;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.LoadableDetachableModel;
import org.openxri.xml.SEPUri;

public class SEPUriAppendModel extends LoadableDetachableModel implements IChoiceRenderer {

	private static final long serialVersionUID = -7547868329105137567L;

	public SEPUriAppendModel() {
		
	}
	
	@Override
	public Object load() {
		
		List<String> list = Arrays.asList(new String[] {
				SEPUri.APPEND_NONE,
				SEPUri.APPEND_LOCAL,
				SEPUri.APPEND_AUTHORITY,
				SEPUri.APPEND_PATH,
				SEPUri.APPEND_QUERY,
				SEPUri.APPEND_QXRI
			});
		
		return(list);
	}

	public Object getDisplayValue(Object object) {
		
		return(object.toString());
	}

	public String getIdValue(Object object, int index) {

		return(object.toString());
	}
}
