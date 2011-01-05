package ibrokerkit.xritools4java;

import ibrokerkit.xritools4java.types.ResolveType;
import ibrokerkit.xritools4java.types.ResultType;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.openxri.XRI;
import org.openxri.resolve.Resolver;
import org.openxri.resolve.ResolverFlags;
import org.openxri.resolve.ResolverState;
import org.openxri.resolve.exception.PartialResolutionException;
import org.openxri.xml.XRD;
import org.openxri.xml.XRDS;


public class ResolutionJob extends Job {

	public final static ResolveType DEFAULT_RESOLVETYPE = ResolveType.AUTH;
	public final static ResultType DEFAULT_RESULTTYPE = ResultType.XRD;
	public final static String DEFAULT_SERVICETYPE = null;
	public final static String DEFAULT_MEDIATYPE = null;
	public final static String DEFAULT_PROXY = null;
	
	public final static boolean DEFAULT_CID = true;
	public final static boolean DEFAULT_HTTPS = false;
	public final static boolean DEFAULT_SAML = false;
	public final static boolean DEFAULT_REFS = true;
	public final static boolean DEFAULT_NODEFAULTP = false;
	public final static boolean DEFAULT_NODEFAULTT = false;
	public final static boolean DEFAULT_NODEFAULTM = false;
	public final static boolean DEFAULT_URIC = false;
	
	private String xri;
	private ResolveType resolveType;
	private ResultType resultType;
	private String serviceType;
	private String mediaType;
	private String proxy;

	private boolean cid;
	private boolean https;
	private boolean saml;
	private boolean refs;
	private boolean noDefaultP;
	private boolean noDefaultT;
	private boolean noDefaultM;
	private boolean uric;

	public ResolutionJob(String xri, ResolveType resolveType, ResultType resultType, String serviceType, String mediaType, String proxy, boolean cid, boolean https, boolean saml, boolean refs, boolean noDefaultP, boolean noDefaultT, boolean noDefaultM, boolean uric) {

		this.xri = xri;
		this.resolveType = resolveType;
		this.resultType = resultType;
		this.serviceType = serviceType;
		this.mediaType = mediaType;
		this.proxy = proxy;

		this.cid = cid;
		this.https = https;
		this.saml = saml;
		this.refs = refs;
		this.noDefaultP = noDefaultP;
		this.noDefaultT = noDefaultT;
		this.noDefaultM = noDefaultM;
		this.uric = uric;
	}

	public ResolutionJob(String xri, ResolveType resolveType, ResultType resultType, String serviceType, String mediaType, String proxy) {

		this(
				xri,
				resolveType,
				resultType,
				serviceType,
				mediaType,
				proxy,
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

		// init buffers

		this.result = new StringBuffer();
		this.oneLineResult = new StringBuffer();
		this.stats = new StringBuffer();

		// init resolver flags, resolver state and resolver

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

		if (this.proxy != null && (! this.proxy.trim().equals(""))) {

			try {

				resolver.setProxyURI(new URI(this.proxy));
				this.result.append("Proxy set to: " + this.proxy + "<br>");
			} catch (URISyntaxException ex) {

				resolver.setProxyURI(null);
				this.result.append("Cannot set proxy: " + ex.getLocalizedMessage() + "<br>");
				this.result.append("Trying it without proxy.<br>");
			}
		} else {

			resolver.setProxyURI(null);
		}

		// do the resolution (catch exceptions so we can output a partial result)

		try {

			if (this.resolveType.equals(ResolveType.SEP)) {

				if (this.resultType.equals(ResultType.URILIST)) {

					List<?> list = resolver.resolveSEPToURIList(
							new XRI(this.xri), 
							this.serviceType, 
							this.mediaType, 
							flags,
							state);

					for (int i=0; i<list.size(); i++) {

						String uri = (String) list.get(i);
						this.result.append(uri + "<br>");
						this.oneLineResult.append(uri + " *** ");
					}
				} else if (this.resultType.equals(ResultType.XRD)) {

					XRD xrd = resolver.resolveSEPToXRD(
							new XRI(this.xri), 
							this.serviceType, 
							this.mediaType, 
							flags,
							state);

					this.result.append(htmlencode(xrd.toResultString()) + "<br>");
					this.oneLineResult.append(
							"Status: " + xrd.getStatusCode() + ", " + 
							"ProviderID: " + xrd.getProviderID() + ", " + 
							"Query: " + xrd.getQuery() + ", " + 
							"SEPs: " + xrd.getNumServices() + ", " +
							"Refs: " + xrd.getNumRefs());
				} else {

					this.result.append("Result Type " + this.resultType + " not supported when resolving to " + this.resolveType + ".<br>");
					this.oneLineResult.append("Result Type " + this.resultType + " not supported when resolving to " + this.resolveType + ".");
				}
			} else if (this.resolveType.equals(ResolveType.AUTH)) {

				if (this.resultType.equals(ResultType.XRD)) {

					XRD xrd = resolver.resolveAuthToXRD(
							new XRI(this.xri), 
							flags,
							state);

					this.result.append(htmlencode(xrd.toString()) + "<br>");
					this.oneLineResult.append(
							"Status: " + xrd.getStatusCode() + ", " + 
							"ProviderID: " + xrd.getProviderID() + ", " + 
							"Query: " + xrd.getQuery() + ", " + 
							"SEPs: " + xrd.getNumServices() + ", " +
							"Refs: " + xrd.getNumRefs());
				} else if (this.resultType.equals(ResultType.XRDS)) {

					XRDS xrds = resolver.resolveAuthToXRDS(
							new XRI(this.xri), 
							flags,
							state);

					this.result.append(htmlencode(xrds.toString()) + "<br>");
					this.oneLineResult.append(
							"Number of XRDs: " + xrds.getNumXRD());
				} else {

					this.result.append("Result Type " + this.resultType + " not supported when resolving to " + this.resolveType + ".<br>");
					this.oneLineResult.append("Result Type " + this.resultType + " not supported when resolving to " + this.resolveType + ".");
				}
			} else {

				this.result.append("Resolve Type " + this.resolveType + " not supported.<br>");
				this.oneLineResult.append("Resolve Type " + this.resolveType + " not supported.");
			}
		} catch (Exception ex) {

			if (ex instanceof PartialResolutionException) {

				XRD finalXRD = ((PartialResolutionException) ex).getPartialXRDS().getFinalXRD();

				if (finalXRD != null) {

					this.result.append("Error: " + finalXRD.getStatus().getText() + "<br>");
					this.oneLineResult.append("Error: " + finalXRD.getStatus().getText() + " - ");
				}
			}

			this.result.append(ex.getLocalizedMessage() + "<br>");
			this.oneLineResult.append(ex.getLocalizedMessage());
		}

		// produce stats

		if (state.getNumSteps() > 0) {

			this.stats.append("Requests: " + state.getNumRequests() + "<br>");
			this.stats.append("Steps: " + state.getNumSteps() + "<br>");
			this.stats.append("Bytes received: " + state.getNumBytesReceived() + "<br>");
			this.stats.append("Time elapsed: " + (System.currentTimeMillis()-state.getTimeStarted()) + "ms<br>");
			this.stats.append("References followed: " + state.getNumRefsFollowed() + "<br>");
		}
	}

	@Override
	public String getJobName() {

		return("Resolution");
	}

	private String htmlencode(String html) {

		return(html
				.replaceAll("<", "&lt;")
				.replaceAll(">", "&gt;")
				.replaceAll(" ", "&nbsp;")
				.replaceAll("\n", "<br>"));
	}
}
