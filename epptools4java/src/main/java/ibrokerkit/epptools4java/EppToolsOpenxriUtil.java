package ibrokerkit.epptools4java;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Vector;

import org.openxri.util.DOMUtils;
import org.openxri.xml.CanonicalEquivID;
import org.openxri.xml.EquivID;
import org.openxri.xml.LocalID;
import org.openxri.xml.Redirect;
import org.openxri.xml.Ref;
import org.openxri.xml.SEPMediaType;
import org.openxri.xml.SEPPath;
import org.openxri.xml.SEPType;
import org.openxri.xml.SEPUri;
import org.openxri.xml.Service;
import org.w3c.dom.Element;

import com.neulevel.epp.xri.EppXriRef;
import com.neulevel.epp.xri.EppXriServiceEndpoint;
import com.neulevel.epp.xri.EppXriServiceEndpointMediaType;
import com.neulevel.epp.xri.EppXriServiceEndpointPath;
import com.neulevel.epp.xri.EppXriServiceEndpointType;
import com.neulevel.epp.xri.EppXriSynonym;
import com.neulevel.epp.xri.EppXriURI;

public class EppToolsOpenxriUtil {

	private static final String TAG_GRS_ID = "grsid";

	/*
	 * Methods for authority data
	 */

	public static EppXriSynonym[] makeEppXriSynonyms(EquivID[] openxriEquivIDs) throws EppToolsException {

		EppXriSynonym[] eppXriSynonyms = new EppXriSynonym[openxriEquivIDs.length];
		for (int i=0; i<openxriEquivIDs.length; i++) eppXriSynonyms[i] = makeEppXriSynonym(openxriEquivIDs[i]);

		return eppXriSynonyms;
	}

	public static EppXriRef[] makeEppXriRefs(Ref[] openxriRefs) throws EppToolsException {

		EppXriRef[] EppXriRefs = new EppXriRef[openxriRefs.length];
		for (int i=0; i<openxriRefs.length; i++) EppXriRefs[i] = makeEppXriRef(openxriRefs[i]);

		return EppXriRefs;
	}

	public static EppXriURI[] makeEppXriURIs(Redirect[] openxriRedirects) throws EppToolsException {

		EppXriURI[] eppXriURIs = new EppXriURI[openxriRedirects.length];
		for (int i=0; i<openxriRedirects.length; i++) eppXriURIs[i] = makeEppXriURI(openxriRedirects[i]);

		return eppXriURIs;
	}

	public static EppXriServiceEndpoint[] makeEppXriServiceEndpoints(Service[] openxriServices) throws EppToolsException {

		EppXriServiceEndpoint[] eppXriServiceEndpoints = new EppXriServiceEndpoint[openxriServices.length];
		for (int i=0; i<openxriServices.length; i++) eppXriServiceEndpoints[i] = makeEppXriServiceEndpoint(openxriServices[i]);

		return eppXriServiceEndpoints;
	}

	public static String makeCanonicalEquivIDString(CanonicalEquivID openxriCanonicalEquivID) throws EppToolsException {

		String canonicalEquivIDString = openxriCanonicalEquivID.getValue();

		return canonicalEquivIDString;
	}

	public static String[] makeEquivIDStrings(EquivID[] openxriEquivIDs) throws EppToolsException {

		String[] equivIDs = new String[openxriEquivIDs.length];
		for (int i=0; i<openxriEquivIDs.length; i++) equivIDs[i] = openxriEquivIDs[i].getValue();

		return equivIDs;
	}

	public static String[] makeRefStrings(Ref[] openxriRefs) throws EppToolsException {

		String[] refStrings = new String[openxriRefs.length];
		for (int i=0; i<openxriRefs.length; i++) refStrings[i] = openxriRefs[i].getValue();

		return refStrings;
	}

	public static String[] makeRedirectStrings(Redirect[] openxriRedirects) throws EppToolsException {

		String[] redirectStrings = new String[openxriRedirects.length];
		for (int i=0; i<openxriRedirects.length; i++) redirectStrings[i] = openxriRedirects[i].getValue();

		return redirectStrings;
	}

	public static String[] makeServiceIds(Service[] openxriServices) throws EppToolsException {

		String[] serviceIds = new String[openxriServices.length];
		for (int i=0; i<openxriServices.length; i++) serviceIds[i] = ((Element) openxriServices[i].getOtherTagValues(TAG_GRS_ID).get(0)).getTextContent();

		return serviceIds;
	}

	/*
	 * Methods for converting from OpenXRI classes to EPP classes
	 */

	@SuppressWarnings("unchecked")
	public static EppXriServiceEndpoint makeEppXriServiceEndpoint(Service service) {

		EppXriServiceEndpoint eppXriServiceEndpoint = new EppXriServiceEndpoint();

		for (SEPPath path : (Vector<SEPPath>) service.getPaths()) eppXriServiceEndpoint.addPath(makeEppXriServiceEndpointPath(path));
		for (SEPType type : (Vector<SEPType>) service.getTypes()) eppXriServiceEndpoint.addType(makeEppXriServiceEndpointType(type));
		for (SEPMediaType mediaType : (Vector<SEPMediaType>) service.getMediaTypes()) eppXriServiceEndpoint.addMediaType(makeEppXriServiceEndpointMediaType(mediaType));
		for (SEPUri uri : (Vector<SEPUri>) service.getURIs()) eppXriServiceEndpoint.addURI(makeEppXriURI(uri));
		for (Ref ref : (Vector<Ref>) service.getRefs()) eppXriServiceEndpoint.addRef(makeEppXriRef(ref));
		for (Redirect redirect : (Vector<Redirect>) service.getRedirects()) eppXriServiceEndpoint.addRedirect(makeEppXriURI(redirect));
		for (int i=0; i<service.getNumLocalIDs(); i++) eppXriServiceEndpoint.addLocalID(makeEppXriSynonym(service.getLocalIDAt(i)));
		eppXriServiceEndpoint.setPriority(service.getPriority() == null ? 10 : service.getPriority().intValue());
		eppXriServiceEndpoint.setAuthority(service.getProviderId());

		String id;
		String extension;

		if (service.getOtherTagValues(TAG_GRS_ID) != null && service.getOtherTagValues(TAG_GRS_ID).size() > 0) {

			id = ((Element) service.getOtherTagValues(TAG_GRS_ID).get(0)).getTextContent();
			service.getOtherChildrenVectorMap().remove(TAG_GRS_ID);
			extension = service.getExtension();
			service.setOtherTagValues(TAG_GRS_ID, "<" + TAG_GRS_ID + ">" + id + "</" + TAG_GRS_ID + ">");
		} else {

			id = EppTools.makeGrsServiceId();
			extension = service.getExtension();
		}

		if (service.getKeyInfo() != null) {

			if (extension == null) extension = "";
			extension += DOMUtils.toString(service.getKeyInfo().getElement(), true, true);
		}

		eppXriServiceEndpoint.setId(id);
		eppXriServiceEndpoint.setExtension(extension);

		return(eppXriServiceEndpoint);
	}

	public static EppXriServiceEndpointPath makeEppXriServiceEndpointPath(SEPPath path) {

		return(new EppXriServiceEndpointPath(path.getPath(), path.getMatch(), new Boolean(path.getSelect())));
	}

	public static EppXriServiceEndpointType makeEppXriServiceEndpointType(SEPType type) {

		return(new EppXriServiceEndpointType(type.getType(), type.getMatch(), new Boolean(type.getSelect())));
	}

	public static EppXriServiceEndpointMediaType makeEppXriServiceEndpointMediaType(SEPMediaType mediaType) {

		return(new EppXriServiceEndpointMediaType(mediaType.getMediaType(), mediaType.getMatch(), new Boolean(mediaType.getSelect())));
	}

	public static EppXriURI makeEppXriURI(SEPUri uri) {

		EppXriURI eppXriURI = new EppXriURI(uri.getUriString());
		if (uri.getPriority() != null) eppXriURI.setPriority(uri.getPriority().intValue());
		if (uri.getAppend() != null) eppXriURI.setAppend(uri.getAppend());
		return(eppXriURI);
	}

	public static EppXriURI makeEppXriURI(Redirect redirect) {

		EppXriURI eppXriURI = new EppXriURI(redirect.getValue());
		if (redirect.getPriority() != null) eppXriURI.setPriority(redirect.getPriority().intValue());
		if (redirect.getAppend() != null) eppXriURI.setAppend(redirect.getAppend());
		return(eppXriURI);
	}

	public static EppXriRef makeEppXriRef(Ref ref) {

		EppXriRef eppXriRef = new EppXriRef(ref.getValue());
		if (ref.getPriority() != null) eppXriRef.setPriority(ref.getPriority().intValue());
		return(eppXriRef);
	}

	public static EppXriSynonym makeEppXriSynonym(CanonicalEquivID canonicalEquivID) {

		EppXriSynonym eppXriSynonym = new EppXriSynonym(canonicalEquivID.getValue());
		return(eppXriSynonym);
	}

	public static EppXriSynonym makeEppXriSynonym(LocalID localID) {

		EppXriSynonym eppXriSynonym = new EppXriSynonym(localID.getValue());
		return(eppXriSynonym);
	}

	public static EppXriSynonym makeEppXriSynonym(EquivID equivID) {

		EppXriSynonym eppXriSynonym = new EppXriSynonym(equivID.getValue());
		if (equivID.getPriority() != null) eppXriSynonym.setPriority(equivID.getPriority().intValue());
		return(eppXriSynonym);
	}

	/*
	 * Methods for converting from EPP classes to OpenXRI classes
	 */

	@SuppressWarnings("unchecked")
	public static Service makeOpenxriService(EppXriServiceEndpoint eppXriServiceEndpoint) throws EppToolsException {

		Service service = new Service();

		try {

			for (EppXriServiceEndpointPath eppXriServiceEndpointPath : (List<EppXriServiceEndpointPath>) eppXriServiceEndpoint.getPath()) service.addPath(makeOpenxriPath(eppXriServiceEndpointPath));
			for (EppXriServiceEndpointType eppXriServiceEndpointType : (List<EppXriServiceEndpointType>) eppXriServiceEndpoint.getType()) service.addType(makeOpenxriType(eppXriServiceEndpointType));
			for (EppXriServiceEndpointMediaType eppXriServiceEndpointMediaType : (List<EppXriServiceEndpointMediaType>) eppXriServiceEndpoint.getMediaType()) service.addMediaType(makeOpenxriMediaType(eppXriServiceEndpointMediaType));
			for (EppXriURI eppXriURI : (List<EppXriURI>) eppXriServiceEndpoint.getURI()) service.addURI(makeOpenxriUri(eppXriURI));
			for (EppXriRef eppXriRef : (List<EppXriRef>) eppXriServiceEndpoint.getRef()) service.addRef(makeOpenxriRef(eppXriRef));
			for (EppXriURI eppXriURI : (List<EppXriURI>) eppXriServiceEndpoint.getRedirect()) service.addRedirect(makeOpenxriRedirect(eppXriURI));
			for (EppXriSynonym eppXriSynonym : (List<EppXriSynonym>) eppXriServiceEndpoint.getLocalID()) service.addLocalID(makeOpenxriLocalID(eppXriSynonym));
			if (eppXriServiceEndpoint.getPriority() != -1) service.setPriority(new Integer(eppXriServiceEndpoint.getPriority()));
			if (eppXriServiceEndpoint.getExtension() != null) service.setExtension(eppXriServiceEndpoint.getExtension());
			if (eppXriServiceEndpoint.getAuthority() != null) service.setProviderId(eppXriServiceEndpoint.getAuthority());
		} catch (Exception ex) {

			throw new EppToolsException("Cannot convert service endpoint: " + ex.getMessage(), ex);
		}

		service.getOtherChildrenVectorMap().remove(TAG_GRS_ID);
		service.setOtherTagValues(TAG_GRS_ID, "<" + TAG_GRS_ID + ">" + eppXriServiceEndpoint.getId() + "</" + TAG_GRS_ID + ">");

		return(service);
	}

	public static SEPPath makeOpenxriPath(EppXriServiceEndpointPath eppXriServiceEndpointPath) {

		String path = eppXriServiceEndpointPath.getPath();
		if (path != null && path.trim().equals("")) path = null;

		String match = eppXriServiceEndpointPath.getMatch();
		if (match != null && match.trim().equals("")) match = null;

		// TODO: the toolkit doesn't tell us if the select attribute exists or not
		Boolean select = new Boolean(eppXriServiceEndpointPath.getSelect());

		return(new SEPPath(path, match, select));
	}

	public static SEPType makeOpenxriType(EppXriServiceEndpointType eppXriServiceEndpointType) {

		String type = eppXriServiceEndpointType.getType();
		if (type != null && type.trim().equals("")) type = null;

		String match = eppXriServiceEndpointType.getMatch();
		if (match != null && match.trim().equals("")) match = null;

		// TODO: the toolkit doesn't tell us if the select attribute exists or not
		Boolean select = new Boolean(eppXriServiceEndpointType.getSelect());

		return(new SEPType(type, match, select)); 
	}

	public static SEPMediaType makeOpenxriMediaType(EppXriServiceEndpointMediaType eppXriServiceEndpointMediaType) {

		String mediaType = eppXriServiceEndpointMediaType.getMediaType();
		if (mediaType != null && mediaType.trim().equals("")) mediaType = null;

		String match = eppXriServiceEndpointMediaType.getMatch();
		if (match != null && match.trim().equals("")) match = null;

		// TODO: the toolkit doesn't tell us if the select attribute exists or not
		Boolean select = new Boolean(eppXriServiceEndpointMediaType.getSelect());

		return(new SEPMediaType(mediaType, match, select)); 
	}

	public static SEPUri makeOpenxriUri(EppXriURI eppXriURI) {

		try {

			SEPUri uri = new SEPUri(eppXriURI.getURI());
			if (eppXriURI.getPriority() != -1) uri.setPriority(eppXriURI.getPriority());
			if (eppXriURI.getAppend() != null) uri.setAppend(eppXriURI.getAppend());
			return(uri);
		} catch (URISyntaxException ex) {

			throw new RuntimeException(ex);
		} 
	}

	public static Ref makeOpenxriRef(EppXriRef eppXriRef) {

		Ref ref = new Ref(eppXriRef.getRef());
		if (eppXriRef.getPriority() != -1) ref.setPriority(new Integer(eppXriRef.getPriority()));
		return(ref);
	}

	public static Redirect makeOpenxriRedirect(EppXriURI eppXriURI) {

		Redirect redirect = new Redirect(eppXriURI.getURI());
		if (eppXriURI.getPriority() != -1) redirect.setPriority(new Integer(eppXriURI.getPriority()));
		if (eppXriURI.getAppend() != null) redirect.setAppend(eppXriURI.getAppend());
		return(redirect);
	}

	public static EquivID makeOpenxriEquivID(EppXriSynonym eppXriSynonym) {

		EquivID equivID = new EquivID(eppXriSynonym.getSynonym());
		if (eppXriSynonym.getPriority() != -1) equivID.setPriority(new Integer(eppXriSynonym.getPriority()));
		return(equivID);
	}

	public static LocalID makeOpenxriLocalID(EppXriSynonym eppXriSynonym) {

		LocalID localID = new LocalID(eppXriSynonym.getSynonym());
		if (eppXriSynonym.getPriority() != -1) localID.setPriority(new Integer(eppXriSynonym.getPriority()));
		return(localID);
	}
}
