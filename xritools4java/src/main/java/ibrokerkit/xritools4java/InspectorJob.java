package ibrokerkit.xritools4java;

import org.openxri.GCSAuthority;
import org.openxri.IRIAuthority;
import org.openxri.XRI;
import org.openxri.XRefAuthority;
import org.openxri.resolve.Resolver;

public class InspectorJob extends Job {

	private String xri;

	public InspectorJob(String xri) {

		this.xri = xri;

		if (this.xri.toLowerCase().startsWith("xri://")) this.xri = this.xri.substring(6);
	}

	@Override
	public void execute(Resolver resolver) {

		// do the inspection

		XRI x = new XRI(this.xri);

		// produce full result

		this.result = new StringBuffer();

		if (x.isAbsolute()) {

			this.result.append("This is an <b>absolute</b> XRI.<br>");
		} else {

			this.result.append("This is a <b>relative</b> XRI.<br>");
		}

		if (x.getAuthorityPath() instanceof GCSAuthority) {

			this.result.append("This XRI has a GCS Authority.<br>");
		} else if (x.getAuthorityPath() instanceof XRefAuthority) {

			this.result.append("This XRI has a XRef Authority.<br>");
		} else if (x.getAuthorityPath() instanceof IRIAuthority) {

			this.result.append("This XRI has a IRI Authority.<br>");
		}

		this.result.append("<b>Authority:</b> " + ((x.getAuthorityPath() == null) ? "" : x.getAuthorityPath().toString()) + "<br>");
		this.result.append("<b>Path:</b> " + ((x.getXRIPath() == null) ? "" : x.getXRIPath().toString()) + "<br>");

		if (x.getXRIPath() != null) {

			for (int i=0; i<x.getXRIPath().getNumSegments(); i++) {

				this.result.append("<b>Segment " + (i+1) + ":</b> " + x.getXRIPath().getSegmentAt(i).toString() + "<br>");
			}
		}

		this.result.append("<b>Query:</b> " + ((x.getQuery() == null) ? "" : x.getQuery().toString()) + "<br>");
		this.result.append("<b>Fragment:</b> " + ((x.getFragment() == null) ? "" : x.getFragment().toString()) + "<br>");

		this.result.append("<b>IRI normal form:</b> " + x.toIRINormalForm() + "<br>");
		this.result.append("<b>URI normal form:</b> " + x.toURINormalForm() + "<br>");

		// produce one line result

		this.oneLineResult = new StringBuffer();

		this.oneLineResult.append("Authority: " + ((x.getAuthorityPath() == null) ? "" : x.getAuthorityPath().toString()) + " | ");
		this.oneLineResult.append("Path: " + ((x.getXRIPath() == null) ? "" : x.getXRIPath().toString()) + " | ");
		this.oneLineResult.append("Query: " + ((x.getQuery() == null) ? "" : x.getQuery().toString()) + " | ");
		this.oneLineResult.append("Fragment: " + ((x.getFragment() == null) ? "" : x.getFragment().toString()) + " | ");

		// produce stats

		this.stats = null;
	}

	@Override
	public String getJobName() {

		return("Inspector");
	}
}
