package ibrokerkit.ibrokerfront.models;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.LoadableDetachableModel;

public class GCSModel extends LoadableDetachableModel implements IChoiceRenderer {

	private static final long serialVersionUID = 2019955141027434373L;

	private static Log log = LogFactory.getLog(GCSModel.class.getName());

	@Override
	public Object load() {

		List<String> list;

		list = new ArrayList<String> ();

		// add all GCS to the model

		list.add("=");
		list.add("@");

		log.debug("Done.");
		return(list);
	}

	public Object getDisplayValue(Object object) {

		String gcs = (String) object;

		return(gcs);
	}

	public String getIdValue(Object object, int index) {

		return(Integer.toString(index));
	}
}
