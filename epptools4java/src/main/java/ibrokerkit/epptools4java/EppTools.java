package ibrokerkit.epptools4java;

import ibrokerkit.epptools4java.store.Store;
import ibrokerkit.epptools4java.store.StoreException;
import ibrokerkit.epptools4java.store.impl.db.DatabaseStore;

import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.security.Security;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Random;
import java.util.TimeZone;
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
import org.openxri.xml.XRD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.neulevel.epp.core.EppAddress;
import com.neulevel.epp.core.EppAuthInfo;
import com.neulevel.epp.core.EppContactData;
import com.neulevel.epp.core.EppError;
import com.neulevel.epp.core.EppGenericNVPairs;
import com.neulevel.epp.core.EppGreeting;
import com.neulevel.epp.core.EppObject;
import com.neulevel.epp.core.EppPeriod;
import com.neulevel.epp.core.EppUnspec;
import com.neulevel.epp.core.command.EppCommand;
import com.neulevel.epp.core.command.EppCommandCheck;
import com.neulevel.epp.core.command.EppCommandCreate;
import com.neulevel.epp.core.command.EppCommandDelete;
import com.neulevel.epp.core.command.EppCommandLogin;
import com.neulevel.epp.core.command.EppCommandPoll;
import com.neulevel.epp.core.command.EppCommandTransfer;
import com.neulevel.epp.core.command.EppCreds;
import com.neulevel.epp.core.response.EppResponse;
import com.neulevel.epp.core.response.EppResponseData;
import com.neulevel.epp.core.response.EppResponseDataInfo;
import com.neulevel.epp.core.response.EppResult;
import com.neulevel.epp.transport.EppChannel;
import com.neulevel.epp.transport.EppSession;
import com.neulevel.epp.transport.tcp.EppSessionTcp;
import com.neulevel.epp.xri.EppXriAuthority;
import com.neulevel.epp.xri.EppXriName;
import com.neulevel.epp.xri.EppXriNumber;
import com.neulevel.epp.xri.EppXriRef;
import com.neulevel.epp.xri.EppXriServiceEndpoint;
import com.neulevel.epp.xri.EppXriServiceEndpointMediaType;
import com.neulevel.epp.xri.EppXriServiceEndpointPath;
import com.neulevel.epp.xri.EppXriServiceEndpointType;
import com.neulevel.epp.xri.EppXriSocialData;
import com.neulevel.epp.xri.EppXriSynonym;
import com.neulevel.epp.xri.EppXriTrustee;
import com.neulevel.epp.xri.EppXriURI;
import com.neulevel.epp.xri.command.EppCommandInfoXriAuthority;
import com.neulevel.epp.xri.command.EppCommandInfoXriName;
import com.neulevel.epp.xri.command.EppCommandInfoXriNumber;
import com.neulevel.epp.xri.command.EppCommandRenewXriName;
import com.neulevel.epp.xri.command.EppCommandRenewXriNumber;
import com.neulevel.epp.xri.command.EppCommandTransferXriAuthority;
import com.neulevel.epp.xri.command.EppCommandTransferXriName;
import com.neulevel.epp.xri.command.EppCommandUpdateXriAuthority;
import com.neulevel.epp.xri.response.EppResponseDataCheckXriAuthority;
import com.neulevel.epp.xri.response.EppResponseDataCheckXriName;
import com.neulevel.epp.xri.response.EppResponseDataCheckXriNumber;
import com.neulevel.epp.xri.response.EppResponseDataCreateXriAuthority;
import com.neulevel.epp.xri.response.EppResponseDataCreateXriName;
import com.neulevel.epp.xri.response.EppResponseDataCreateXriNumber;
import com.neulevel.epp.xri.response.EppResponseDataRenewXriName;
import com.neulevel.epp.xri.response.EppResponseDataRenewXriNumber;
import com.neulevel.epp.xri.response.EppResponseDataTransferXriAuthority;
import com.neulevel.epp.xri.response.EppResponseDataTransferXriName;

public class EppTools implements Serializable {

	private static final long serialVersionUID = 3837202598036526233L;

	private static final Logger log = LoggerFactory.getLogger(EppTools.class.getName());

	private static final String TAG_GRS_ID = "grsid";

	private static final int NUM_SESSIONS = 5;

	public static SimpleDateFormat xmlDateFormat;
	public static SimpleDateFormat xmlShortDateFormat;

	private static Random random;

	private Properties properties;
	private transient Store store;

	private transient EppSession[] eppSessionEqual, eppSessionAt;
	private transient EppChannel[] eppChannelEqual, eppChannelAt;
	private Boolean[] eppBlockedEqual, eppBlockedAt;

	private final List<EppListener> eppListeners;

	static {

		xmlDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
		xmlDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		xmlShortDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		xmlShortDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		random = new Random();
	}

	public EppTools(Properties properties) {

		this.properties = properties;

		this.eppSessionEqual = new EppSession[NUM_SESSIONS];
		this.eppSessionAt = new EppSession[NUM_SESSIONS];
		this.eppChannelEqual = new EppChannel[NUM_SESSIONS];
		this.eppChannelAt = new EppChannel[NUM_SESSIONS];
		this.eppBlockedEqual = new Boolean[NUM_SESSIONS];
		this.eppBlockedAt = new Boolean[NUM_SESSIONS];

		this.eppListeners = new ArrayList<EppListener> ();
	}

	/**
	 * Init everything and start a session
	 */
	public synchronized void init() throws Exception {

		log.debug("init()");

		Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());

		// init the first EPP session. additional sessions can be opened later.

		try {

			this.beginSessionEqual(null, 0);
		} catch (Exception ex) {

			log.warn("Cannot initialize = session 0.");
		}

		try {

			this.beginSessionAt(null, 0);
		} catch (Exception ex) {

			log.warn("Cannot initialize @ session 0.");
		}

		// init store

		if (this.store == null) {

			this.store = new DatabaseStore(this.properties);
			this.store.init();
		}

		log.debug("Done.");
	}

	/**
	 * Shut down
	 */
	public void close() {

		// close EPP session

		for (int i=0; i<this.eppSessionEqual.length; i++) this.endSessionEqual(i);
		for (int i=0; i<this.eppSessionAt.length; i++) this.endSessionAt(i);

		// close store

		this.store.close();
	}

	/**
	 * Change the password
	 * @param newPassword
	 */
	public synchronized void changePassword(String newPassword) throws EppToolsException {

		try {

			this.endSessionEqual(0);
			this.beginSessionEqual(newPassword, 0);

			this.endSessionAt(0);
			this.beginSessionAt(newPassword, 0);
		} catch (Exception ex) {

			throw new EppToolsException(ex.getMessage(), ex);
		}
	}

	public boolean isOwnClientId(String clientId) {

		return(clientId.equals(this.properties.getProperty("epp-clientid")));
	}

	/*
	 * Methods for epp structures
	 */

	public String makeGrsAuthorityId(char gcs, String authorityId) {

		StringBuffer buffer = new StringBuffer();
		buffer.append(gcs);
		buffer.append("!(");
		buffer.append(this.properties.getProperty("epp-network"));
		buffer.append("!0");
		buffer.append("!" + authorityId);
		buffer.append(")");

		return(buffer.toString());
	}

	public String makeGrsAuthorityId(char gcs) {

		StringBuffer buffer = new StringBuffer();
		for (int i=0; i<4; i++) buffer.append(Integer.toString(random.nextInt(10)));
		buffer.append(".");
		for (int i=0; i<4; i++) buffer.append(Integer.toString(random.nextInt(10)));
		buffer.append(".");
		for (int i=0; i<4; i++) buffer.append(Integer.toString(random.nextInt(10)));
		buffer.append(".");
		for (int i=0; i<4; i++) buffer.append(Integer.toString(random.nextInt(10)));

		return makeGrsAuthorityId(gcs, buffer.toString());
	}

	private static final String GRS_AUTHORITYPASSWORD_PREFIX = "";
	private static final String GRS_AUTHORITYPASSWORD_CHARS = "12345678901234567890abcdefghijklmnopqrstuvwxyz";
	private static final int GRS_AUTHORITYPASSWORD_LENGTH = 16;

	public static String makeGrsAuthorityPassword() {

		StringBuffer buffer = new StringBuffer();

		buffer.append(GRS_AUTHORITYPASSWORD_PREFIX);
		for (int i=0; i<GRS_AUTHORITYPASSWORD_LENGTH; i++) buffer.append(GRS_AUTHORITYPASSWORD_CHARS.charAt(random.nextInt(GRS_AUTHORITYPASSWORD_CHARS.length())));

		return(buffer.toString());
	}

	public static String makeGrsServiceId() {

		StringBuffer buffer = new StringBuffer();
		buffer.append("sep-");
		buffer.append(Integer.toString(Math.abs(random.nextInt())));

		return(buffer.toString());
	}

	public static EppXriSocialData makeEppXriSocialData(String[] street, String city, String state, String postalCode, String countryCode, String name, String organization, String primaryVoice, String secondaryVoice, String fax, String primaryEmail, String secondaryEmail, String pager) {

		EppAddress eppAddress = new EppAddress();
		if (street != null) eppAddress.setStreet(street); else eppAddress.setStreet(new String[] { "-" });
		if (city != null) eppAddress.setCity(city); else eppAddress.setCity("-");
		if (state != null) eppAddress.setState(state); else eppAddress.setState("-");
		if (postalCode != null) eppAddress.setPostalCode(postalCode); else eppAddress.setPostalCode("-");
		if (countryCode != null) eppAddress.setCountryCode(countryCode); else eppAddress.setCountryCode("-");

		EppContactData eppContactData = new EppContactData();
		eppContactData.setAddress(eppAddress);
		if (name != null) eppContactData.setName(name); else eppContactData.setName("-");
		if (organization != null) eppContactData.setOrganization(organization); else eppContactData.setOrganization("-");

		EppXriSocialData eppXriSocialData = new EppXriSocialData();
		eppXriSocialData.setPostalInfo(eppContactData);
		if (primaryVoice != null) eppXriSocialData.setPrimaryVoice(fixPhone(primaryVoice)); else eppXriSocialData.setPrimaryVoice("+1.0000000");
		if (secondaryVoice != null) eppXriSocialData.setSecondaryVoice(fixPhone(secondaryVoice)); else eppXriSocialData.setSecondaryVoice("+1.0000000");
		if (fax != null) eppXriSocialData.setFax(fixPhone(fax)); else eppXriSocialData.setFax("+1.0000000");
		if (primaryEmail != null) eppXriSocialData.setPrimaryEmail(primaryEmail); else eppXriSocialData.setPrimaryEmail("noemail@noemail.com");
		if (secondaryEmail != null) eppXriSocialData.setSecondaryEmail(secondaryEmail); else eppXriSocialData.setSecondaryEmail("noemail@noemail.com");
		if (pager != null) eppXriSocialData.setPager(fixPhone(pager)); else eppXriSocialData.setPager("+1.0000000");

		return(eppXriSocialData);
	}

	private static String fixPhone(String phone) {

		String fixedPhone = phone.trim();

		if (! fixedPhone.startsWith("+")) fixedPhone = "+1 " + fixedPhone;
		fixedPhone = fixedPhone.replaceAll("[ \\.\\-/:()]+", " ");
		fixedPhone = fixedPhone.replaceFirst(" ", ".");
		fixedPhone = fixedPhone.replaceAll(" ", "");

		return(fixedPhone);
	}

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

			id = makeGrsServiceId();
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

	@SuppressWarnings("unchecked")
	public static Service makeService(EppXriServiceEndpoint eppXriServiceEndpoint) throws EppToolsException {

		Service service = new Service();

		try {

			for (EppXriServiceEndpointPath eppXriServiceEndpointPath : (List<EppXriServiceEndpointPath>) eppXriServiceEndpoint.getPath()) service.addPath(makePath(eppXriServiceEndpointPath));
			for (EppXriServiceEndpointType eppXriServiceEndpointType : (List<EppXriServiceEndpointType>) eppXriServiceEndpoint.getType()) service.addType(makeType(eppXriServiceEndpointType));
			for (EppXriServiceEndpointMediaType eppXriServiceEndpointMediaType : (List<EppXriServiceEndpointMediaType>) eppXriServiceEndpoint.getMediaType()) service.addMediaType(makeMediaType(eppXriServiceEndpointMediaType));
			for (EppXriURI eppXriURI : (List<EppXriURI>) eppXriServiceEndpoint.getURI()) service.addURI(makeUri(eppXriURI));
			for (EppXriRef eppXriRef : (List<EppXriRef>) eppXriServiceEndpoint.getRef()) service.addRef(makeRef(eppXriRef));
			for (EppXriURI eppXriURI : (List<EppXriURI>) eppXriServiceEndpoint.getRedirect()) service.addRedirect(makeRedirect(eppXriURI));
			for (EppXriSynonym eppXriSynonym : (List<EppXriSynonym>) eppXriServiceEndpoint.getLocalID()) service.addLocalID(makeLocalID(eppXriSynonym));
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

	public static SEPPath makePath(EppXriServiceEndpointPath eppXriServiceEndpointPath) {

		String path = eppXriServiceEndpointPath.getPath();
		if (path != null && path.trim().equals("")) path = null;

		String match = eppXriServiceEndpointPath.getMatch();
		if (match != null && match.trim().equals("")) match = null;

		// TODO: the toolkit doesn't tell us if the select attribute exists or not
		Boolean select = new Boolean(eppXriServiceEndpointPath.getSelect());

		return(new SEPPath(path, match, select));
	}

	public static SEPType makeType(EppXriServiceEndpointType eppXriServiceEndpointType) {

		String type = eppXriServiceEndpointType.getType();
		if (type != null && type.trim().equals("")) type = null;

		String match = eppXriServiceEndpointType.getMatch();
		if (match != null && match.trim().equals("")) match = null;

		// TODO: the toolkit doesn't tell us if the select attribute exists or not
		Boolean select = new Boolean(eppXriServiceEndpointType.getSelect());

		return(new SEPType(type, match, select)); 
	}

	public static SEPMediaType makeMediaType(EppXriServiceEndpointMediaType eppXriServiceEndpointMediaType) {

		String mediaType = eppXriServiceEndpointMediaType.getMediaType();
		if (mediaType != null && mediaType.trim().equals("")) mediaType = null;

		String match = eppXriServiceEndpointMediaType.getMatch();
		if (match != null && match.trim().equals("")) match = null;

		// TODO: the toolkit doesn't tell us if the select attribute exists or not
		Boolean select = new Boolean(eppXriServiceEndpointMediaType.getSelect());

		return(new SEPMediaType(mediaType, match, select)); 
	}

	public static SEPUri makeUri(EppXriURI eppXriURI) {

		try {

			SEPUri uri = new SEPUri(eppXriURI.getURI());
			if (eppXriURI.getPriority() != -1) uri.setPriority(eppXriURI.getPriority());
			if (eppXriURI.getAppend() != null) uri.setAppend(eppXriURI.getAppend());
			return(uri);
		} catch (URISyntaxException ex) {

			throw new RuntimeException(ex);
		} 
	}

	public static Ref makeRef(EppXriRef eppXriRef) {

		Ref ref = new Ref(eppXriRef.getRef());
		if (eppXriRef.getPriority() != -1) ref.setPriority(new Integer(eppXriRef.getPriority()));
		return(ref);
	}

	public static Redirect makeRedirect(EppXriURI eppXriURI) {

		Redirect redirect = new Redirect(eppXriURI.getURI());
		if (eppXriURI.getPriority() != -1) redirect.setPriority(new Integer(eppXriURI.getPriority()));
		if (eppXriURI.getAppend() != null) redirect.setAppend(eppXriURI.getAppend());
		return(redirect);
	}

	public static EquivID makeEquivID(EppXriSynonym eppXriSynonym) {

		EquivID equivID = new EquivID(eppXriSynonym.getSynonym());
		if (eppXriSynonym.getPriority() != -1) equivID.setPriority(new Integer(eppXriSynonym.getPriority()));
		return(equivID);
	}

	public static LocalID makeLocalID(EppXriSynonym eppXriSynonym) {

		LocalID localID = new LocalID(eppXriSynonym.getSynonym());
		if (eppXriSynonym.getPriority() != -1) localID.setPriority(new Integer(eppXriSynonym.getPriority()));
		return(localID);
	}

	/*
	 * Methods for polling
	 */

	public EppResponseData poll(char gcs, boolean ack) throws EppToolsException {

		log.debug("poll(gcs=" + gcs + ", ack=" + ack + ")");

		EppCommandPoll eppCommandPoll = new EppCommandPoll(this.generateTransactionId());
		eppCommandPoll.setOperation(EppCommandPoll.OPTYPE_REQ);

		EppResponse eppResponse = this.send(gcs, eppCommandPoll);

		if (eppResponse.getMessageQueued() < 1) return(null);
		String messageId = eppResponse.getMessageId();

		EppResponseData eppResponseData = eppResponse.getResponseData();
		if (eppResponseData == null) throw new EppToolsException("No response data");

		// log and ack it?

		if (ack) {

			// log the poll

			try {

				this.store.createPoll(new Character(gcs), eppResponse.getTransactionId().getClientTransactionId(), eppResponse.toString());
			} catch (StoreException ex2) {

				log.error("Cannot store failed EPP action: " + ex2.getMessage(), ex2);
			}

			// ack the poll

			eppCommandPoll = new EppCommandPoll(this.generateTransactionId());
			eppCommandPoll.setOperation(EppCommandPoll.OPTYPE_ACK);
			eppCommandPoll.setMessageId(messageId);

			this.send(gcs, eppCommandPoll);
		}

		return(eppResponseData);
	}

	/*
	 * Methods for authorities
	 */

	public EppResponseDataCreateXriAuthority createAuthority(char gcs, String authId, String password, String inumber, String iname, EppXriSocialData eppXriSocialData, String trusteeEscrowAgent, String trusteeContactAgent, Map<String, String> extension) throws EppToolsException {

		EppXriTrustee eppXriTrusteeEscrowAgent = new EppXriTrustee();
		eppXriTrusteeEscrowAgent.setAuthorityId(trusteeEscrowAgent != null ? trusteeEscrowAgent : this.properties.getProperty("epp-trusteeescrowagent"));

		EppXriTrustee eppXriTrusteeContactAgent = new EppXriTrustee();
		eppXriTrusteeContactAgent.setAuthorityId(trusteeContactAgent != null ? trusteeContactAgent : this.properties.getProperty("epp-trusteecontactagent"));

		EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, password != null ? password : makeGrsAuthorityPassword());

		EppXriAuthority eppXriAuthority = new EppXriAuthority(authId != null ? authId : this.makeGrsAuthorityId(gcs));

		eppXriAuthority.setEscrowAgent(eppXriTrusteeEscrowAgent);
		eppXriAuthority.setContactAgent(eppXriTrusteeContactAgent);
		eppXriAuthority.setSocialData(eppXriSocialData);
		eppXriAuthority.setAuthInfo(eppAuthInfo);

		if (inumber != null) eppXriAuthority.addINumber(inumber);
		if (iname != null) eppXriAuthority.addIName(iname);

		EppCommandCreate eppCommandCreate = EppCommand.create(eppXriAuthority, this.generateTransactionId());

		if (extension != null) {

			EppUnspec unspec = new EppUnspec();
			EppGenericNVPairs nvPairs = new EppGenericNVPairs();
			for (Entry<String, String> extensionEntry : extension.entrySet()) nvPairs.addGenericNVPair(extensionEntry.getKey(), extensionEntry.getValue());
			unspec.setGenericNVPairs(nvPairs);
			eppCommandCreate.setUnspec(unspec);
		}

		EppResponse eppResponse = this.send(gcs, eppCommandCreate);

		EppResponseDataCreateXriAuthority eppResponseData = (EppResponseDataCreateXriAuthority) eppResponse.getResponseData();
		if (eppResponseData == null) throw new EppToolsException("No response data");

		return(eppResponseData);
	}

	public EppResponseDataCreateXriAuthority createAuthority(char gcs, String authId, String password, String inumber, String iname, EppXriSocialData eppXriSocialData, String trusteeEscrowAgent, String trusteeContactAgent) throws EppToolsException {

		return this.createAuthority(gcs, authId, password, inumber, iname, eppXriSocialData, trusteeEscrowAgent, trusteeContactAgent, null);
	}

	public EppResponseDataCreateXriAuthority createAuthority(char gcs, String authId, String password, EppXriSocialData eppXriSocialData, String trusteeEscrowAgent, String trusteeContactAgent, Map<String, String> extension) throws EppToolsException {

		return this.createAuthority(gcs, authId, password, null, null, eppXriSocialData, trusteeEscrowAgent, trusteeContactAgent, extension);
	}

	public EppResponseDataCreateXriAuthority createAuthority(char gcs, String authId, String password, EppXriSocialData eppXriSocialData, String trusteeEscrowAgent, String trusteeContactAgent) throws EppToolsException {

		return this.createAuthority(gcs, authId, password, eppXriSocialData, trusteeEscrowAgent, trusteeContactAgent, null);
	}

	public void deleteAuthority(char gcs, String authId) throws EppToolsException {

		EppCommandDelete eppCommandDelete = EppCommand.delete(EppObject.XRI_AUTHORITY, authId, this.generateTransactionId());

		this.send(gcs, eppCommandDelete);
	}

	public boolean checkAuthority(char gcs, String authId) throws EppToolsException {

		EppCommandCheck eppCommandCheck = EppCommand.check(EppObject.XRI_AUTHORITY, this.generateTransactionId());
		eppCommandCheck.add(authId);

		EppResponse eppResponse = this.send(gcs, eppCommandCheck);

		EppResponseDataCheckXriAuthority eppResponseData = (EppResponseDataCheckXriAuthority) eppResponse.getResponseData();
		if (eppResponseData == null) throw new EppToolsException("No response data");

		return(! eppResponseData.isAvailable(authId));
	}

	public EppXriAuthority infoAuthority(char gcs, String authId, boolean all) throws EppToolsException {

		EppCommandInfoXriAuthority eppCommandInfo = (EppCommandInfoXriAuthority) EppCommand.info(EppObject.XRI_AUTHORITY, authId, this.generateTransactionId());
		if (all) eppCommandInfo.setControl(EppCommandInfoXriAuthority.CONTROL_ALL);

		EppResponse eppResponse;

		try {

			eppResponse = this.send(gcs, eppCommandInfo);
		} catch (EppToolsUnsuccessfulException ex) {

			if (ex.getResult().getCode() == EppError.CODE_OBJECT_DOES_NOT_EXIST) return(null);

			throw ex;
		}

		EppResponseDataInfo eppResponseData = (EppResponseDataInfo) eppResponse.getResponseData();
		if (eppResponseData == null) throw new EppToolsException("No response data");

		return((EppXriAuthority) eppResponseData.getObject());
	}

	public String transferRequestAuthority(char gcs, String authId) throws EppToolsException {

		EppCommandTransferXriAuthority eppCommandTransfer = (EppCommandTransferXriAuthority) EppCommand.transfer(EppObject.XRI_AUTHORITY, authId, this.generateTransactionId());
		eppCommandTransfer.setOperation(EppCommandTransfer.OPTYPE_REQUEST);

		EppResponse eppResponse = this.send(gcs, eppCommandTransfer);

		EppResponseDataTransferXriAuthority eppResponseData = (EppResponseDataTransferXriAuthority) eppResponse.getResponseData();
		if (eppResponseData == null) throw new EppToolsException("No response data");
		if (eppResponseData.getTransferToken() == null) throw new EppToolsException("No transfer token");

		return(eppResponseData.getTransferToken());
	}

	public void transferApproveAuthority(char gcs, String authId, String password, String transferToken) throws EppToolsException {

		EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, password);

		EppCommandTransferXriAuthority eppCommandTransfer = (EppCommandTransferXriAuthority) EppCommand.transfer(EppObject.XRI_AUTHORITY, authId, this.generateTransactionId());
		eppCommandTransfer.setOperation(EppCommandTransfer.OPTYPE_APPROVE);
		eppCommandTransfer.setAuthInfo(eppAuthInfo);
		eppCommandTransfer.setTransferToken(transferToken);

		this.send(gcs, eppCommandTransfer);
	}

	public void transferRejectAuthority(char gcs, String authId, String password, String transferToken) throws EppToolsException {

		EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, password);

		EppCommandTransferXriAuthority eppCommandTransfer = (EppCommandTransferXriAuthority) EppCommand.transfer(EppObject.XRI_AUTHORITY, authId, this.generateTransactionId());
		eppCommandTransfer.setOperation(EppCommandTransfer.OPTYPE_REJECT);
		eppCommandTransfer.setAuthInfo(eppAuthInfo);
		eppCommandTransfer.setTransferToken(transferToken);

		this.send(gcs, eppCommandTransfer);
	}

	public EppResponseDataTransferXriAuthority transferQueryAuthority(char gcs, String authId, String password) throws EppToolsException {

		EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, password);

		EppCommandTransfer eppCommandTransfer = EppCommand.transfer(EppObject.XRI_AUTHORITY, authId, this.generateTransactionId());
		eppCommandTransfer.setOperation(EppCommandTransfer.OPTYPE_QUERY);
		eppCommandTransfer.setAuthInfo(eppAuthInfo);

		EppResponse eppResponse = this.send(gcs, eppCommandTransfer);

		EppResponseDataTransferXriAuthority eppResponseData = (EppResponseDataTransferXriAuthority) eppResponse.getResponseData();
		if (eppResponseData == null) throw new EppToolsException("No response data");

		return(eppResponseData);
	}

	/*
	 * Methods for authority data
	 */

	public void initAuthorityFromXrd(char gcs, String authId, String password, XRD xrd) throws EppToolsException {

		EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, password);

		EppCommandUpdateXriAuthority eppCommandUpdate = (EppCommandUpdateXriAuthority) EppCommand.update(EppObject.XRI_AUTHORITY, authId, this.generateTransactionId());
		eppCommandUpdate.setCanonicalEquivID(xrd.getCanonicalEquivID().getValue());
		for (int i=0; i<xrd.getNumEquivIDs(); i++) eppCommandUpdate.addEquivID(makeEppXriSynonym(xrd.getEquivIDAt(i)));
		for (int i=0; i<xrd.getNumRefs(); i++) eppCommandUpdate.addRef(makeEppXriRef(xrd.getRefAt(i)));
		for (int i=0; i<xrd.getNumRedirects(); i++) eppCommandUpdate.addRedirect(makeEppXriURI(xrd.getRedirectAt(i)));
		for (int i=0; i<xrd.getNumServices(); i++) eppCommandUpdate.addServiceEndpoint(makeEppXriServiceEndpoint(xrd.getServiceAt(i)));
		eppCommandUpdate.setAuthInfo(eppAuthInfo);

		this.send(gcs, eppCommandUpdate);
	}

	public void setSocialData(char gcs, String authId, String password, EppXriSocialData eppXriSocialData) throws EppToolsException {

		EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, password);

		EppCommandUpdateXriAuthority eppCommandUpdate = (EppCommandUpdateXriAuthority) EppCommand.update(EppObject.XRI_AUTHORITY, authId, this.generateTransactionId());
		eppCommandUpdate.setNewSocialData(eppXriSocialData);
		eppCommandUpdate.setAuthInfo(eppAuthInfo);

		this.send(gcs, eppCommandUpdate);
	}

	public void setSocialData(char gcs, String authId, String password, String[] street, String city, String state, String postalCode, String countryCode, String name, String organization, String primaryVoice, String secondaryVoice, String fax, String primaryEmail, String secondaryEmail, String pager) throws EppToolsException {

		EppXriSocialData eppXriSocialData = makeEppXriSocialData(street, city, state, postalCode, countryCode, name, organization, primaryVoice, secondaryVoice, fax, primaryEmail, secondaryEmail, pager);

		this.setSocialData(gcs, authId, password, eppXriSocialData);
	}

	public void setCanonicalEquivID(char gcs, String authId, String password, CanonicalEquivID canonicalEquivID) throws EppToolsException {

		EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, password);

		EppCommandUpdateXriAuthority eppCommandUpdate = (EppCommandUpdateXriAuthority) EppCommand.update(EppObject.XRI_AUTHORITY, authId, this.generateTransactionId());
		eppCommandUpdate.setCanonicalEquivID(canonicalEquivID.getValue());
		eppCommandUpdate.setAuthInfo(eppAuthInfo);

		this.send(gcs, eppCommandUpdate);
	}

	public void setExtension(char gcs, String authId, String password, String extension) throws EppToolsException {

		EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, password);

		EppCommandUpdateXriAuthority eppCommandUpdate = (EppCommandUpdateXriAuthority) EppCommand.update(EppObject.XRI_AUTHORITY, authId, this.generateTransactionId());
		eppCommandUpdate.setNewExtension(extension);
		eppCommandUpdate.setAuthInfo(eppAuthInfo);

		this.send(gcs, eppCommandUpdate);
	}

	public void addEquivIDs(char gcs, String authId, String password, EquivID[] equivIDs) throws EppToolsException {

		EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, password);

		EppCommandUpdateXriAuthority eppCommandUpdate = (EppCommandUpdateXriAuthority) EppCommand.update(EppObject.XRI_AUTHORITY, authId, this.generateTransactionId());
		for (EquivID equivID : equivIDs) eppCommandUpdate.addEquivID(makeEppXriSynonym(equivID));
		eppCommandUpdate.setAuthInfo(eppAuthInfo);

		this.send(gcs, eppCommandUpdate);
	}

	public void addRefs(char gcs, String authId, String password, Ref[] refs) throws EppToolsException {

		EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, password);

		EppCommandUpdateXriAuthority eppCommandUpdate = (EppCommandUpdateXriAuthority) EppCommand.update(EppObject.XRI_AUTHORITY, authId, this.generateTransactionId());
		for (Ref ref : refs) eppCommandUpdate.addRef(makeEppXriRef(ref));
		eppCommandUpdate.setAuthInfo(eppAuthInfo);

		this.send(gcs, eppCommandUpdate);
	}

	public void addRedirects(char gcs, String authId, String password, Redirect[] redirects) throws EppToolsException {

		EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, password);

		EppCommandUpdateXriAuthority eppCommandUpdate = (EppCommandUpdateXriAuthority) EppCommand.update(EppObject.XRI_AUTHORITY, authId, this.generateTransactionId());
		for (Redirect redirect : redirects) eppCommandUpdate.addRedirect(makeEppXriURI(redirect));
		eppCommandUpdate.setAuthInfo(eppAuthInfo);

		this.send(gcs, eppCommandUpdate);
	}

	public void addServices(char gcs, String authId, String password, Service[] services) throws EppToolsException {

		EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, password);

		EppCommandUpdateXriAuthority eppCommandUpdate = (EppCommandUpdateXriAuthority) EppCommand.update(EppObject.XRI_AUTHORITY, authId, this.generateTransactionId());
		for (Service service : services) eppCommandUpdate.addServiceEndpoint(makeEppXriServiceEndpoint(service));
		eppCommandUpdate.setAuthInfo(eppAuthInfo);

		this.send(gcs, eppCommandUpdate);
	}

	public void addDiscoveryKeys(char gcs, String authId, String password, String[] keys) throws EppToolsException {

		EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, password);

		EppCommandUpdateXriAuthority eppCommandUpdate = (EppCommandUpdateXriAuthority) EppCommand.update(EppObject.XRI_AUTHORITY, authId, this.generateTransactionId());
		for (String key : keys) eppCommandUpdate.addDiscoverykey(key);
		eppCommandUpdate.setAuthInfo(eppAuthInfo);

		this.send(gcs, eppCommandUpdate);
	}

	public void deleteCanonicalEquivID(char gcs, String authId, String password, CanonicalEquivID canonicalEquivID) throws EppToolsException {

		EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, password);

		EppCommandUpdateXriAuthority eppCommandUpdate = (EppCommandUpdateXriAuthority) EppCommand.update(EppObject.XRI_AUTHORITY, authId, this.generateTransactionId());
		eppCommandUpdate.removeCanonicalEquivID(canonicalEquivID.getValue());
		eppCommandUpdate.setAuthInfo(eppAuthInfo);

		this.send(gcs, eppCommandUpdate);
	}

	public void deleteEquivIDs(char gcs, String authId, String password, EquivID[] equivIDs) throws EppToolsException {

		EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, password);

		EppCommandUpdateXriAuthority eppCommandUpdate = (EppCommandUpdateXriAuthority) EppCommand.update(EppObject.XRI_AUTHORITY, authId, this.generateTransactionId());
		for (EquivID equivID : equivIDs) eppCommandUpdate.removeEquivID(equivID.getValue());
		eppCommandUpdate.setAuthInfo(eppAuthInfo);

		this.send(gcs, eppCommandUpdate);
	}

	public void deleteRefs(char gcs, String authId, String password, Ref[] refs) throws EppToolsException {

		EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, password);

		EppCommandUpdateXriAuthority eppCommandUpdate = (EppCommandUpdateXriAuthority) EppCommand.update(EppObject.XRI_AUTHORITY, authId, this.generateTransactionId());
		for (Ref ref : refs) eppCommandUpdate.removeRef(ref.getValue());
		eppCommandUpdate.setAuthInfo(eppAuthInfo);

		this.send(gcs, eppCommandUpdate);
	}

	public void deleteRedirects(char gcs, String authId, String password, Redirect[] redirects) throws EppToolsException {

		EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, password);

		EppCommandUpdateXriAuthority eppCommandUpdate = (EppCommandUpdateXriAuthority) EppCommand.update(EppObject.XRI_AUTHORITY, authId, this.generateTransactionId());
		for (Redirect redirect : redirects) eppCommandUpdate.removeRedirect(redirect.getValue());
		eppCommandUpdate.setAuthInfo(eppAuthInfo);

		this.send(gcs, eppCommandUpdate);
	}

	public void deleteServices(char gcs, String authId, String password, Service[] services) throws EppToolsException {

		EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, password);

		EppCommandUpdateXriAuthority eppCommandUpdate = (EppCommandUpdateXriAuthority) EppCommand.update(EppObject.XRI_AUTHORITY, authId, this.generateTransactionId());
		for (Service service : services) eppCommandUpdate.removeServiceEndpoint(((Element) service.getOtherTagValues(TAG_GRS_ID).get(0)).getTextContent());
		eppCommandUpdate.setAuthInfo(eppAuthInfo);

		this.send(gcs, eppCommandUpdate);
	}

	public void deleteDiscoveryKeys(char gcs, String authId, String password, String[] keys) throws EppToolsException {

		EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, password);

		EppCommandUpdateXriAuthority eppCommandUpdate = (EppCommandUpdateXriAuthority) EppCommand.update(EppObject.XRI_AUTHORITY, authId, this.generateTransactionId());
		for (String key : keys) eppCommandUpdate.removeDiscoverykey(key);
		eppCommandUpdate.setAuthInfo(eppAuthInfo);

		this.send(gcs, eppCommandUpdate);
	}

	/*
	 * Methods for i-numbers
	 */

	public EppResponseDataCreateXriNumber createInumber(char gcs, String authId, String referenceId, String inumber, int years, Map<String, String> extension) throws EppToolsException {

		EppPeriod eppPeriod = new EppPeriod(years, EppPeriod.UNIT_YEAR);

		EppXriNumber eppXriNumber = new EppXriNumber(inumber);
		eppXriNumber.setReferenceId(referenceId);
		eppXriNumber.setAuthorityId(authId);
		eppXriNumber.setPeriod(eppPeriod);

		EppCommandCreate eppCommandCreate = EppCommand.create(eppXriNumber, this.generateTransactionId());

		if (extension != null) {

			EppUnspec unspec = new EppUnspec();
			EppGenericNVPairs nvPairs = new EppGenericNVPairs();
			for (Entry<String, String> extensionEntry : extension.entrySet()) nvPairs.addGenericNVPair(extensionEntry.getKey(), extensionEntry.getValue());
			unspec.setGenericNVPairs(nvPairs);
			eppCommandCreate.setUnspec(unspec);
		}

		EppResponse eppResponse = this.send(gcs, eppCommandCreate);

		EppResponseDataCreateXriNumber eppResponseData = (EppResponseDataCreateXriNumber) eppResponse.getResponseData();
		if (eppResponseData == null) throw new EppToolsException("No response data");

		return(eppResponseData);
	}

	public EppResponseDataCreateXriNumber createInumber(char gcs, String authId, String referenceId, String inumber, int years) throws EppToolsException {

		return this.createInumber(gcs, authId, referenceId, inumber, years, null);
	}

	public EppResponseDataCreateXriNumber createInumber(char gcs, String authId, String referenceId, int years, Map<String, String> extension) throws EppToolsException {

		return this.createInumber(gcs, authId, referenceId, null, years, extension);
	}

	public EppResponseDataCreateXriNumber createInumber(char gcs, String authId, String referenceId, int years) throws EppToolsException {

		return this.createInumber(gcs, authId, referenceId, years, null);
	}

	public void deleteInumber(char gcs, String inumber) throws EppToolsException {

		EppCommandDelete eppCommandDelete = EppCommand.delete(EppObject.XRI_INUMBER, inumber, this.generateTransactionId());

		this.send(gcs, eppCommandDelete);
	}

	public boolean checkInumber(char gcs, String inumber) throws EppToolsException {

		EppCommandCheck eppCommandCheck = EppCommand.check(EppObject.XRI_INUMBER, this.generateTransactionId());
		eppCommandCheck.add(inumber);

		EppResponse eppResponse = this.send(gcs, eppCommandCheck);

		EppResponseDataCheckXriNumber eppResponseData = (EppResponseDataCheckXriNumber) eppResponse.getResponseData();
		if (eppResponseData == null) throw new EppToolsException("No response data");

		return(! eppResponseData.isAvailable(inumber));
	}

	public EppXriNumber infoInumber(char gcs, String inumber) throws EppToolsException {

		EppCommandInfoXriNumber eppCommandInfo = (EppCommandInfoXriNumber) EppCommand.info(EppObject.XRI_INUMBER, inumber, this.generateTransactionId());

		EppResponse eppResponse;

		try {

			eppResponse = this.send(gcs, eppCommandInfo);
		} catch (EppToolsUnsuccessfulException ex) {

			if (ex.getResult().getCode() == EppError.CODE_OBJECT_DOES_NOT_EXIST) return(null);

			throw ex;
		}

		EppResponseDataInfo eppResponseData = (EppResponseDataInfo) eppResponse.getResponseData();
		if (eppResponseData == null) throw new EppToolsException("No response data");

		return((EppXriNumber) eppResponseData.getObject());
	}

	public Calendar renewInumber(char gcs, String inumber, Calendar curExpDate, int years) throws EppToolsException {

		EppPeriod eppPeriod = new EppPeriod(years, EppPeriod.UNIT_YEAR);

		EppCommandRenewXriNumber eppCommandRenew = (EppCommandRenewXriNumber) EppCommand.renew(EppObject.XRI_INUMBER, inumber, this.generateTransactionId());
		eppCommandRenew.setPeriod(eppPeriod);
		eppCommandRenew.setCurrentExpireDate(curExpDate);

		EppResponse eppResponse = this.send(gcs, eppCommandRenew);

		EppResponseDataRenewXriNumber eppResponseData = (EppResponseDataRenewXriNumber) eppResponse.getResponseData();
		if (eppResponseData == null) throw new EppToolsException("No response data");

		return(eppResponseData.getDateExpired());
	}

	/*
	 * Methods for i-names
	 */

	public EppResponseDataCreateXriName createIname(char gcs, String iname, String authId, int years, Map<String, String> extension) throws EppToolsException {

		EppPeriod eppPeriod = new EppPeriod(years, EppPeriod.UNIT_YEAR);

		EppXriName eppXriName = new EppXriName(iname);
		eppXriName.setAuthorityId(authId);
		eppXriName.setPeriod(eppPeriod);

		EppCommandCreate eppCommandCreate = EppCommand.create(eppXriName, this.generateTransactionId());

		if (extension != null) {

			EppUnspec unspec = new EppUnspec();
			EppGenericNVPairs nvPairs = new EppGenericNVPairs();
			for (Entry<String, String> extensionEntry : extension.entrySet()) nvPairs.addGenericNVPair(extensionEntry.getKey(), extensionEntry.getValue());
			unspec.setGenericNVPairs(nvPairs);
			eppCommandCreate.setUnspec(unspec);
		}

		EppResponse eppResponse = this.send(gcs, eppCommandCreate);

		EppResponseDataCreateXriName eppResponseData = (EppResponseDataCreateXriName) eppResponse.getResponseData();
		if (eppResponseData == null) throw new EppToolsException("No response data");

		return(eppResponseData);
	}

	public void deleteIname(char gcs, String iname) throws EppToolsException {

		EppCommandDelete eppCommandDelete = EppCommand.delete(EppObject.XRI_INAME, iname, this.generateTransactionId());

		this.send(gcs, eppCommandDelete);
	}

	public boolean checkIname(char gcs, String iname) throws EppToolsException {

		EppCommandCheck eppCommandCheck = EppCommand.check(EppObject.XRI_INAME, this.generateTransactionId());
		eppCommandCheck.add(iname);

		EppResponse eppResponse = this.send(gcs, eppCommandCheck);

		EppResponseDataCheckXriName eppResponseData = (EppResponseDataCheckXriName) eppResponse.getResponseData();
		if (eppResponseData == null) throw new EppToolsException("No response data");

		return(! eppResponseData.isAvailable(iname));
	}

	public EppXriName infoIname(char gcs, String iname) throws EppToolsException {

		EppCommandInfoXriName eppCommandInfo = (EppCommandInfoXriName) EppCommand.info(EppObject.XRI_INAME, iname, this.generateTransactionId());

		EppResponse eppResponse;

		try {

			eppResponse = this.send(gcs, eppCommandInfo);
		} catch (EppToolsUnsuccessfulException ex) {

			if (ex.getResult().getCode() == EppError.CODE_OBJECT_DOES_NOT_EXIST) return(null);

			throw ex;
		}

		EppResponseDataInfo eppResponseData = (EppResponseDataInfo) eppResponse.getResponseData();
		if (eppResponseData == null) throw new EppToolsException("No response data");

		return((EppXriName) eppResponseData.getObject());
	}

	public Calendar renewIname(char gcs, String iname, Calendar curExpDate, int years) throws EppToolsException {

		EppPeriod eppPeriod = new EppPeriod(years, EppPeriod.UNIT_YEAR);

		EppCommandRenewXriName eppCommandRenew = (EppCommandRenewXriName) EppCommand.renew(EppObject.XRI_INAME, iname, this.generateTransactionId());
		eppCommandRenew.setPeriod(eppPeriod);
		eppCommandRenew.setCurrentExpireDate(curExpDate);

		EppResponse eppResponse = this.send(gcs, eppCommandRenew);

		EppResponseDataRenewXriName eppResponseData = (EppResponseDataRenewXriName) eppResponse.getResponseData();
		if (eppResponseData == null) throw new EppToolsException("No response data");

		return(eppResponseData.getDateExpired());
	}

	public String transferRequestIname(char gcs, String iname, String authId, String password) throws EppToolsException {

		EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, password);

		EppCommandTransferXriName eppCommandTransfer = (EppCommandTransferXriName) EppCommand.transfer(EppObject.XRI_INAME, iname, this.generateTransactionId());
		eppCommandTransfer.setOperation(EppCommandTransfer.OPTYPE_REQUEST);
		eppCommandTransfer.setTarget(authId, eppAuthInfo);

		EppResponse eppResponse = this.send(gcs, eppCommandTransfer);

		EppResponseDataTransferXriAuthority eppResponseData = (EppResponseDataTransferXriAuthority) eppResponse.getResponseData();
		if (eppResponseData == null) throw new EppToolsException("No response data");
		if (eppResponseData.getTransferToken() == null) throw new EppToolsException("No transfer token");

		return(eppResponseData.getTransferToken());
	}

	public void transferApproveIname(char gcs, String iname, String password, String transferToken) throws EppToolsException {

		EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, password);

		EppCommandTransferXriName eppCommandTransfer = (EppCommandTransferXriName) EppCommand.transfer(EppObject.XRI_INAME, iname, this.generateTransactionId());
		eppCommandTransfer.setOperation(EppCommandTransfer.OPTYPE_APPROVE);
		eppCommandTransfer.setAuthInfo(eppAuthInfo);
		eppCommandTransfer.setTransferToken(transferToken);

		this.send(gcs, eppCommandTransfer);
	}

	public void transferRejectIname(char gcs, String iname, String password, String transferToken) throws EppToolsException {

		EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, password);

		EppCommandTransferXriName eppCommandTransfer = (EppCommandTransferXriName) EppCommand.transfer(EppObject.XRI_INAME, iname, this.generateTransactionId());
		eppCommandTransfer.setOperation(EppCommandTransfer.OPTYPE_REJECT);
		eppCommandTransfer.setAuthInfo(eppAuthInfo);
		eppCommandTransfer.setTransferToken(transferToken);

		this.send(gcs, eppCommandTransfer);
	}

	public EppResponseDataTransferXriName transferQueryIname(char gcs, String iname, String password) throws EppToolsException {

		EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, password);

		EppCommandTransfer eppCommandTransfer = EppCommand.transfer(EppObject.XRI_INAME, iname, this.generateTransactionId());
		eppCommandTransfer.setOperation(EppCommandTransfer.OPTYPE_QUERY);
		eppCommandTransfer.setAuthInfo(eppAuthInfo);

		EppResponse eppResponse = this.send(gcs, eppCommandTransfer);

		EppResponseDataTransferXriName eppResponseData = (EppResponseDataTransferXriName) eppResponse.getResponseData();
		if (eppResponseData == null) throw new EppToolsException("No response data");

		return(eppResponseData);
	}

	/*
	 * Helper methods for maintaining the session
	 */

	/**
	 * Start the session and log in.
	 */
	private synchronized void beginSessionEqual(String newPassword, int i) throws Exception {

		if (this.eppSessionEqual == null) this.eppSessionEqual = new EppSession[NUM_SESSIONS];
		if (this.eppChannelEqual == null) this.eppChannelEqual = new EppChannel[NUM_SESSIONS];

		// end session first if it's open

		if (this.eppChannelEqual[i] != null || this.eppSessionEqual[i] != null) endSessionEqual(i);

		// open session and channel

		EppGreeting eppGreeting;

		try {

			if (Boolean.parseBoolean(this.properties.getProperty("epp-usetls"))) {

				this.eppSessionEqual[i] = new EppSessionTcp();
				this.eppSessionEqual[i].init(this.properties);
			} else {

				this.eppSessionEqual[i] = new EppSessionTcp(false);
			}

			String eppHost = this.properties.getProperty("epp-host-equal");
			int eppPort = Integer.parseInt(this.properties.getProperty("epp-port-equal"));

			log.info("{= " + i + "} Trying to connect to " + eppHost + ":" + Integer.toString(eppPort) + " for = services.");

			eppGreeting = this.eppSessionEqual[i].connect(eppHost, eppPort);
			if (eppGreeting == null) throw new EppToolsException("{= " + i + "} No greeting on connect: " + this.eppSessionEqual[i].getException().getMessage(), this.eppSessionEqual[i].getException());

			this.eppChannelEqual[i] = this.eppSessionEqual[i].getChannel();
		} catch (Exception ex) {

			this.eppSessionEqual[i] = null;
			this.eppChannelEqual[i] = null;
			log.error(ex.getMessage(), ex);
			throw ex;
		}

		// log in

		try {

			EppCommandLogin eppCommandLogin = new EppCommandLogin(eppGreeting.getServiceMenu());
			eppCommandLogin.setClientTransactionId(this.generateTransactionId());
			eppCommandLogin.setCreds(new EppCreds(this.properties.getProperty("epp-username"), this.properties.getProperty("epp-password")));
			if (newPassword != null) eppCommandLogin.setNewPassword(newPassword);

			EppResponse eppResponse = this.eppChannelEqual[i].start(eppCommandLogin);
			EppResult eppResult = (EppResult) eppResponse.getResult().get(0);

			if (eppResult == null) throw new EppToolsException("No result");
			if (! (eppResponse.success())) throw makeEppToolsUnsuccessfulException(eppResult);
			if (eppResponse.getTransactionId() == null || eppResponse.getTransactionId().getClientTransactionId() == null || ! (eppResponse.getTransactionId().getClientTransactionId().equals(this.getLastTransactionId()))) throw new EppToolsException("Unexpected clTRID: " + eppResponse.getTransactionId().getClientTransactionId());
		} catch (Exception ex) {

			this.eppSessionEqual = null;
			this.eppChannelEqual = null;
			log.error(ex.getMessage(), ex);
			throw ex;
		}
	}

	/**
	 * Start the session and log in.
	 */
	private synchronized void beginSessionAt(String newPassword, int i) throws Exception {

		if (this.eppSessionAt == null) this.eppSessionAt = new EppSession[NUM_SESSIONS];
		if (this.eppChannelAt == null) this.eppChannelAt = new EppChannel[NUM_SESSIONS];

		// end session first if it's open

		if (this.eppChannelAt[i] != null || this.eppSessionAt[i] != null) endSessionAt(i);

		// open session and channel

		EppGreeting eppGreeting;

		try {

			if (Boolean.parseBoolean(this.properties.getProperty("epp-usetls"))) {

				this.eppSessionAt[i] = new EppSessionTcp();
				this.eppSessionAt[i].init(this.properties);
			} else {

				this.eppSessionAt[i] = new EppSessionTcp(false);
			}

			String eppHost = this.properties.getProperty("epp-host-at");
			int eppPort = Integer.parseInt(this.properties.getProperty("epp-port-at"));

			log.info("{@ " + i + "} Trying to connect to " + eppHost + ":" + Integer.toString(eppPort) + " for @ services.");

			eppGreeting = this.eppSessionAt[i].connect(eppHost, eppPort);
			if (eppGreeting == null) throw new EppToolsException("{@ " + i + "} No greeting on connect: " + this.eppSessionAt[i].getException().getMessage(), this.eppSessionAt[i].getException());

			this.eppChannelAt[i] = this.eppSessionAt[i].getChannel();
		} catch (Exception ex) {

			this.eppSessionAt[i] = null;
			this.eppChannelAt[i] = null;
			log.error(ex.getMessage(), ex);
			throw ex;
		}

		// log in

		try {

			EppCommandLogin eppCommandLogin = new EppCommandLogin(eppGreeting.getServiceMenu());
			eppCommandLogin.setClientTransactionId(this.generateTransactionId());
			eppCommandLogin.setCreds(new EppCreds(this.properties.getProperty("epp-username"), this.properties.getProperty("epp-password")));
			if (newPassword != null) eppCommandLogin.setNewPassword(newPassword);

			EppResponse eppResponse = this.eppChannelAt[i].start(eppCommandLogin);
			EppResult eppResult = (EppResult) eppResponse.getResult().get(0);

			if (eppResult == null) throw new EppToolsException("No result");
			if (! (eppResponse.success())) throw makeEppToolsUnsuccessfulException(eppResult);
			if (eppResponse.getTransactionId() == null || eppResponse.getTransactionId().getClientTransactionId() == null || ! (eppResponse.getTransactionId().getClientTransactionId().equals(this.getLastTransactionId()))) throw new EppToolsException("Unexpected clTRID: " + eppResponse.getTransactionId().getClientTransactionId());
		} catch (Exception ex) {

			this.eppSessionAt = null;
			this.eppChannelAt = null;
			log.error(ex.getMessage(), ex);
			throw ex;
		}
	}

	/**
	 * End the session.
	 */
	private synchronized void endSessionEqual(int i) {

		// shut down channel and session

		/*		log.debug("{=" + " " + i + "} Terminating channel to = server.");

		if (this.eppChannelEqual[i] != null) {

			this.eppChannelEqual[i].terminate();
		}*/

		log.debug("{=" + " " + i + "} Closing session to = server.");

		if (this.eppSessionEqual[i] != null) {

			try {

				if (((EppSessionTcp) this.eppSessionEqual[i]).getSocket() != null) ((EppSessionTcp) this.eppSessionEqual[i]).getSocket().close();
			} catch (IOException ex) { }
		}

		this.eppSessionEqual[i] = null;
		this.eppChannelEqual[i] = null;

		log.debug("{=" + " " + i + "} Session ended to = server.");
	}

	/**
	 * End the session.
	 */
	private synchronized void endSessionAt(int i) {

		// shut down channel and session

		/*		log.debug("{@" + " " + i + "} Terminating channel to @ server.");

		if (this.eppChannelAt[i] != null) {

			this.eppChannelAt[i].terminate();
		}*/

		log.debug("{@" + " " + i + "} Closing session to @ server.");

		if (this.eppSessionAt[i] != null) {

			try {

				if (((EppSessionTcp) this.eppSessionAt[i]).getSocket() != null) ((EppSessionTcp) this.eppSessionAt[i]).getSocket().close();
			} catch (IOException ex) { }
		}

		this.eppSessionAt[i] = null;
		this.eppChannelAt[i] = null;

		log.debug("{@" + " " + i + "} Session ended to @ server.");
	}

	/**
	 * Check if the session and channel are alive.
	 * If no, reconnect.
	 */
	public EppChannel checkChannelEqual(int i) throws EppToolsException {

		synchronized (this.eppChannelEqual[i]) {

			try {

				EppGreeting eppGreeting = this.eppChannelEqual[i].hello();
				if (eppGreeting == null) throw new NullPointerException();
			} catch (Exception ex) {

				log.warn("{= " + i + "} Channel to = server seems to have gone away: " + ex.getMessage() + " -> Trying to restore.");

				try {

					this.endSessionEqual(i);
					this.beginSessionEqual(null, i);
				} catch (Exception ex2) {

					log.error(ex2.getMessage(), ex2);
					throw new EppToolsException("{= " + i + "} Cannot restore channel: " + ex.getMessage(), ex);
				}

				log.info("{= " + i + "} Successfully restored channel after: " + ex.getMessage());
			}

			return(this.eppChannelEqual[i]);
		}
	}

	/**
	 * Check if the session and channel are alive.
	 * If no, reconnect.
	 */
	public EppChannel checkChannelAt(int i) throws EppToolsException {

		synchronized (this.eppChannelAt[i]) {

			try {

				EppGreeting eppGreeting = this.eppChannelAt[i].hello();
				if (eppGreeting == null) throw new NullPointerException();
			} catch (Exception ex) {

				log.warn("{@ " + i + "} Channel to @ server seems to have gone away: " + ex.getMessage() + " -> Trying to restore.");

				try {

					this.endSessionAt(i);
					this.beginSessionAt(null, i);
				} catch (Exception ex2) {

					log.error(ex2.getMessage(), ex2);
					throw new EppToolsException("{@ " + i + "} Cannot restore channel: " + ex.getMessage(), ex);
				}

				log.info("{@ " + i + "} Successfully restored channel after: " + ex.getMessage());
			}

			return(this.eppChannelAt[i]);
		}
	}

	/*
	 * Helper methods for sending
	 */

	/**
	 * Sends an EPP command and returns the EPP response.
	 * Everything is logged to our action store.
	 */
	private EppResponse send(char gcs, EppCommand eppCommand, int i) throws EppToolsException {

		if (gcs != '=' && gcs != '@') throw new IllegalArgumentException("GCS must be = or @.");

		// timestamp

		Date beginTimestamp = new Date();

		// make sure our session and channel are still alive

		EppChannel eppChannel = null;

		try {

			if (gcs == '=') {

				if (this.eppChannelEqual == null || this.eppChannelEqual.length <= i || this.eppChannelEqual[i] == null) {

					this.beginSessionEqual(null, i);
				}

				eppChannel = this.eppChannelEqual[i];
			}

			if (gcs == '@') {

				if (this.eppChannelAt == null || this.eppChannelAt.length <= i || this.eppChannelAt[i] == null) {

					this.beginSessionAt(null, i);
				}

				eppChannel = this.eppChannelAt[i];
			}

			if (eppChannel == null) throw new IOException("{" + gcs + " " + i + "} No channel.");
		} catch (EppToolsException ex) {

			throw ex;
		} catch (Exception ex) {

			throw new EppToolsException("Cannot initialize channel " + i + ": " + ex.getMessage(), ex);
		}

		// try to send the command and read the response

		EppResponse eppResponse = null;
		EppResult eppResult = null;

		synchronized (eppChannel) {

			log.debug("{" + gcs + " " + i + "} Blocking channel.");
			if (gcs == '=') this.eppBlockedEqual[i] = Boolean.TRUE;
			if (gcs == '@') this.eppBlockedAt[i] = Boolean.TRUE;

			try {

				log.debug("{" + gcs + " " + i + "} Attempting to send transaction " + eppCommand.getClientTransactionId() + " to " + gcs + " server.");

				eppResponse = eppChannel.send(eppCommand);
				if (eppChannel.getException() != null) throw eppChannel.getException();
				if (eppResponse == null) throw new IOException("{" + gcs + " " + i + "} No response.");

				eppResult = (EppResult) eppResponse.getResult().get(0);
				if (eppResult == null) throw new IOException("{" + gcs + " " + i + "} No result.");

				if (eppResult.getCode() == EppError.CODE_SESSION_LIMIT_EXCEEDED_SERVER_CLOSING_CONNECTION) throw new IOException("{" + gcs + " " + i + "} Session limit exceeded.");

				log.debug("{" + gcs + " " + i + "} Transaction " + eppCommand.getClientTransactionId() + " sent to " + gcs + " server.");
			} catch (Exception ex) {

				log.warn("{" + gcs + " " + i + "} Channel to " + gcs + " server seems to have gone away: " + ex.getMessage(), ex);

				// if there's just a problem with the socket, we try to restore and send the command again

				if (ex instanceof IOException) {

					log.debug("{" + gcs + " " + i + "} Trying to restore channel to " + gcs + " server.");

					// try to restore the channel

					try {

						if (gcs == '=') {

							this.endSessionEqual(i);
							this.beginSessionEqual(null, i);
							eppChannel = this.eppChannelEqual[i];
						}

						if (gcs == '@') {

							this.endSessionAt(i);
							this.beginSessionAt(null, i);
							eppChannel = this.eppChannelAt[i];
						}

						if (eppChannel == null) throw new IOException("{" + gcs + " " + i + "} Channel has gone away.");
					} catch (Exception ex2) {

						log.error("{" + gcs + " " + i + "} Cannot restore channel: " + ex2.getMessage(), ex2);
						ex = ex2;
						eppChannel = null;
					}

					// after restoring the channel, try to re-send the command

					if (eppChannel != null) {

						try {

							log.debug("{" + gcs + " " + i + "} Trying to re-send transaction " + eppCommand.getClientTransactionId() + " to " + gcs + " server."); 

							eppResponse = eppChannel.send(eppCommand);
							if (eppChannel.getException() != null) throw eppChannel.getException();
							if (eppResponse == null) throw new IOException("{" + gcs + " " + i + "} No response.");

							eppResult = (EppResult) eppResponse.getResult().get(0);
							if (eppResult == null) throw new IOException("{" + gcs + " " + i + "} No result.");

							if (eppResult.getCode() == EppError.CODE_SESSION_LIMIT_EXCEEDED_SERVER_CLOSING_CONNECTION) throw new IOException("{" + gcs + " " + i + "} Session limit exceeded.");

							log.debug("{" + gcs + " " + i + "} Transaction " + eppCommand.getClientTransactionId() + " sent to " + gcs + " server.");

							ex = null;
						} catch (Exception ex2) {

							log.warn("{" + gcs + " " + i + "} Still cannot send transaction " + eppCommand.getClientTransactionId() + " to " + gcs + " server: " + ex2.getMessage(), ex2);

							ex = ex2;
						}
					}
				}

				// if we couldn't handle the exception, we log and throw it

				if (ex != null)  {

					log.error("{" + gcs + " " + i + "} Failed to send transaction " + eppCommand.getClientTransactionId() + " to " + gcs + " server: " + ex.getMessage(), ex);

					// log the failed action

					try {

						this.store.createAction(new Character(gcs), eppCommand.getClientTransactionId(), eppCommand.toString(), ex.getMessage());
					} catch (StoreException ex2) {

						log.error("{" + gcs + " " + i + "} Cannot store failed EPP action: " + ex2.getMessage(), ex2);
					}

					log.debug("{" + gcs + " " + i + "} Unblocking channel.");
					if (gcs == '=') this.eppBlockedEqual[i] = Boolean.FALSE;
					if (gcs == '@') this.eppBlockedAt[i] = Boolean.FALSE;

					throw new EppToolsException("{" + gcs + " " + i + "} Cannot send transaction " + eppCommand.getClientTransactionId() + " to " + gcs + " server: " + ex.getMessage(), ex);
				}
			}

			log.debug("{" + gcs + " " + i + "} Unblocking channel.");
			if (gcs == '=') this.eppBlockedEqual[i] = Boolean.FALSE;
			if (gcs == '@') this.eppBlockedAt[i] = Boolean.FALSE;
		}

		// log the successful action

		try {

			this.store.createAction(Character.valueOf(gcs), eppCommand.getClientTransactionId(), eppCommand.toString(), eppResponse.toString());
		} catch (StoreException ex) {

			log.error("{" + gcs + " " + i + "} Cannot store successful EPP action:" + ex.getMessage(), ex);
		}

		// timestamp

		Date endTimestamp = new Date();

		// event

		EppEvent eppEvent = new EppEvent(this, Character.valueOf(gcs), beginTimestamp, endTimestamp, eppChannel, eppCommand, eppResponse);

		this.fireEppEvent(eppEvent);

		// check the EPP response

		if (! (eppResponse.success())) throw makeEppToolsUnsuccessfulException(eppResult);
		if (eppResponse.getTransactionId() == null || eppResponse.getTransactionId().getClientTransactionId() == null || ! (eppResponse.getTransactionId().getClientTransactionId().equals(eppCommand.getClientTransactionId()))) throw new EppToolsException("Unexpected clTRID: " + eppResponse.getTransactionId().getClientTransactionId() + " (expected " + eppCommand.getClientTransactionId() + ")");

		log.info("{" + gcs + " " + i + "} Successfully completed transaction " + eppCommand.getClientTransactionId() + " with " + gcs + " server.");

		return(eppResponse);
	}

	/**
	 * Sends an EPP command and returns the EPP response.
	 * Everything is logged to our action store.
	 */
	private EppResponse send(char gcs, EppCommand eppCommand) throws EppToolsException {

		int i = 0;

		if (gcs == '=') while (this.eppBlockedEqual[i] != null && this.eppBlockedEqual[i].equals(Boolean.TRUE) && i < NUM_SESSIONS) i++;
		if (gcs == '@') while (this.eppBlockedAt[i] != null && this.eppBlockedAt[i].equals(Boolean.TRUE) && i < NUM_SESSIONS) i++;

		if (i == NUM_SESSIONS) throw new EppToolsException("All channels to " + gcs + " registry are blocked. Please try again later.");

		log.debug("Sending to " + gcs + " channel " + i);

		return(this.send(gcs, eppCommand, i));
	}

	@SuppressWarnings("unchecked")
	private static EppToolsUnsuccessfulException makeEppToolsUnsuccessfulException(EppResult eppResult) {

		if (eppResult == null) return(new EppToolsUnsuccessfulException("Unsuccessful.", eppResult));

		StringBuffer buffer = new StringBuffer();
		buffer.append("Unsuccessful: ");
		if (eppResult.getMessage() != null) buffer.append(eppResult.getMessage().getMessage());
		for (String value : (List<String>) eppResult.getValue()) buffer.append(" / " + value);

		return(new EppToolsUnsuccessfulException(buffer.toString(), eppResult));
	}

	/*
	 * Helper methods for transaction ids
	 */

	private int currentTransactionNumber = 0;
	private String lastTransactionId;

	/**
	 * Generate and return a new client transaction ID, consisting of:
	 * - our EPP client ID
	 * - a sequential number
	 * - our thread ID
	 * - a timestamp
	 */
	private String generateTransactionId() {

		this.currentTransactionNumber++;

		StringBuffer buffer = new StringBuffer();
		buffer.append("id-");
		buffer.append(this.properties.getProperty("epp-username") + "-");
		buffer.append(Long.toString(Thread.currentThread().getId()) + "-");
		buffer.append(Integer.toString(this.currentTransactionNumber) + "-");
		buffer.append(Long.toString(System.currentTimeMillis()));

		this.lastTransactionId = buffer.toString();

		return(this.lastTransactionId);
	}

	/**
	 * Returns the last client transaction ID the we generated.
	 */
	public String getLastTransactionId() {

		return(this.lastTransactionId);
	}

	/*
	 * Events
	 */

	public void addEppListener(EppListener eppListener) {

		if (this.eppListeners.contains(eppListener)) return;
		this.eppListeners.add(eppListener);
	}

	public void removeEppListener(EppListener eppListener) {

		this.eppListeners.remove(eppListener);
	}

	public void fireEppEvent(EppEvent eppEvent) {

		for (EppListener eppListener : this.eppListeners) eppListener.onSend(eppEvent);
	}

	/*
	 * Getters and Setters
	 */

	public Store getStore() {

		return store;
	}

	public void setStore(Store store) {

		this.store = store;
	}
}
