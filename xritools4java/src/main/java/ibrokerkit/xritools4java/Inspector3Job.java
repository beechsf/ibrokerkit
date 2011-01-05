package ibrokerkit.xritools4java;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Iterator;

import org.openxri.resolve.Resolver;
import org.openxri.xri3.XRISegment;
import org.openxri.xri3.XRISubSegment;
import org.openxri.xri3.impl.XRI3;
import org.openxri.xri3.impl.parser.TreeDisplayer;

public class Inspector3Job extends Job {

	public final static boolean DEFAULT_RAW = false;

	private String xri;
	private boolean raw;

	public Inspector3Job(String xri, boolean raw) {

		this.xri = xri;
		this.raw = raw;
	}

	@Override
	public void execute(Resolver resolver) {

		// do the inspection

		XRI3 xri = new XRI3(this.xri);

		// produce full result

		this.result = new StringBuffer();

		if (! this.raw) {
	
			if (xri.hasAuthority()) {
	
				this.result.append("The authority has " + xri.getAuthority().getNumSubSegments() + " subsegments.<br>");
	
				int count = 0;
	
				for (Iterator<?> i = xri.getAuthority().getSubSegments().iterator(); i.hasNext(); ) {
	
					count++;
	
					XRISubSegment subSegment = (XRISubSegment) i.next();
	
					this.result.append("-- Subsegment " + count + ": GCS=" + subSegment.getGCS() + " LCS=" + subSegment.getLCS() + " Literal=" + subSegment.getLiteral() + " XRef=" + subSegment.getXRef() + "<br>");
				}
			}
	
			if (xri.hasPath()) {
	
				this.result.append("The path has " + xri.getPath().getNumSegments() + " segments.<br>");
	
				int count = 0;
	
				for (Iterator<?> i = xri.getPath().getSegments().iterator(); i.hasNext(); ) {
	
					count++;
	
					XRISegment segment = (XRISegment) i.next();
	
					this.result.append("++ Segment " + count + ": " + segment.toString() + "<br>");
	
					if (segment.hasLiteral()) {
	
						this.result.append("++ The segment has a literal.<br>");
						this.result.append("++ -- Literal=" + segment.getLiteral() + "<br>");
					}
	
					this.result.append("++ The segment has " + segment.getNumSubSegments() + " subsegments.<br>");
	
					int count2 = 0;
	
					for (Iterator<?> i2 = segment.getSubSegments().iterator(); i2.hasNext(); ) {
	
						count2++;
	
						XRISubSegment subSegment = (XRISubSegment) i2.next();
	
						this.result.append("++ -- Subsegment " + count2 + ": GCS=" + subSegment.getGCS() + " LCS=" + subSegment.getLCS() + " Literal=" + subSegment.getLiteral() + " XRef=" + subSegment.getXRef() + "<br>");
					}
				}
			}
	
			this.result.append("<b>Authority:</b> " + ((xri.getAuthority() == null) ? "" : xri.getAuthority().toString()) + "<br>");
			this.result.append("<b>Path:</b> " + ((xri.getPath() == null) ? "" : xri.getPath().toString()) + "<br>");
			this.result.append("<b>Query:</b> " + ((xri.getQuery() == null) ? "" : xri.getQuery().toString()) + "<br>");
			this.result.append("<b>Fragment:</b> " + ((xri.getFragment() == null) ? "" : xri.getFragment().toString()) + "<br>");
	
			this.result.append("<b>IRI normal form:</b> " + xri.toIRINormalForm() + "<br>");
			this.result.append("<b>URI normal form:</b> " + xri.toURINormalForm() + "<br>");

			if (xri.isIName()) {

				this.result.append("This XRI is a valid i-name.<br>");
			} else {
	
				this.result.append("This XRI is not a valid i-name.<br>");
			}
	
			if (xri.isReserved()) {
	
				this.result.append("This XRI is a reserved i-name.<br>");
			} else {
	
				this.result.append("This XRI is not a reserved i-name.<br>");
			}
	
			if (xri.isINumber()) {
	
				this.result.append("This XRI is a valid i-number.<br>");
			} else {
	
				this.result.append("This XRI is not a valid i-number.<br>");
			}
		} else {
			
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			new TreeDisplayer(new PrintStream(stream)).visit(xri.getParserObject());
			String string = "<pre>" + stream.toString() + "</pre>";
			string.replaceAll("\\r\\n", "<br>");
			string.replaceAll("\\n", "<br>");
			this.result.append(string);
		}

		// produce one line result

		this.oneLineResult = new StringBuffer();

		this.oneLineResult.append("Authority: " + ((xri.getAuthority() == null) ? "" : xri.getAuthority().toString()) + " | ");
		this.oneLineResult.append("Path: " + ((xri.getPath() == null) ? "" : xri.getPath().toString()) + " | ");
		this.oneLineResult.append("Query: " + ((xri.getQuery() == null) ? "" : xri.getQuery().toString()) + " | ");
		this.oneLineResult.append("Fragment: " + ((xri.getFragment() == null) ? "" : xri.getFragment().toString()) + " | ");
		this.oneLineResult.append("Subsegments: " + (xri.hasAuthority() ? xri.getAuthority().getNumSubSegments() : 0) + " | ");
		this.oneLineResult.append("Segments: " + (xri.hasPath() ? xri.getPath().getNumSegments() : 0) + " | ");
		this.oneLineResult.append("i-name: " + xri.isIName() + " | ");
		this.oneLineResult.append("i-number: " + xri.isINumber() + " | ");

		// produce stats

		this.stats = null;
	}

	@Override
	public String getJobName() {

		return("Inspector3");
	}
}
