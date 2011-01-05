package ibrokerkit.xritools4java;

import java.util.List;

import org.openxri.XRI;
import org.openxri.resolve.Resolver;
import org.openxri.resolve.ResolverFlags;
import org.openxri.resolve.ResolverState;
import org.openxri.resolve.ResolverState.ResolverStep;
import org.openxri.resolve.exception.PartialResolutionException;
import org.openxri.xml.XRD;

public class TracerouteJob extends Job {
	
	public final static boolean DEFAULT_CID = true;
	public final static boolean DEFAULT_HTTPS = false;
	public final static boolean DEFAULT_SAML = false;
	public final static boolean DEFAULT_REFS = true;
	public final static boolean DEFAULT_NODEFAULTP = false;
	public final static boolean DEFAULT_NODEFAULTT = false;
	public final static boolean DEFAULT_NODEFAULTM = false;
	public final static boolean DEFAULT_URIC = false;

	private String xri;

	private boolean cid;
	private boolean https;
	private boolean saml;
	private boolean refs;
	private boolean noDefaultP;
	private boolean noDefaultT;
	private boolean noDefaultM;
	private boolean uric;

	public TracerouteJob(String xri, boolean cid, boolean https, boolean saml, boolean refs, boolean noDefaultP, boolean noDefaultT, boolean noDefaultM, boolean uric) {

		this.xri = xri;

		this.cid = cid;
		this.https = https;
		this.saml = saml;
		this.refs = refs;
		this.noDefaultP = noDefaultP;
		this.noDefaultT = noDefaultT;
		this.noDefaultM = noDefaultM;
		this.uric = uric;

		if (this.xri.toLowerCase().startsWith("xri://")) this.xri = this.xri.substring(6);
	}

	public TracerouteJob(String xri) {

		this(
				xri,
				DEFAULT_CID,
				DEFAULT_HTTPS,
				DEFAULT_SAML,
				DEFAULT_REFS,
				DEFAULT_NODEFAULTP,
				DEFAULT_NODEFAULTT,
				DEFAULT_NODEFAULTM,
				DEFAULT_URIC);
	}

	@Override
	public void execute(Resolver resolver) throws PartialResolutionException {

		// init resolver flags, resolver state, and resolver

		ResolverFlags flags = new ResolverFlags();
		flags.setCid(this.cid);
		flags.setHttps(this.https);
		flags.setSaml(this.saml);
		flags.setRefs(this.refs);
		flags.setNoDefaultP(this.noDefaultP);
		flags.setNoDefaultT(this.noDefaultT);
		flags.setNoDefaultM(this.noDefaultM);
		flags.setUric(this.uric);

		ResolverState state = new ResolverState();

		resolver.setProxyURI(null);

		// init buffers

		this.result = new StringBuffer();
		this.oneLineResult = new StringBuffer();
		this.stats = new StringBuffer();

		// do the traceroute (catch exceptions so we can output a partial result)

		int stepnr = 1;

		try {

			List<?> uris = resolver.resolveSEPToURIList(
					new XRI(this.xri), 
					null,
					null,
					flags,
					state);

			// output the root authority

			this.printStartStep(resolver, this.xri.substring(0, 1), stepnr);
			this.printOneLineStartStep(resolver, this.xri.substring(0, 1), stepnr);
			stepnr++;

			// output each resolver step

			for (int i=0; i<state.getNumSteps(); i++) {

				ResolverStep step = state.getStepAt(i);

				this.printResolverStep(state, step, stepnr);
				this.printOneLineResolverStep(state, step, stepnr);
				stepnr++;
			}

			// output each result URI

			for (int i=0; i<uris.size(); i++) {

				String uri = (String) uris.get(i);

				this.printResultStep(state, uri, stepnr);
				this.printOneLineResultStep(state, uri, stepnr);
				stepnr++;
			}
		} catch (Exception ex) {	// we may get an exception if we only resolved partially

			// output the root authority

			this.printStartStep(resolver, this.xri.substring(0, 1), stepnr);
			this.printOneLineStartStep(resolver, this.xri.substring(0, 1), stepnr);
			stepnr++;

			// output each resolver step

			for (int i=0; i<state.getNumSteps(); i++) {

				ResolverStep step = state.getStepAt(i);

				this.printResolverStep(state, step, stepnr);
				this.printOneLineResolverStep(state, step, stepnr);
				stepnr++;
			}

			// output exception information

			this.printException(ex, stepnr);
			this.printOneLineException(ex, stepnr);
			stepnr++;
		}

		// produce stats

		if (state.getNumSteps() > 0) {

			this.stats.append("Requests: " + state.getNumRequests() + "<br>");
			this.stats.append("Steps: " + (stepnr-1) + "<br>");
			this.stats.append("Bytes received: " + state.getNumBytesReceived() + "<br>");
			this.stats.append("Time elapsed: " + (System.currentTimeMillis()-state.getTimeStarted()) + "ms<br>");
			this.stats.append("References followed: " + state.getNumRefsFollowed() + "<br>");
		}
	}

	@Override
	public String getJobName() {

		return("Traceroute");
	}

	private void printStartStep(Resolver resolver, String root, int stepnr) {

		XRD rootAuth = resolver.getAuthority(root);

		if (rootAuth != null) {

			this.result.append(String.format("%3d", new Object[] { new Integer(stepnr) }) + ": ");
			this.result.append(String.format("%4d", new Object[] { new Integer(0) } ));
			this.result.append("ms ");
			this.result.append("(" + root + ") ");
			this.result.append(rootAuth.getServiceAt(0).getURIAt(0).getURI().toString() + " ");
			this.result.append("<br>");
		} else {

			this.result.append(String.format("%3d", new Object[] { new Integer(stepnr) }) + ": ");
			this.result.append(String.format("%4d", new Object[] { new Integer(0) } ));
			this.result.append("ms ");
			this.result.append("No root authority found.");
			this.result.append("<br>");
		}
	}

	private void printOneLineStartStep(Resolver resolver, String root, int stepnr) {

		XRD rootAuth = resolver.getAuthority(root);

		if (rootAuth != null) {

			this.oneLineResult.append(" *** ");
			this.oneLineResult.append(String.format("%3d", new Object[] { new Integer(stepnr) }) + ": ");
			this.oneLineResult.append(String.format("%4d", new Object[] { new Integer(0) } ));
			this.oneLineResult.append("ms ");
			this.oneLineResult.append("(" + root + ") ");
			this.oneLineResult.append(rootAuth.getServiceAt(0).getURIAt(0).getURI().toString() + " ");
		} else {

			this.oneLineResult.append(" *** ");
			this.oneLineResult.append(String.format("%3d", new Object[] { new Integer(stepnr) }) + ": ");
			this.oneLineResult.append(String.format("%4d", new Object[] { new Integer(0) } ));
			this.oneLineResult.append("ms ");
			this.oneLineResult.append("No root authority found.");
		}
	}

	private void printResolverStep(ResolverState state, ResolverStep step, int stepnr) {

		this.result.append(String.format("%3d", new Object[] { new Integer(stepnr) }) + ": ");
		this.result.append(String.format("%4d", new Object[] { new Long(step.timeCompleted - state.getTimeStarted()) } ));
		this.result.append("ms ");
		this.result.append("(" + step.qxri + ") ");
		this.result.append(step.uri + " ");
		this.result.append("ref=" + step.ref);
		this.result.append("<br>");
	}

	private void printOneLineResolverStep(ResolverState state, ResolverStep step, int stepnr) {

		this.oneLineResult.append(" *** ");
		this.oneLineResult.append(new Integer(stepnr).toString() + ": ");
		this.oneLineResult.append(new Long(step.timeCompleted - state.getTimeStarted()).toString());
		this.oneLineResult.append("ms ");
		this.oneLineResult.append("(" + step.qxri + ") ");
		this.oneLineResult.append(step.uri + " ");
	}

	private void printResultStep(ResolverState state, String uri, int stepnr) {

		this.result.append(String.format("%3d", new Object[] { new Integer(stepnr) }) + ": ");
		this.result.append(String.format("%4d", new Object[] { new Long(System.currentTimeMillis() - state.getTimeStarted()) } ));
		this.result.append("ms " + uri);
		this.result.append("<br>");
	}

	private void printOneLineResultStep(ResolverState state, String uri, int stepnr) {

		this.oneLineResult.append(" *** ");
		this.oneLineResult.append(String.format("%3d", new Object[] { new Integer(stepnr) }) + ": ");
		this.oneLineResult.append(String.format("%4d", new Object[] { new Long(System.currentTimeMillis() - state.getTimeStarted()) } ));
		this.oneLineResult.append("ms " + uri);
	}

	private void printException(Exception ex, int stepnr) {

		if (ex instanceof PartialResolutionException) {

			XRD finalXRD = ((PartialResolutionException) ex).getPartialXRDS().getFinalXRD();

			if (finalXRD != null) {

				this.result.append("Error at last step: " + finalXRD.getStatus().getText() + "<br>");
			}
		}

		this.result.append(ex.getMessage() + "<br>");
	}

	private void printOneLineException(Exception ex, int stepnr) {

		if (ex instanceof PartialResolutionException) {

			XRD finalXRD = ((PartialResolutionException) ex).getPartialXRDS().getFinalXRD();

			if (finalXRD != null) {

				this.oneLineResult.append("ERROR: " + finalXRD.getStatus().getText() + ", ");
			}
		}

		this.oneLineResult.append(ex.getMessage());
	}
}
