package ibrokerkit.epptools4java;

import ibrokerkit.epptools4java.events.EppEvents;
import ibrokerkit.epptools4java.store.Store;
import ibrokerkit.epptools4java.store.StoreException;
import ibrokerkit.epptools4java.store.impl.db.DatabaseStore;

import java.io.Serializable;
import java.security.Security;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Random;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neulevel.epp.core.EppAddress;
import com.neulevel.epp.core.EppAuthInfo;
import com.neulevel.epp.core.EppContactData;
import com.neulevel.epp.core.EppError;
import com.neulevel.epp.core.EppGenericNVPairs;
import com.neulevel.epp.core.EppObject;
import com.neulevel.epp.core.EppPeriod;
import com.neulevel.epp.core.EppUnspec;
import com.neulevel.epp.core.command.EppCommand;
import com.neulevel.epp.core.command.EppCommandCheck;
import com.neulevel.epp.core.command.EppCommandCreate;
import com.neulevel.epp.core.command.EppCommandDelete;
import com.neulevel.epp.core.command.EppCommandPoll;
import com.neulevel.epp.core.command.EppCommandTransfer;
import com.neulevel.epp.core.response.EppResponse;
import com.neulevel.epp.core.response.EppResponseData;
import com.neulevel.epp.core.response.EppResponseDataInfo;
import com.neulevel.epp.xri.EppXriAuthority;
import com.neulevel.epp.xri.EppXriName;
import com.neulevel.epp.xri.EppXriNumber;
import com.neulevel.epp.xri.EppXriRef;
import com.neulevel.epp.xri.EppXriServiceEndpoint;
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

	public static SimpleDateFormat xmlDateFormat;
	public static SimpleDateFormat xmlShortDateFormat;

	private static Random random;

	private Properties properties;

	private Store store;
	private EppTransactionIdGenerator eppTransactionIdGenerator;
	private EppEvents eppEvents;

	private final Map<Character, EppConnection> eppConnections;

	static {

		xmlDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
		xmlDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		xmlShortDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		xmlShortDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		random = new Random();
	}

	public EppTools(Properties properties) {

		this.properties = properties;

		this.store = new DatabaseStore(this.properties);
		this.eppTransactionIdGenerator = new EppTransactionIdGenerator(this.properties);
		this.eppEvents = new EppEvents();

		this.eppConnections = new HashMap<Character, EppConnection> ();
	}

	/**
	 * Init everything and start a session
	 */
	@SuppressWarnings("restriction")
	public synchronized void init() throws Exception {

		log.debug("init()");

		Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());

		// create EPP connections

		if (this.properties.containsKey("epp-host-equal")) {

			String eppHostEqual = this.properties.getProperty("epp-host-equal");
			Integer eppPortEqual = Integer.valueOf(this.properties.getProperty("epp-port-equal"));
			EppConnection eppConnectionEqual = new EppConnection('=', eppHostEqual, eppPortEqual, this.properties, this.store, this.eppTransactionIdGenerator, this.eppEvents);

			this.eppConnections.put(Character.valueOf('='), eppConnectionEqual);
		}

		if (this.properties.containsKey("epp-host-at")) {

			String eppHostAt = this.properties.getProperty("epp-host-at");
			Integer eppPortAt = Integer.valueOf(this.properties.getProperty("epp-port-at"));
			EppConnection eppConnectionAt = new EppConnection('@', eppHostAt, eppPortAt, this.properties, this.store, this.eppTransactionIdGenerator, this.eppEvents);

			this.eppConnections.put(Character.valueOf('@'), eppConnectionAt);
		}

		if (this.properties.containsKey("epp-host-plus")) {

			String eppHostPlus = this.properties.getProperty("epp-host-plus");
			Integer eppPortPlus = Integer.valueOf(this.properties.getProperty("epp-port-plus"));
			EppConnection eppConnectionPlus = new EppConnection('+', eppHostPlus, eppPortPlus, this.properties, this.store, this.eppTransactionIdGenerator, this.eppEvents);

			this.eppConnections.put(Character.valueOf('+'), eppConnectionPlus);
		}

		log.debug("Loaded EPP connections: " + this.eppConnections.values());

		// init EPP connections

		for (EppConnection eppConnection : this.eppConnections.values()) {

			eppConnection.init();
		}

		// init store

		this.store.init();

		// done

		log.debug("Done.");
	}

	/**
	 * Shut down
	 */
	public void close() {

		// close EPP connections

		for (EppConnection eppConnection : this.eppConnections.values()) {

			eppConnection.close();
		}

		// close store

		this.store.close();
	}

	/**
	 * Change the password
	 * @param newPassword
	 */
	public synchronized void changePassword(String newPassword) throws EppToolsException {

		for (EppConnection eppConnection : this.eppConnections.values()) {

			eppConnection.changePassword(newPassword);
		}
	}

	public boolean isOwnClientId(String clientId) {

		return clientId.equals(this.properties.getProperty("epp-clientid"));
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

	/*
	 * Methods for polling
	 */

	public EppResponseData poll(char gcs, boolean ack) throws EppToolsException {

		log.debug("poll(gcs=" + gcs + ", ack=" + ack + ")");

		EppCommandPoll eppCommandPoll = new EppCommandPoll(this.eppTransactionIdGenerator.generateTransactionId());
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

			eppCommandPoll = new EppCommandPoll(this.eppTransactionIdGenerator.generateTransactionId());
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

		EppCommandCreate eppCommandCreate = EppCommand.create(eppXriAuthority, this.eppTransactionIdGenerator.generateTransactionId());

		if (extension != null) {

			StringBuffer buffer = new StringBuffer();
			boolean first = true;

			for (Entry<String, String> extensionEntry : extension.entrySet()) {

				if (extensionEntry.getKey().contains("=") || extensionEntry.getKey().contains("+")) continue;
				if (extensionEntry.getValue().contains("=") || extensionEntry.getValue().contains("+")) continue;

				if (! first) buffer.append(" ");
				buffer.append(extensionEntry.getKey());
				buffer.append("=");
				buffer.append(extensionEntry.getValue());
				first = false;
			}

			EppUnspec unspec = new EppUnspec(buffer.toString());
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

		EppCommandDelete eppCommandDelete = EppCommand.delete(EppObject.XRI_AUTHORITY, authId, this.eppTransactionIdGenerator.generateTransactionId());

		this.send(gcs, eppCommandDelete);
	}

	public boolean checkAuthority(char gcs, String authId) throws EppToolsException {

		EppCommandCheck eppCommandCheck = EppCommand.check(EppObject.XRI_AUTHORITY, this.eppTransactionIdGenerator.generateTransactionId());
		eppCommandCheck.add(authId);

		EppResponse eppResponse = this.send(gcs, eppCommandCheck);

		EppResponseDataCheckXriAuthority eppResponseData = (EppResponseDataCheckXriAuthority) eppResponse.getResponseData();
		if (eppResponseData == null) throw new EppToolsException("No response data");

		return(! eppResponseData.isAvailable(authId));
	}

	public EppXriAuthority infoAuthority(char gcs, String authId, boolean all) throws EppToolsException {

		EppCommandInfoXriAuthority eppCommandInfo = (EppCommandInfoXriAuthority) EppCommand.info(EppObject.XRI_AUTHORITY, authId, this.eppTransactionIdGenerator.generateTransactionId());
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

		EppCommandTransferXriAuthority eppCommandTransfer = (EppCommandTransferXriAuthority) EppCommand.transfer(EppObject.XRI_AUTHORITY, authId, this.eppTransactionIdGenerator.generateTransactionId());
		eppCommandTransfer.setOperation(EppCommandTransfer.OPTYPE_REQUEST);

		EppResponse eppResponse = this.send(gcs, eppCommandTransfer);

		EppResponseDataTransferXriAuthority eppResponseData = (EppResponseDataTransferXriAuthority) eppResponse.getResponseData();
		if (eppResponseData == null) throw new EppToolsException("No response data");
		if (eppResponseData.getTransferToken() == null) throw new EppToolsException("No transfer token");

		return(eppResponseData.getTransferToken());
	}

	public void transferApproveAuthority(char gcs, String authId, String password, String transferToken) throws EppToolsException {

		EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, password);

		EppCommandTransferXriAuthority eppCommandTransfer = (EppCommandTransferXriAuthority) EppCommand.transfer(EppObject.XRI_AUTHORITY, authId, this.eppTransactionIdGenerator.generateTransactionId());
		eppCommandTransfer.setOperation(EppCommandTransfer.OPTYPE_APPROVE);
		eppCommandTransfer.setAuthInfo(eppAuthInfo);
		eppCommandTransfer.setTransferToken(transferToken);

		this.send(gcs, eppCommandTransfer);
	}

	public void transferRejectAuthority(char gcs, String authId, String password, String transferToken) throws EppToolsException {

		EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, password);

		EppCommandTransferXriAuthority eppCommandTransfer = (EppCommandTransferXriAuthority) EppCommand.transfer(EppObject.XRI_AUTHORITY, authId, this.eppTransactionIdGenerator.generateTransactionId());
		eppCommandTransfer.setOperation(EppCommandTransfer.OPTYPE_REJECT);
		eppCommandTransfer.setAuthInfo(eppAuthInfo);
		eppCommandTransfer.setTransferToken(transferToken);

		this.send(gcs, eppCommandTransfer);
	}

	public EppResponseDataTransferXriAuthority transferQueryAuthority(char gcs, String authId, String password) throws EppToolsException {

		EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, password);

		EppCommandTransfer eppCommandTransfer = EppCommand.transfer(EppObject.XRI_AUTHORITY, authId, this.eppTransactionIdGenerator.generateTransactionId());
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

	/*	public void initAuthority(char gcs, String authId, String password, EppCommandUpdateXriAuthority eppCommandUpdateXriAuthority) throws EppToolsException {

		EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, password);

		EppCommandUpdateXriAuthority eppCommandUpdate = (EppCommandUpdateXriAuthority) EppCommand.update(EppObject.XRI_AUTHORITY, authId, this.eppTransactionIdGenerator.generateTransactionId());
		eppCommandUpdate.setCanonicalEquivID(xrd.getCanonicalEquivID().getValue());
		for (int i=0; i<xrd.getNumEquivIDs(); i++) eppCommandUpdate.addEquivID(makeEppXriSynonym(xrd.getEquivIDAt(i)));
		for (int i=0; i<xrd.getNumRefs(); i++) eppCommandUpdate.addRef(makeEppXriRef(xrd.getRefAt(i)));
		for (int i=0; i<xrd.getNumRedirects(); i++) eppCommandUpdate.addRedirect(makeEppXriURI(xrd.getRedirectAt(i)));
		for (int i=0; i<xrd.getNumServices(); i++) eppCommandUpdate.addServiceEndpoint(makeEppXriServiceEndpoint(xrd.getServiceAt(i)));
		eppCommandUpdate.setAuthInfo(eppAuthInfo);

		this.send(gcs, eppCommandUpdate);
	}*/

	public void setSocialData(char gcs, String authId, String password, EppXriSocialData eppXriSocialData) throws EppToolsException {

		EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, password);

		EppCommandUpdateXriAuthority eppCommandUpdate = (EppCommandUpdateXriAuthority) EppCommand.update(EppObject.XRI_AUTHORITY, authId, this.eppTransactionIdGenerator.generateTransactionId());
		eppCommandUpdate.setNewSocialData(eppXriSocialData);
		eppCommandUpdate.setAuthInfo(eppAuthInfo);

		this.send(gcs, eppCommandUpdate);
	}

	public void setSocialData(char gcs, String authId, String password, String[] street, String city, String state, String postalCode, String countryCode, String name, String organization, String primaryVoice, String secondaryVoice, String fax, String primaryEmail, String secondaryEmail, String pager) throws EppToolsException {

		EppXriSocialData eppXriSocialData = makeEppXriSocialData(street, city, state, postalCode, countryCode, name, organization, primaryVoice, secondaryVoice, fax, primaryEmail, secondaryEmail, pager);

		this.setSocialData(gcs, authId, password, eppXriSocialData);
	}

	public void setCanonicalEquivID(char gcs, String authId, String password, String canonicalEquivIDString) throws EppToolsException {

		EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, password);

		EppCommandUpdateXriAuthority eppCommandUpdate = (EppCommandUpdateXriAuthority) EppCommand.update(EppObject.XRI_AUTHORITY, authId, this.eppTransactionIdGenerator.generateTransactionId());
		eppCommandUpdate.setCanonicalEquivID(canonicalEquivIDString);
		eppCommandUpdate.setAuthInfo(eppAuthInfo);

		this.send(gcs, eppCommandUpdate);
	}

	public void setExtension(char gcs, String authId, String password, String extension) throws EppToolsException {

		EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, password);

		EppCommandUpdateXriAuthority eppCommandUpdate = (EppCommandUpdateXriAuthority) EppCommand.update(EppObject.XRI_AUTHORITY, authId, this.eppTransactionIdGenerator.generateTransactionId());
		eppCommandUpdate.setNewExtension(extension);
		eppCommandUpdate.setAuthInfo(eppAuthInfo);

		this.send(gcs, eppCommandUpdate);
	}

	public void addEquivIDs(char gcs, String authId, String password, EppXriSynonym[] eppXriSynonyms) throws EppToolsException {

		EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, password);

		EppCommandUpdateXriAuthority eppCommandUpdate = (EppCommandUpdateXriAuthority) EppCommand.update(EppObject.XRI_AUTHORITY, authId, this.eppTransactionIdGenerator.generateTransactionId());
		for (EppXriSynonym equivID : eppXriSynonyms) eppCommandUpdate.addEquivID(equivID);
		eppCommandUpdate.setAuthInfo(eppAuthInfo);

		this.send(gcs, eppCommandUpdate);
	}

	public void addRefs(char gcs, String authId, String password, EppXriRef[] eppXriRefs) throws EppToolsException {

		EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, password);

		EppCommandUpdateXriAuthority eppCommandUpdate = (EppCommandUpdateXriAuthority) EppCommand.update(EppObject.XRI_AUTHORITY, authId, this.eppTransactionIdGenerator.generateTransactionId());
		for (EppXriRef eppXriRef : eppXriRefs) eppCommandUpdate.addRef(eppXriRef);
		eppCommandUpdate.setAuthInfo(eppAuthInfo);

		this.send(gcs, eppCommandUpdate);
	}

	public void addRedirects(char gcs, String authId, String password, EppXriURI[] eppXriURIs) throws EppToolsException {

		EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, password);

		EppCommandUpdateXriAuthority eppCommandUpdate = (EppCommandUpdateXriAuthority) EppCommand.update(EppObject.XRI_AUTHORITY, authId, this.eppTransactionIdGenerator.generateTransactionId());
		for (EppXriURI eppXriURI : eppXriURIs) eppCommandUpdate.addRedirect(eppXriURI);
		eppCommandUpdate.setAuthInfo(eppAuthInfo);

		this.send(gcs, eppCommandUpdate);
	}

	public void addServices(char gcs, String authId, String password, EppXriServiceEndpoint[] eppXriServiceEndpoints) throws EppToolsException {

		for (EppXriServiceEndpoint eppXriServiceEndpoint : eppXriServiceEndpoints) if (eppXriServiceEndpoint.getId() == null) eppXriServiceEndpoint.setId(EppTools.makeGrsServiceId());

		EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, password);

		EppCommandUpdateXriAuthority eppCommandUpdate = (EppCommandUpdateXriAuthority) EppCommand.update(EppObject.XRI_AUTHORITY, authId, this.eppTransactionIdGenerator.generateTransactionId());
		for (EppXriServiceEndpoint eppXriServiceEndpoint : eppXriServiceEndpoints) eppCommandUpdate.addServiceEndpoint(eppXriServiceEndpoint);
		eppCommandUpdate.setAuthInfo(eppAuthInfo);

		this.send(gcs, eppCommandUpdate);
	}

	public void addDiscoveryKeys(char gcs, String authId, String password, String[] keys) throws EppToolsException {

		EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, password);

		EppCommandUpdateXriAuthority eppCommandUpdate = (EppCommandUpdateXriAuthority) EppCommand.update(EppObject.XRI_AUTHORITY, authId, this.eppTransactionIdGenerator.generateTransactionId());
		for (String key : keys) eppCommandUpdate.addDiscoverykey(key);
		eppCommandUpdate.setAuthInfo(eppAuthInfo);

		this.send(gcs, eppCommandUpdate);
	}

	public void deleteCanonicalEquivID(char gcs, String authId, String password, String canonicalEquivIDString) throws EppToolsException {

		EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, password);

		EppCommandUpdateXriAuthority eppCommandUpdate = (EppCommandUpdateXriAuthority) EppCommand.update(EppObject.XRI_AUTHORITY, authId, this.eppTransactionIdGenerator.generateTransactionId());
		eppCommandUpdate.removeCanonicalEquivID(canonicalEquivIDString);
		eppCommandUpdate.setAuthInfo(eppAuthInfo);

		this.send(gcs, eppCommandUpdate);
	}

	public void deleteEquivIDs(char gcs, String authId, String password, String[] equivIDStrings) throws EppToolsException {

		EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, password);

		EppCommandUpdateXriAuthority eppCommandUpdate = (EppCommandUpdateXriAuthority) EppCommand.update(EppObject.XRI_AUTHORITY, authId, this.eppTransactionIdGenerator.generateTransactionId());
		for (String equivIDString : equivIDStrings) eppCommandUpdate.removeEquivID(equivIDString);
		eppCommandUpdate.setAuthInfo(eppAuthInfo);

		this.send(gcs, eppCommandUpdate);
	}

	public void deleteRefs(char gcs, String authId, String password, String[] refStrings) throws EppToolsException {

		EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, password);

		EppCommandUpdateXriAuthority eppCommandUpdate = (EppCommandUpdateXriAuthority) EppCommand.update(EppObject.XRI_AUTHORITY, authId, this.eppTransactionIdGenerator.generateTransactionId());
		for (String refString : refStrings) eppCommandUpdate.removeRef(refString);
		eppCommandUpdate.setAuthInfo(eppAuthInfo);

		this.send(gcs, eppCommandUpdate);
	}

	public void deleteRedirects(char gcs, String authId, String password, String[] redirectStrings) throws EppToolsException {

		EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, password);

		EppCommandUpdateXriAuthority eppCommandUpdate = (EppCommandUpdateXriAuthority) EppCommand.update(EppObject.XRI_AUTHORITY, authId, this.eppTransactionIdGenerator.generateTransactionId());
		for (String redirectString : redirectStrings) eppCommandUpdate.removeRedirect(redirectString);
		eppCommandUpdate.setAuthInfo(eppAuthInfo);

		this.send(gcs, eppCommandUpdate);
	}

	public void deleteServiceIds(char gcs, String authId, String password, String[] serviceIds) throws EppToolsException {

		EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, password);

		EppCommandUpdateXriAuthority eppCommandUpdate = (EppCommandUpdateXriAuthority) EppCommand.update(EppObject.XRI_AUTHORITY, authId, this.eppTransactionIdGenerator.generateTransactionId());
		for (String service : serviceIds) eppCommandUpdate.removeServiceEndpoint(service);
		eppCommandUpdate.setAuthInfo(eppAuthInfo);

		this.send(gcs, eppCommandUpdate);
	}

	public void deleteDiscoveryKeys(char gcs, String authId, String password, String[] keys) throws EppToolsException {

		EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, password);

		EppCommandUpdateXriAuthority eppCommandUpdate = (EppCommandUpdateXriAuthority) EppCommand.update(EppObject.XRI_AUTHORITY, authId, this.eppTransactionIdGenerator.generateTransactionId());
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

		EppCommandCreate eppCommandCreate = EppCommand.create(eppXriNumber, this.eppTransactionIdGenerator.generateTransactionId());

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

		EppCommandDelete eppCommandDelete = EppCommand.delete(EppObject.XRI_INUMBER, inumber, this.eppTransactionIdGenerator.generateTransactionId());

		this.send(gcs, eppCommandDelete);
	}

	public boolean checkInumber(char gcs, String inumber) throws EppToolsException {

		EppCommandCheck eppCommandCheck = EppCommand.check(EppObject.XRI_INUMBER, this.eppTransactionIdGenerator.generateTransactionId());
		eppCommandCheck.add(inumber);

		EppResponse eppResponse = this.send(gcs, eppCommandCheck);

		EppResponseDataCheckXriNumber eppResponseData = (EppResponseDataCheckXriNumber) eppResponse.getResponseData();
		if (eppResponseData == null) throw new EppToolsException("No response data");

		return(! eppResponseData.isAvailable(inumber));
	}

	public EppXriNumber infoInumber(char gcs, String inumber) throws EppToolsException {

		EppCommandInfoXriNumber eppCommandInfo = (EppCommandInfoXriNumber) EppCommand.info(EppObject.XRI_INUMBER, inumber, this.eppTransactionIdGenerator.generateTransactionId());

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

		EppCommandRenewXriNumber eppCommandRenew = (EppCommandRenewXriNumber) EppCommand.renew(EppObject.XRI_INUMBER, inumber, this.eppTransactionIdGenerator.generateTransactionId());
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

		EppCommandCreate eppCommandCreate = EppCommand.create(eppXriName, this.eppTransactionIdGenerator.generateTransactionId());

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

	public EppResponseDataCreateXriName createIname(char gcs, String iname, String authId, int years) throws EppToolsException {

		return this.createIname(gcs, iname, authId, years, null);
	}

	public void deleteIname(char gcs, String iname) throws EppToolsException {

		EppCommandDelete eppCommandDelete = EppCommand.delete(EppObject.XRI_INAME, iname, this.eppTransactionIdGenerator.generateTransactionId());

		this.send(gcs, eppCommandDelete);
	}

	public boolean checkIname(char gcs, String iname) throws EppToolsException {

		EppCommandCheck eppCommandCheck = EppCommand.check(EppObject.XRI_INAME, this.eppTransactionIdGenerator.generateTransactionId());
		eppCommandCheck.add(iname);

		EppResponse eppResponse = this.send(gcs, eppCommandCheck);

		EppResponseDataCheckXriName eppResponseData = (EppResponseDataCheckXriName) eppResponse.getResponseData();
		if (eppResponseData == null) throw new EppToolsException("No response data");

		return(! eppResponseData.isAvailable(iname));
	}

	public EppXriName infoIname(char gcs, String iname) throws EppToolsException {

		EppCommandInfoXriName eppCommandInfo = (EppCommandInfoXriName) EppCommand.info(EppObject.XRI_INAME, iname, this.eppTransactionIdGenerator.generateTransactionId());

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

		EppCommandRenewXriName eppCommandRenew = (EppCommandRenewXriName) EppCommand.renew(EppObject.XRI_INAME, iname, this.eppTransactionIdGenerator.generateTransactionId());
		eppCommandRenew.setPeriod(eppPeriod);
		eppCommandRenew.setCurrentExpireDate(curExpDate);

		EppResponse eppResponse = this.send(gcs, eppCommandRenew);

		EppResponseDataRenewXriName eppResponseData = (EppResponseDataRenewXriName) eppResponse.getResponseData();
		if (eppResponseData == null) throw new EppToolsException("No response data");

		return(eppResponseData.getDateExpired());
	}

	public String transferRequestIname(char gcs, String iname, String authId, String password) throws EppToolsException {

		EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, password);

		EppCommandTransferXriName eppCommandTransfer = (EppCommandTransferXriName) EppCommand.transfer(EppObject.XRI_INAME, iname, this.eppTransactionIdGenerator.generateTransactionId());
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

		EppCommandTransferXriName eppCommandTransfer = (EppCommandTransferXriName) EppCommand.transfer(EppObject.XRI_INAME, iname, this.eppTransactionIdGenerator.generateTransactionId());
		eppCommandTransfer.setOperation(EppCommandTransfer.OPTYPE_APPROVE);
		eppCommandTransfer.setAuthInfo(eppAuthInfo);
		eppCommandTransfer.setTransferToken(transferToken);

		this.send(gcs, eppCommandTransfer);
	}

	public void transferRejectIname(char gcs, String iname, String password, String transferToken) throws EppToolsException {

		EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, password);

		EppCommandTransferXriName eppCommandTransfer = (EppCommandTransferXriName) EppCommand.transfer(EppObject.XRI_INAME, iname, this.eppTransactionIdGenerator.generateTransactionId());
		eppCommandTransfer.setOperation(EppCommandTransfer.OPTYPE_REJECT);
		eppCommandTransfer.setAuthInfo(eppAuthInfo);
		eppCommandTransfer.setTransferToken(transferToken);

		this.send(gcs, eppCommandTransfer);
	}

	public EppResponseDataTransferXriName transferQueryIname(char gcs, String iname, String password) throws EppToolsException {

		EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, password);

		EppCommandTransfer eppCommandTransfer = EppCommand.transfer(EppObject.XRI_INAME, iname, this.eppTransactionIdGenerator.generateTransactionId());
		eppCommandTransfer.setOperation(EppCommandTransfer.OPTYPE_QUERY);
		eppCommandTransfer.setAuthInfo(eppAuthInfo);

		EppResponse eppResponse = this.send(gcs, eppCommandTransfer);

		EppResponseDataTransferXriName eppResponseData = (EppResponseDataTransferXriName) eppResponse.getResponseData();
		if (eppResponseData == null) throw new EppToolsException("No response data");

		return(eppResponseData);
	}

	/*
	 * Sending
	 */

	public EppResponse send(char gcs, EppCommand eppCommand) throws EppToolsException {

		EppConnection eppConnection = this.eppConnections.get(Character.valueOf(gcs));
		if (eppConnection == null) throw new EppToolsException("No EPP connection for " + gcs);

		return eppConnection.send(eppCommand);
	}

	/*
	 * Getters and Setters
	 */

	public Store getStore() {

		return this.store;
	}

	public void setStore(Store store) {

		this.store = store;
	}

	public EppTransactionIdGenerator getEppTransactionIdGenerator() {

		return eppTransactionIdGenerator;
	}

	public void setEppTransactionIdGenerator(EppTransactionIdGenerator eppTransactionIdGenerator) {

		this.eppTransactionIdGenerator = eppTransactionIdGenerator;
	}

	public EppEvents getEppEvents() {

		return this.eppEvents;
	}

	public void setEppEvents(EppEvents eppEvents) {

		this.eppEvents = eppEvents;
	}
}
