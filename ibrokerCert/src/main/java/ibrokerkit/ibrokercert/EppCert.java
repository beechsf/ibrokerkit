package ibrokerkit.ibrokercert;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import java.util.TimeZone;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.security.cert.X509Certificate;

import com.neulevel.epp.core.EppAddress;
import com.neulevel.epp.core.EppAuthInfo;
import com.neulevel.epp.core.EppContactData;
import com.neulevel.epp.core.EppGreeting;
import com.neulevel.epp.core.EppObject;
import com.neulevel.epp.core.EppPeriod;
import com.neulevel.epp.core.EppStatus;
import com.neulevel.epp.core.command.EppCommand;
import com.neulevel.epp.core.command.EppCommandCheck;
import com.neulevel.epp.core.command.EppCommandCreate;
import com.neulevel.epp.core.command.EppCommandDelete;
import com.neulevel.epp.core.command.EppCommandInfo;
import com.neulevel.epp.core.command.EppCommandLogin;
import com.neulevel.epp.core.command.EppCommandTransfer;
import com.neulevel.epp.core.command.EppCreds;
import com.neulevel.epp.core.response.EppResponse;
import com.neulevel.epp.core.response.EppResponseDataInfo;
import com.neulevel.epp.core.response.EppResult;
import com.neulevel.epp.transport.EppChannel;
import com.neulevel.epp.transport.EppSession;
import com.neulevel.epp.transport.tcp.EppChannelTcp;
import com.neulevel.epp.transport.tcp.EppSessionTcp;
import com.neulevel.epp.xri.EppXriAuthority;
import com.neulevel.epp.xri.EppXriName;
import com.neulevel.epp.xri.EppXriNumber;
import com.neulevel.epp.xri.EppXriServiceEndpoint;
import com.neulevel.epp.xri.EppXriSocialData;
import com.neulevel.epp.xri.EppXriTrustee;
import com.neulevel.epp.xri.command.EppCommandRenewXriName;
import com.neulevel.epp.xri.command.EppCommandRenewXriNumber;
import com.neulevel.epp.xri.command.EppCommandTransferXriAuthority;
import com.neulevel.epp.xri.command.EppCommandTransferXriName;
import com.neulevel.epp.xri.command.EppCommandUpdateXriAuthority;
import com.neulevel.epp.xri.command.EppCommandUpdateXriName;
import com.neulevel.epp.xri.command.EppCommandUpdateXriNumber;
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

public class EppCert {

	/*
	 * Flags for printing debug messages to stdout.
	 */
	static final boolean doDumpGreeting = true;
	static final boolean doDumpCommand = true;
	static final boolean doDumpResponse = true;

	/*
	 * This checks the responses from the certification server.
	 * It is recommended to leave this turned off, since this is not required.
	 */
	static final boolean doChecks = false;

	static EppSession eppSession;
	static EppChannel eppChannel;
	static EppGreeting eppGreeting;

	static SimpleDateFormat xmlDateFormat;
	static SimpleDateFormat xmlShortDateFormat;

	static {

		xmlDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
		xmlDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		xmlShortDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		xmlShortDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	static void log(String text) {

		System.out.println(text);
	}

	static void alert(String text) {

		System.err.println(text);

		Exception ex = eppSession.getException();
		if (ex != null) ex.printStackTrace(System.err);

//		if (eppChannel != null) eppChannel.terminate();
//		if (eppSession != null) eppSession.close();

//		System.exit(1);
	}

	static void dumpChannel(EppChannel channel) {

		Socket socket = ((EppChannelTcp) channel).getSocket();

		System.out.println("Connected from: " + socket.getInetAddress() + " remote port: " + socket.getPort());

		if (! (socket instanceof SSLSocket)) {

			System.out.println("Session started without TLS");
			System.out.flush();
			return;
		}

		SSLSocket ssls = (SSLSocket) socket;
		SSLSession session = ssls.getSession();
		System.out.println("Session    = " + session);
		System.out.println("Session Id = " + session.getId());
		System.out.println("Peer Host  = " + session.getPeerHost());

		try {

			X509Certificate certs[] = session.getPeerCertificateChain();

			for (int n = 0; n < certs.length; n++) {

				System.out.println("PeerCertificates[" + n + "] = " + certs[n]);
			}
		}
		catch (SSLPeerUnverifiedException ex) {

			ex.printStackTrace();
		}

		System.out.flush();
	}

	static void dumpGreeting(EppGreeting eppGreeting) throws IOException {

		if (! doDumpGreeting) return;

		File file = new File("eppGreeting.txt");
		FileWriter fileWriter = new FileWriter(file);

		fileWriter.write(eppGreeting.toString());

		fileWriter.close();
	}

	static void dumpCommand(int i, EppCommand eppCommand) throws IOException {

		if (! doDumpCommand) return;

		File file = new File("eppCommand" + Integer.toString(i) + ".txt");
		FileWriter fileWriter = new FileWriter(file);

		fileWriter.write(eppCommand.toString());

		fileWriter.close();
	}

	static void dumpResponse(int i, EppResponse eppResponse) throws IOException {

		if (! doDumpResponse) return;

		File file = new File("eppResponse" + Integer.toString(i) + ".txt");
		FileWriter fileWriter = new FileWriter(file);

		fileWriter.write(eppResponse.toString());

		fileWriter.close();
	}

	@SuppressWarnings("deprecation")
	public static void main(String[] args) {

		// read properties

		Properties properties = new Properties();

		try {

			properties.load(new FileReader(new File("conf/application.properties")));
		} catch (IOException ex) {

			ex.printStackTrace(System.err);
			System.err.println("Cannot read application.properties: " + ex.getMessage());
			System.err.println("Make sure the file is in your current working directory.");
			System.exit(1);
		}

		String host = properties.getProperty("host");
		int port = Integer.parseInt(properties.getProperty("port"));
		String clientid = properties.getProperty("clientid");
		String password = properties.getProperty("password");

		// establish the session

		try {

			eppSession = new EppSessionTcp();
			eppSession.init("testkeys.client.prop");
		} catch (Exception ex) {

			ex.printStackTrace(System.err);
			alert("Test: Exception: " + ex.getMessage());
			System.exit(1);
		}

		// Test 1: Connection Establishment - Start Session

		log("Starting Test 1");

		try {

			eppGreeting = eppSession.connect(host, port);
			if (eppGreeting == null) alert("Test 1: No EppGreeting received on session connect.");

			eppChannel = eppSession.getChannel();

			eppGreeting = eppChannel.hello();
			if (eppGreeting == null) alert("Test 1: No EppGreeting received on hello().");

			dumpChannel(eppChannel);
			dumpGreeting(eppGreeting);
		} catch (Exception ex) {

			ex.printStackTrace(System.err);
			alert("Test 1: Exception: " + ex.getMessage());
			System.exit(1);
		}

		// Test 2: Login and Authentication

		log("Starting Test 2");

		try {

			EppCommandLogin eppCommandLogin = new EppCommandLogin(eppGreeting.getServiceMenu());
			eppCommandLogin.setClientTransactionId("xri-testcase02cmd");
			eppCommandLogin.setCreds(new EppCreds(clientid, password));

			EppResponse eppResponse = eppChannel.start(eppCommandLogin);
			EppResult eppResult = (EppResult) eppResponse.getResult().get(0);

			dumpCommand(2, eppCommandLogin);
			dumpResponse(2, eppResponse);

			if (doChecks) {

				if (! (eppResponse.success())) alert("Test 2: Unsuccessful");
				if (! (eppResult.getCode() == 1000)) alert("Test 2: Unexpected result code: " + eppResult.getCode());
				if (! (eppResult.getMessage().getMessage().equals("Command completed successfully"))) alert("Test 2: Unexpected msg: " + eppResult.getMessage().getMessage());
				if (! (eppResponse.getTransactionId().getClientTransactionId().equals("xri-testcase02cmd"))) alert("Test 2: Unexpected clTRID: " + eppResponse.getTransactionId().getClientTransactionId());
				if (! (eppResponse.getTransactionId().getServiceTransactionId().equals("xri-testcase02res"))) alert("Test 2: Unexpected svTRID: " + eppResponse.getTransactionId().getServiceTransactionId());
			}
		} catch (Exception ex) {

			ex.printStackTrace(System.err);
			alert("Test 2: Exception: " + ex.getMessage());
			System.exit(1);
		}

		// Test 3: Change Password

		log("Starting Test 3");

		try {

			eppChannel.terminate();
			eppSession.close();

			eppSession = new EppSessionTcp();
			eppSession.init("testkeys.client.prop");

			eppChannel = eppSession.getChannel();

			eppGreeting = eppSession.connect(host, port);
			if (eppGreeting == null) alert("Test 3: No EppGreeting received on session connect.");

			EppCommandLogin eppCommandLogin = new EppCommandLogin(eppGreeting.getServiceMenu());
			eppCommandLogin.setClientTransactionId("xri-testcase03cmd");
			eppCommandLogin.setCreds(new EppCreds(clientid, password));
			eppCommandLogin.setNewPassword("qwerty99");

			EppResponse eppResponse = eppChannel.start(eppCommandLogin);
			EppResult eppResult = (EppResult) eppResponse.getResult().get(0);

			dumpCommand(3, eppCommandLogin);
			dumpResponse(3, eppResponse);

			if (doChecks) {

				if (! (eppResponse.success())) alert("Test 3: Unsuccessful");
				if (! (eppResult.getCode() == 1000)) alert("Test 3: Unexpected result code: " + eppResult.getCode());
				if (! (eppResult.getMessage().getMessage().equals("Command completed successfully"))) alert("Test 3: Unexpected msg: " + eppResult.getMessage().getMessage());
				if (! (eppResponse.getTransactionId().getClientTransactionId().equals("xri-testcase03cmd"))) alert("Test 3: Unexpected clTRID: " + eppResponse.getTransactionId().getClientTransactionId());
				if (! (eppResponse.getTransactionId().getServiceTransactionId().equals("xri-testcase03res"))) alert("Test 3: Unexpected svTRID: " + eppResponse.getTransactionId().getServiceTransactionId());
			}
		} catch (Exception ex) {

			ex.printStackTrace(System.err);
			alert("Test 3: Exception: " + ex.getMessage());
			System.exit(1);
		}

		// Test 4: Create an Authority

		log("Starting Test 4");

		try {

			EppXriTrustee eppXriTrusteeEscrowAgent = new EppXriTrustee();
			eppXriTrusteeEscrowAgent.setAuthorityId("@!1001.1234.1234.1234");

			EppXriTrustee eppXriTrusteeContactAgent = new EppXriTrustee();
			eppXriTrusteeContactAgent.setAuthorityId("@!1001.6543.3456.9876");

			EppAddress eppAddress = new EppAddress();
			eppAddress.setStreet(new String[] { "Address1" });
			eppAddress.setCity("Vienna");
			eppAddress.setPostalCode("22182");
			eppAddress.setCountryCode("US");

			EppContactData eppContactData = new EppContactData();
			eppContactData.setName("Name1");
			eppContactData.setAddress(eppAddress);

			EppXriSocialData eppXriSocialData = new EppXriSocialData();
			eppXriSocialData.setPostalInfo(eppContactData);
			eppXriSocialData.setPrimaryVoice("+1.12312312341");
			eppXriSocialData.setPrimaryEmail("test1@test.com");

			EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, "password1");

			EppXriAuthority eppXriAuthority = new EppXriAuthority("=!(!!1001!123E!1235.1234.1241)");
			eppXriAuthority.setEscrowAgent(eppXriTrusteeEscrowAgent);
			eppXriAuthority.setContactAgent(eppXriTrusteeContactAgent);
			eppXriAuthority.setSocialData(eppXriSocialData);
			eppXriAuthority.setAuthInfo(eppAuthInfo);

			EppCommandCreate eppCommandCreate = EppCommand.create(eppXriAuthority, "xri-testcase04cmd");

			EppResponse eppResponse = eppChannel.send(eppCommandCreate);
			EppResult eppResult = (EppResult) eppResponse.getResult().get(0);
			EppResponseDataCreateXriAuthority eppResponseData = (EppResponseDataCreateXriAuthority) eppResponse.getResponseData();

			dumpCommand(4, eppCommandCreate);
			dumpResponse(4, eppResponse);

			if (doChecks) {

				if (! (eppResponse.success())) alert("Test 4: Unsuccessful");
				if (! (eppResult.getCode() == 1000)) alert("Test 4: Unexpected result code: " + eppResult.getCode());
				if (! (eppResult.getMessage().getMessage().equals("Command completed successfully"))) alert("Test 4: Unexpected msg: " + eppResult.getMessage().getMessage());
				if (! (eppResponse.getTransactionId().getClientTransactionId().equals("xri-testcase04cmd"))) alert("Test 4: Unexpected clTRID: " + eppResponse.getTransactionId().getClientTransactionId());
				if (! (eppResponse.getTransactionId().getServiceTransactionId().equals("xri-testcase04res"))) alert("Test 4: Unexpected svTRID: " + eppResponse.getTransactionId().getServiceTransactionId());
				if (! (eppResponseData.getAuthorityId().equals("=!(!!1001!123E!1235.1234.1241)"))) alert("Test 4: Unexpected authId in response data: " + eppResponseData.getAuthorityId());
			}
		} catch (Exception ex) {

			ex.printStackTrace(System.err);
			alert("Test 4: Exception: " + ex.getMessage());
		}

		// Test 5: Create an Authority with Maximum Length

		log("Starting Test 5");

		try {

			EppXriTrustee eppXriTrusteeEscrowAgent = new EppXriTrustee();
			eppXriTrusteeEscrowAgent.setAuthorityId("@!1001.1234.1234.1234");

			EppXriTrustee eppXriTrusteeContactAgent = new EppXriTrustee();
			eppXriTrusteeContactAgent.setAuthorityId("@!1001.6543.3456.9876");

			EppAddress eppAddress = new EppAddress();
			eppAddress.setStreet(new String[] { "address2" });
			eppAddress.setCity("city");
			eppAddress.setPostalCode("22031");
			eppAddress.setCountryCode("us");

			EppContactData eppContactData = new EppContactData();
			eppContactData.setName("name2");
			eppContactData.setAddress(eppAddress);

			EppXriSocialData eppXriSocialData = new EppXriSocialData();
			eppXriSocialData.setPostalInfo(eppContactData);
			eppXriSocialData.setPrimaryVoice("+1.1235161011");
			eppXriSocialData.setPrimaryEmail("test@test.com");

			EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, "password2");

			EppXriAuthority eppXriAuthority = new EppXriAuthority("@!(!!1001!123E!1235.1234.1240!1234.4321.1234!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.1111)");
			eppXriAuthority.setEscrowAgent(eppXriTrusteeEscrowAgent);
			eppXriAuthority.setContactAgent(eppXriTrusteeContactAgent);
			eppXriAuthority.setSocialData(eppXriSocialData);
			eppXriAuthority.setAuthInfo(eppAuthInfo);

			EppCommandCreate eppCommandCreate = EppCommand.create(eppXriAuthority, "xri-testcase05cmd");

			EppResponse eppResponse = eppChannel.send(eppCommandCreate);
			EppResult eppResult = (EppResult) eppResponse.getResult().get(0);
			EppResponseDataCreateXriAuthority eppResponseData = (EppResponseDataCreateXriAuthority) eppResponse.getResponseData();

			dumpCommand(5, eppCommandCreate);
			dumpResponse(5, eppResponse);

			if (doChecks) {

				if (! (eppResponse.success())) alert("Test 5: Unsuccessful");
				if (! (eppResult.getCode() == 1000)) alert("Test 5: Unexpected result code: " + eppResult.getCode());
				if (! (eppResult.getMessage().getMessage().equals("Command completed successfully"))) alert("Test 5: Unexpected msg: " + eppResult.getMessage().getMessage());
				if (! (eppResponse.getTransactionId().getClientTransactionId().equals("xri-testcase05cmd"))) alert("Test 5: Unexpected clTRID: " + eppResponse.getTransactionId().getClientTransactionId());
				if (! (eppResponse.getTransactionId().getServiceTransactionId().equals("xri-testcase05res"))) alert("Test 5: Unexpected svTRID: " + eppResponse.getTransactionId().getServiceTransactionId());
				if (! (eppResponseData.getAuthorityId().equals("@!(!!1001!123E!1235.1234.1240!1234.4321.1234!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.1111)"))) alert("Test 5: Unexpected authId in response data: " + eppResponseData.getAuthorityId());
			}
		} catch (Exception ex) {

			ex.printStackTrace(System.err);
			alert("Test 5: Exception: " + ex.getMessage());
		}

		// Test 6: Create an Authority with Minimum Length

		log("Starting Test 6");

		try {

			EppXriTrustee eppXriTrusteeEscrowAgent = new EppXriTrustee();
			eppXriTrusteeEscrowAgent.setAuthorityId("@!1001.1234.1234.1234");

			EppXriTrustee eppXriTrusteeContactAgent = new EppXriTrustee();
			eppXriTrusteeContactAgent.setAuthorityId("@!1001.6543.3456.9876");

			EppAddress eppAddress = new EppAddress();
			eppAddress.setStreet(new String[] { "address2" });
			eppAddress.setCity("city");
			eppAddress.setPostalCode("22031");
			eppAddress.setCountryCode("us");

			EppContactData eppContactData = new EppContactData();
			eppContactData.setName("name2");
			eppContactData.setAddress(eppAddress);

			EppXriSocialData eppXriSocialData = new EppXriSocialData();
			eppXriSocialData.setPostalInfo(eppContactData);
			eppXriSocialData.setPrimaryVoice("+1.1235161011");
			eppXriSocialData.setPrimaryEmail("test@test.com");

			EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, "password2");

			EppXriAuthority eppXriAuthority = new EppXriAuthority("@!(!!1001!123E!1)");
			eppXriAuthority.setEscrowAgent(eppXriTrusteeEscrowAgent);
			eppXriAuthority.setContactAgent(eppXriTrusteeContactAgent);
			eppXriAuthority.setSocialData(eppXriSocialData);
			eppXriAuthority.setAuthInfo(eppAuthInfo);

			EppCommandCreate eppCommandCreate = EppCommand.create(eppXriAuthority, "xri-testcase06cmd");

			EppResponse eppResponse = eppChannel.send(eppCommandCreate);
			EppResult eppResult = (EppResult) eppResponse.getResult().get(0);
			EppResponseDataCreateXriAuthority eppResponseData = (EppResponseDataCreateXriAuthority) eppResponse.getResponseData();

			dumpCommand(6, eppCommandCreate);
			dumpResponse(6, eppResponse);

			if (doChecks) {

				if (! (eppResponse.success())) alert("Test 6: Unsuccessful");
				if (! (eppResult.getCode() == 1000)) alert("Test 6: Unexpected result code: " + eppResult.getCode());
				if (! (eppResult.getMessage().getMessage().equals("Command completed successfully"))) alert("Test 6: Unexpected msg: " + eppResult.getMessage().getMessage());
				if (! (eppResponse.getTransactionId().getClientTransactionId().equals("xri-testcase06cmd"))) alert("Test 6: Unexpected clTRID: " + eppResponse.getTransactionId().getClientTransactionId());
				if (! (eppResponse.getTransactionId().getServiceTransactionId().equals("xri-testcase06res"))) alert("Test 6: Unexpected svTRID: " + eppResponse.getTransactionId().getServiceTransactionId());
				if (! (eppResponseData.getAuthorityId().equals("@!(!!1001!123E!1)"))) alert("Test 6: Unexpected authId in response data: " + eppResponseData.getAuthorityId());
			}
		} catch (Exception ex) {

			ex.printStackTrace(System.err);
			alert("Test 6: Exception: " + ex.getMessage());
		}

		// Test 7: Check Authority (Authority Not Available)

		log("Starting Test 7");

		try {

			EppCommandCheck eppCommandCheck = EppCommand.check(EppObject.XRI_AUTHORITY, "xri-testcase07cmd");
			eppCommandCheck.add("=!(!!1001!123E!1235.1234.1245)");
			eppCommandCheck.add("=!(!!1001!123E!1235.1234.1242)");

			EppResponse eppResponse = eppChannel.send(eppCommandCheck);
			EppResult eppResult = (EppResult) eppResponse.getResult().get(0);
			EppResponseDataCheckXriAuthority eppResponseData = (EppResponseDataCheckXriAuthority) eppResponse.getResponseData();

			dumpCommand(7, eppCommandCheck);
			dumpResponse(7, eppResponse);

			if (doChecks) {

				if (! (eppResponse.success())) alert("Test 7: Unsuccessful");
				if (! (eppResult.getCode() == 1000)) alert("Test 7: Unexpected result code: " + eppResult.getCode());
				if (! (eppResult.getMessage().getMessage().equals("Command completed successfully"))) alert("Test 7: Unexpected msg: " + eppResult.getMessage().getMessage());
				if (! (eppResponse.getTransactionId().getClientTransactionId().equals("xri-testcase07cmd"))) alert("Test 7: Unexpected clTRID: " + eppResponse.getTransactionId().getClientTransactionId());
				if (! (eppResponse.getTransactionId().getServiceTransactionId().equals("xri-testcase07res"))) alert("Test 7: Unexpected svTRID: " + eppResponse.getTransactionId().getServiceTransactionId());
				if (! (eppResponseData.getHashMap().containsKey("=!(!!1001!123E!1235.1234.1245)"))) alert("Test 7: =!(!!1001!123E!1235.1234.1245) not found in response data");
				if (! (eppResponseData.getHashMap().containsKey("=!(!!1001!123E!1235.1234.1242)"))) alert("Test 7: =!(!!1001!123E!1235.1234.1242) not found in response data");
				if (eppResponseData.isAvailable("=!(!!1001!123E!1235.1234.1245)")) alert("Test 7: =!(!!1001!123E!1235.1234.1245) is available, but should not be");
				if (eppResponseData.isAvailable("=!(!!1001!123E!1235.1234.1242)")) alert("Test 7: =!(!!1001!123E!1235.1234.1242) is available, but should not be");
			}
		} catch (Exception ex) {

			ex.printStackTrace(System.err);
			alert("Test 7: Exception: " + ex.getMessage());
		}

		// Test 8: Check Authority (Authority Available)

		log("Starting Test 8");

		try {

			EppCommandCheck eppCommandCheck = EppCommand.check(EppObject.XRI_AUTHORITY, "xri-testcase08cmd");
			eppCommandCheck.add("=!(!!1001!123E!1235.1234.1299)");

			EppResponse eppResponse = eppChannel.send(eppCommandCheck);
			EppResult eppResult = (EppResult) eppResponse.getResult().get(0);
			EppResponseDataCheckXriAuthority eppResponseData = (EppResponseDataCheckXriAuthority) eppResponse.getResponseData();

			dumpCommand(8, eppCommandCheck);
			dumpResponse(8, eppResponse);

			if (doChecks) {

				if (! (eppResponse.success())) alert("Test 8: Unsuccessful");
				if (! (eppResult.getCode() == 1000)) alert("Test 8: Unexpected result code: " + eppResult.getCode());
				if (! (eppResult.getMessage().getMessage().equals("Command completed successfully"))) alert("Test 8: Unexpected msg: " + eppResult.getMessage().getMessage());
				if (! (eppResponse.getTransactionId().getClientTransactionId().equals("xri-testcase08cmd"))) alert("Test 8: Unexpected clTRID: " + eppResponse.getTransactionId().getClientTransactionId());
				if (! (eppResponse.getTransactionId().getServiceTransactionId().equals("xri-testcase08res"))) alert("Test 8: Unexpected svTRID: " + eppResponse.getTransactionId().getServiceTransactionId());
				if (! (eppResponseData.getHashMap().containsKey("=!(!!1001!123E!1235.1234.1299)"))) alert("Test 8: =!(!!1001!123E!1235.1234.1299) not found in response data");
				if (! (eppResponseData.isAvailable("=!(!!1001!123E!1235.1234.1299)"))) alert("Test 8: =!(!!1001!123E!1235.1234.1299) is not available, but should be");
			}
		} catch (Exception ex) {

			ex.printStackTrace(System.err);
			alert("Test 8: Exception: " + ex.getMessage());
		}

		// Test 9: Check Authority with Maximum Length

		log("Starting Test 9");

		try {

			EppCommandCheck eppCommandCheck = EppCommand.check(EppObject.XRI_AUTHORITY, "xri-testcase09cmd");
			eppCommandCheck.add("@!(!!1001!123E!1235.1234.1240!1234.4321.1234!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.1111)");

			EppResponse eppResponse = eppChannel.send(eppCommandCheck);
			EppResult eppResult = (EppResult) eppResponse.getResult().get(0);
			EppResponseDataCheckXriAuthority eppResponseData = (EppResponseDataCheckXriAuthority) eppResponse.getResponseData();

			dumpCommand(9, eppCommandCheck);
			dumpResponse(9, eppResponse);

			if (doChecks) {

				if (! (eppResponse.success())) alert("Test 9: Unsuccessful");
				if (! (eppResult.getCode() == 1000)) alert("Test 9: Unexpected result code: " + eppResult.getCode());
				if (! (eppResult.getMessage().getMessage().equals("Command completed successfully"))) alert("Test 9: Unexpected msg: " + eppResult.getMessage().getMessage());
				if (! (eppResponse.getTransactionId().getClientTransactionId().equals("xri-testcase09cmd"))) alert("Test 9: Unexpected clTRID: " + eppResponse.getTransactionId().getClientTransactionId());
				if (! (eppResponse.getTransactionId().getServiceTransactionId().equals("xri-testcase09res"))) alert("Test 9: Unexpected svTRID: " + eppResponse.getTransactionId().getServiceTransactionId());
				if (! (eppResponseData.getHashMap().containsKey("@!(!!1001!123E!1235.1234.1240!1234.4321.1234!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.1111)"))) alert("Test 9: @!(!!1001!123E!1235.1234.1240!1234.4321.1234!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.1111) not found in response data");
				if (eppResponseData.isAvailable("@!(!!1001!123E!1235.1234.1240!1234.4321.1234!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.1111)")) alert("Test 9: @!(!!1001!123E!1235.1234.1240!1234.4321.1234!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.4321!4321.1234.1111) is available, but should not be");
			}
		} catch (Exception ex) {

			ex.printStackTrace(System.err);
			alert("Test 9: Exception: " + ex.getMessage());
		}

		// Test 10: Query Authority <info>

		log("Starting Test 10");

		try {

			EppCommandInfo eppCommandInfo = EppCommand.info(EppObject.XRI_AUTHORITY, "=!(!!1002!123E!1235.1234.1282)", "xri-testcase10cmd");

			EppResponse eppResponse = eppChannel.send(eppCommandInfo);
			EppResult eppResult = (EppResult) eppResponse.getResult().get(0);
			EppResponseDataInfo eppResponseData = (EppResponseDataInfo) eppResponse.getResponseData();
			EppXriAuthority eppXriAuthority = eppResponseData == null ? null : (EppXriAuthority) eppResponseData.getObject();
			EppXriTrustee eppXriTrusteeEscrowAgent = eppXriAuthority == null ? null : eppXriAuthority.getEscrowAgent();
			EppXriTrustee eppXriTrusteeContactAgent = eppXriAuthority == null ? null : eppXriAuthority.getContactAgent();
			EppStatus eppStatus = eppXriAuthority == null ? null : (EppStatus) eppXriAuthority.getStatus().get(0);
			EppXriSocialData eppXriSocialData = eppXriAuthority == null ? null : eppXriAuthority.getSocialData();
			EppContactData eppContactData = eppXriSocialData == null ? null : eppXriSocialData.getPostalInfo();
			EppAddress eppAddress = eppContactData == null ? null : eppContactData.getAddress();

			dumpCommand(10, eppCommandInfo);
			dumpResponse(10, eppResponse);

			if (doChecks) {

				if (! (eppResponse.success())) alert("Test 10: Unsuccessful");
				if (! (eppResult.getCode() == 1000)) alert("Test 10: Unexpected result code: " + eppResult.getCode());
				if (! (eppResult.getMessage().getMessage().equals("Command completed successfully"))) alert("Test 10: Unexpected msg: " + eppResult.getMessage().getMessage());
				if (! (eppResponse.getTransactionId().getClientTransactionId().equals("xri-testcase10cmd"))) alert("Test 10: Unexpected clTRID: " + eppResponse.getTransactionId().getClientTransactionId());
				if (! (eppResponse.getTransactionId().getServiceTransactionId().equals("xri-testcase10res"))) alert("Test 10: Unexpected svTRID: " + eppResponse.getTransactionId().getServiceTransactionId());
				if (! (eppXriAuthority.getAuthorityId().equals("=!(!!1002!123E!1235.1234.1282)"))) alert("Test 10: Unexpected authId in response data: " + eppXriAuthority.getAuthorityId());
				if (! (eppXriTrusteeEscrowAgent.getAuthorityId().equals("@!1001.1234.1234.1234"))) alert ("Test 10: Unexpected escrow authId in response data: " + eppXriTrusteeEscrowAgent.getAuthorityId());
				if (! (eppXriTrusteeContactAgent.getAuthorityId().equals("@!1000.6543.3456.9876"))) alert ("Test 10: Unexpected contact authId in response data: " + eppXriTrusteeContactAgent.getAuthorityId());
				if (! (eppXriAuthority.getRoid().equals("A635-PER"))) alert ("Test 10: Unexpected roid in response data: " + eppXriAuthority.getRoid());
				if (! (eppStatus.getStatus().equals("ok"))) alert ("Test 10: Unexpected status in response data: " + eppStatus.getStatus());
				if (! (eppContactData.getName().equals("Name2"))) alert ("Test 10: Unexpected name in response data: " + eppContactData.getName());
				if (! (eppAddress.getStreet()[0].equals("Address2"))) alert ("Test 10: Unexpected street in response data: " + eppAddress.getStreet()[0]);
				if (! (eppAddress.getCity().equals("Vienna"))) alert ("Test 10: Unexpected city in response data: " + eppAddress.getCity());
				if (! (eppAddress.getPostalCode().equals("22182"))) alert ("Test 10: Unexpected pc in response data: " + eppAddress.getPostalCode());
				if (! (eppAddress.getCountryCode().equals("US"))) alert ("Test 10: Unexpected cc in response data: " + eppAddress.getCountryCode());
				if (! (eppXriSocialData.getPrimaryVoice().equals("+1.12312312351"))) alert ("Test 10: Unexpected voice in response data: " + eppXriSocialData.getPrimaryVoice());
				if (! (eppXriSocialData.getPrimaryEmail().equals("test2@test.com"))) alert ("Test 10: Unexpected email in response data: " + eppXriSocialData.getPrimaryEmail());
				if (! (eppXriAuthority.getClientId().equals("NEUSTAR"))) alert ("Test 10: Unexpected clID in response data: " + eppXriAuthority.getClientId());
				if (! (eppXriAuthority.getClientIdCreated().equals("NEUSTAR"))) alert ("Test 10: Unexpected crID in response data: " + eppXriAuthority.getClientIdCreated());
				if (! (xmlDateFormat.format(eppXriAuthority.getDateCreated().getTime()).equals("2005-10-26T13:30:57.0Z"))) alert ("Test 10: Unexpected crDate in response data: " + eppXriAuthority.getDateCreated());
				if (! (eppXriAuthority.getAuthInfo().getValue().equals("password2"))) alert ("Test 10: Unexpected pw in response data: " + eppXriAuthority.getAuthInfo().getValue());
			}
		} catch (Exception ex) {

			ex.printStackTrace(System.err);
			alert("Test 10: Exception: " + ex.getMessage());
		}

		// Test 11: Transfer Query Command

		log("Starting Test 11");

		try {

			EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, "password2R");

			EppCommandTransfer eppCommandTransfer = EppCommand.transfer(EppObject.XRI_AUTHORITY, "=!(!!1001!123E!1235.1234.1301)", "xri-testcase11cmd");
			eppCommandTransfer.setOperation("query");
			eppCommandTransfer.setAuthInfo(eppAuthInfo);

			EppResponse eppResponse = eppChannel.send(eppCommandTransfer);
			EppResult eppResult = (EppResult) eppResponse.getResult().get(0);
			EppResponseDataTransferXriAuthority eppResponseData = (EppResponseDataTransferXriAuthority) eppResponse.getResponseData();

			dumpCommand(11, eppCommandTransfer);
			dumpResponse(11, eppResponse);

			if (doChecks) {

				if (! (eppResponse.success())) alert("Test 11: Unsuccessful");
				if (! (eppResult.getCode() == 1000)) alert("Test 11: Unexpected result code: " + eppResult.getCode());
				if (! (eppResult.getMessage().getMessage().equals("Command completed successfully"))) alert("Test 11: Unexpected msg: " + eppResult.getMessage().getMessage());
				if (! (eppResponse.getTransactionId().getClientTransactionId().equals("xri-testcase11cmd"))) alert("Test 11: Unexpected clTRID: " + eppResponse.getTransactionId().getClientTransactionId());
				if (! (eppResponse.getTransactionId().getServiceTransactionId().equals("xri-testcase11res"))) alert("Test 11: Unexpected svTRID: " + eppResponse.getTransactionId().getServiceTransactionId());
				if (! (eppResponseData.getAuthorityId().equals("=!(!!1001!123E!1235.1234.1301)"))) alert("Test 11: Unexpected authId in response data: " + eppResponseData.getAuthorityId());
				if (! (eppResponseData.getSourceAuthorityId().equals("testsource"))) alert("Test 11: Unexpected source in response data: " + eppResponseData.getSourceAuthorityId());
				if (! (eppResponseData.getTransferToken().equals("OneTimePass"))) alert("Test 11: Unexpected trToken in response data: " + eppResponseData.getTransferToken());
				if (! (eppResponseData.getTransferStatus().equals("pending"))) alert("Test 11: Unexpected trStatus in response data: " + eppResponseData.getTransferStatus());
				if (! (eppResponseData.getRequestClientId().equals("ClientX"))) alert("Test 11: Unexpected reID in response data: " + eppResponseData.getRequestClientId());
				if (! (xmlDateFormat.format(eppResponseData.getRequestDate().getTime()).equals("2005-05-06T22:00:00.0Z"))) alert ("Test 11: Unexpected reDate in response data: " + eppResponseData.getRequestDate());
				if (! (eppResponseData.getActionClientId().equals("ClientY"))) alert("Test 11: Unexpected acID in response data: " + eppResponseData.getActionClientId());
				if (! (xmlDateFormat.format(eppResponseData.getActionDate().getTime()).equals("2005-05-11T22:00:00.0Z"))) alert ("Test 11: Unexpected acDate in response data: " + eppResponseData.getActionDate());
			}
		} catch (Exception ex) {

			ex.printStackTrace(System.err);
			alert("Test 11: Exception: " + ex.getMessage());
		}

		// Test 12: Delete an Authority

		log("Starting Test 12");

		try {

			EppCommandDelete eppCommandDelete = EppCommand.delete(EppObject.XRI_AUTHORITY, "=!(!!1001!123E!1235.1234.1301)", "xri-testcase12cmd");

			EppResponse eppResponse = eppChannel.send(eppCommandDelete);
			EppResult eppResult = (EppResult) eppResponse.getResult().get(0);

			dumpCommand(12, eppCommandDelete);
			dumpResponse(12, eppResponse);

			if (doChecks) {

				if (! (eppResponse.success())) alert("Test 12: Unsuccessful");
				if (! (eppResult.getCode() == 1000)) alert("Test 12: Unexpected result code: " + eppResult.getCode());
				if (! (eppResult.getMessage().getMessage().equals("Command completed successfully"))) alert("Test 12: Unexpected msg: " + eppResult.getMessage().getMessage());
				if (! (eppResponse.getTransactionId().getClientTransactionId().equals("xri-testcase12cmd"))) alert("Test 12: Unexpected clTRID: " + eppResponse.getTransactionId().getClientTransactionId());
				if (! (eppResponse.getTransactionId().getServiceTransactionId().equals("xri-testcase12res"))) alert("Test 12: Unexpected svTRID: " + eppResponse.getTransactionId().getServiceTransactionId());
			}
		} catch (Exception ex) {

			ex.printStackTrace(System.err);
			alert("Test 12: Exception: " + ex.getMessage());
		}

		// Test 13: Transfer an Authority

		log("Starting Test 13");

		try {

			EppAuthInfo eppAuthInfoTarget = new EppAuthInfo(EppAuthInfo.TYPE_PW, "password1");

			EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, "password2");

			EppCommandTransferXriAuthority eppCommandTransfer = (EppCommandTransferXriAuthority) EppCommand.transfer(EppObject.XRI_AUTHORITY, "=!(!!1001!123E!1235.1234.1301)", "xri-testcase13cmd");
			eppCommandTransfer.setOperation("request");
			eppCommandTransfer.setTarget("=!(!!1001!123E!1235.1234.1302)", eppAuthInfoTarget);
			eppCommandTransfer.setTransferToken("OneTimePass");
			eppCommandTransfer.setAuthInfo(eppAuthInfo);

			EppResponse eppResponse = eppChannel.send(eppCommandTransfer);
			EppResult eppResult = (EppResult) eppResponse.getResult().get(0);
			EppResponseDataTransferXriAuthority eppResponseData = (EppResponseDataTransferXriAuthority) eppResponse.getResponseData();

			dumpCommand(13, eppCommandTransfer);
			dumpResponse(13, eppResponse);

			if (doChecks) {

				if (! (eppResponse.success())) alert("Test 13: Unsuccessful");
				if (! (eppResult.getCode() == 1001)) alert("Test 13: Unexpected result code: " + eppResult.getCode());
				if (! (eppResult.getMessage().getMessage().equals("Command completed successfully; action pending"))) alert("Test 13: Unexpected msg: " + eppResult.getMessage().getMessage());
				if (! (eppResponse.getTransactionId().getClientTransactionId().equals("xri-testcase13cmd"))) alert("Test 13: Unexpected clTRID: " + eppResponse.getTransactionId().getClientTransactionId());
				if (! (eppResponse.getTransactionId().getServiceTransactionId().equals("xri-testcase13res"))) alert("Test 13: Unexpected svTRID: " + eppResponse.getTransactionId().getServiceTransactionId());
				if (! (eppResponseData.getAuthorityId().equals("=!(!!1001!123E!1235.1234.1301)"))) alert("Test 13: Unexpected authId in response data: " + eppResponseData.getAuthorityId());
				if (! (eppResponseData.getTransferToken().equals("OneTimePass"))) alert("Test 13: Unexpected trToken in response data: " + eppResponseData.getTransferToken());
				if (! (eppResponseData.getTransferStatus().equals("pending"))) alert("Test 13: Unexpected trStatus in response data: " + eppResponseData.getTransferStatus());
				if (! (eppResponseData.getRequestClientId().equals("ClientX"))) alert("Test 13: Unexpected reID in response data: " + eppResponseData.getRequestClientId());
				if (! (xmlDateFormat.format(eppResponseData.getRequestDate().getTime()).equals("2005-05-06T22:00:00.0Z"))) alert ("Test 13: Unexpected reDate in response data: " + eppResponseData.getRequestDate());
				if (! (eppResponseData.getActionClientId().equals("ClientY"))) alert("Test 13: Unexpected acID in response data: " + eppResponseData.getActionClientId());
				if (! (xmlDateFormat.format(eppResponseData.getActionDate().getTime()).equals("2005-05-11T22:00:00.0Z"))) alert ("Test 13: Unexpected acDate in response data: " + eppResponseData.getActionDate());
			}
		} catch (Exception ex) {

			ex.printStackTrace(System.err);
			alert("Test 13: Exception: " + ex.getMessage());
		}

		// Test 14: Change Authority Trustees

		log("Starting Test 14");

		try {

			EppXriTrustee eppXriTrustee = new EppXriTrustee();
			eppXriTrustee.setINumber("=!23FA.C835.7449.553D");

			EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, "password1");

			EppCommandUpdateXriAuthority eppCommandUpdate = (EppCommandUpdateXriAuthority) EppCommand.update(EppObject.XRI_AUTHORITY, "=!(!!1001!123E!1235.1234.1241)", "xri-testcase14cmd");
			eppCommandUpdate.addTrustee(eppXriTrustee);
			eppCommandUpdate.setAuthInfo(eppAuthInfo);

			EppResponse eppResponse = eppChannel.send(eppCommandUpdate);
			EppResult eppResult = (EppResult) eppResponse.getResult().get(0);

			dumpCommand(14, eppCommandUpdate);
			dumpResponse(14, eppResponse);

			if (doChecks) {

				if (! (eppResponse.success())) alert("Test 14: Unsuccessful");
				if (! (eppResult.getCode() == 1000)) alert("Test 14: Unexpected result code: " + eppResult.getCode());
				if (! (eppResult.getMessage().getMessage().equals("Command completed successfully"))) alert("Test 14: Unexpected msg: " + eppResult.getMessage().getMessage());
				if (! (eppResponse.getTransactionId().getClientTransactionId().equals("xri-testcase14cmd"))) alert("Test 14: Unexpected clTRID: " + eppResponse.getTransactionId().getClientTransactionId());
				if (! (eppResponse.getTransactionId().getServiceTransactionId().equals("xri-testcase14res"))) alert("Test 14: Unexpected svTRID: " + eppResponse.getTransactionId().getServiceTransactionId());
			}
		} catch (Exception ex) {

			ex.printStackTrace(System.err);
			alert("Test 14: Exception: " + ex.getMessage());
		}

		// Test 15: Change Escrow and contact agent

		log("Starting Test 15");

		try {

			EppXriTrustee eppXriTrusteeEscrowAgent = new EppXriTrustee();
			eppXriTrusteeEscrowAgent.setINumber("@!1234.1234.1234.1234");

			EppXriTrustee eppXriTrusteeContactAgent = new EppXriTrustee();
			eppXriTrusteeContactAgent.setINumber("@!1234.1234.1234.1234");
			eppXriTrusteeContactAgent.setExternal(false);

			EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, "password1");

			EppCommandUpdateXriAuthority eppCommandUpdate = (EppCommandUpdateXriAuthority) EppCommand.update(EppObject.XRI_AUTHORITY, "=!(!!1001!123E!1235.1234.1241)", "xri-testcase15cmd");
			eppCommandUpdate.setNewEscrowAgent(eppXriTrusteeEscrowAgent);
			eppCommandUpdate.setNewContactAgent(eppXriTrusteeContactAgent);
			eppCommandUpdate.setAuthInfo(eppAuthInfo);

			EppResponse eppResponse = eppChannel.send(eppCommandUpdate);
			EppResult eppResult = (EppResult) eppResponse.getResult().get(0);

			dumpCommand(15, eppCommandUpdate);
			dumpResponse(15, eppResponse);

			if (doChecks) {

				if (! (eppResponse.success())) alert("Test 15: Unsuccessful");
				if (! (eppResult.getCode() == 1000)) alert("Test 15: Unexpected result code: " + eppResult.getCode());
				if (! (eppResult.getMessage().getMessage().equals("Command completed successfully"))) alert("Test 15: Unexpected msg: " + eppResult.getMessage().getMessage());
				if (! (eppResponse.getTransactionId().getClientTransactionId().equals("xri-testcase15cmd"))) alert("Test 15: Unexpected clTRID: " + eppResponse.getTransactionId().getClientTransactionId());
				if (! (eppResponse.getTransactionId().getServiceTransactionId().equals("xri-testcase15res"))) alert("Test 15: Unexpected svTRID: " + eppResponse.getTransactionId().getServiceTransactionId());
			}
		} catch (Exception ex) {

			ex.printStackTrace(System.err);
			alert("Test 15: Exception: " + ex.getMessage());
		}

		// Test 16: Change Authority Social Data

		log("Starting Test 16");

		try {

			EppAddress eppAddress = new EppAddress();
			eppAddress.setStreet(new String[] { "address2" });
			eppAddress.setCity("fairfax");
			eppAddress.setPostalCode("20171");
			eppAddress.setCountryCode("us");

			EppContactData eppContactData = new EppContactData();
			eppContactData.setName("name2");
			eppContactData.setAddress(eppAddress);

			EppXriSocialData eppXriSocialData = new EppXriSocialData();
			eppXriSocialData.setPostalInfo(eppContactData);
			eppXriSocialData.setPrimaryVoice("+1.7675361872");
			eppXriSocialData.setPrimaryEmail("email@changed.com");

			EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, "password2");

			EppCommandUpdateXriAuthority eppCommandUpdate = (EppCommandUpdateXriAuthority) EppCommand.update(EppObject.XRI_AUTHORITY, "=!(!!1001!123E!1235.1234.1272)", "xri-testcase16cmd");
			eppCommandUpdate.setNewSocialData(eppXriSocialData);
			eppCommandUpdate.setAuthInfo(eppAuthInfo);

			EppResponse eppResponse = eppChannel.send(eppCommandUpdate);
			EppResult eppResult = (EppResult) eppResponse.getResult().get(0);

			dumpCommand(16, eppCommandUpdate);
			dumpResponse(16, eppResponse);

			if (doChecks) {

				if (! (eppResponse.success())) alert("Test 16: Unsuccessful");
				if (! (eppResult.getCode() == 1000)) alert("Test 16: Unexpected result code: " + eppResult.getCode());
				if (! (eppResult.getMessage().getMessage().equals("Command completed successfully"))) alert("Test 16: Unexpected msg: " + eppResult.getMessage().getMessage());
				if (! (eppResponse.getTransactionId().getClientTransactionId().equals("xri-testcase16cmd"))) alert("Test 16: Unexpected clTRID: " + eppResponse.getTransactionId().getClientTransactionId());
				if (! (eppResponse.getTransactionId().getServiceTransactionId().equals("xri-testcase16res"))) alert("Test 16: Unexpected svTRID: " + eppResponse.getTransactionId().getServiceTransactionId());
			}
		} catch (Exception ex) {

			ex.printStackTrace(System.err);
			alert("Test 16: Exception: " + ex.getMessage());
		}

		// Test 17: Change Authority Status and Add Service Endpoint

		log("Starting Test 17");

		try {

			EppXriServiceEndpoint eppXriServiceEndpoint = new EppXriServiceEndpoint();
			eppXriServiceEndpoint.setId("test7");
			eppXriServiceEndpoint.setPriority(1);
			eppXriServiceEndpoint.addType("xri://$res*auth.res/XRI", "null", Boolean.TRUE);
			eppXriServiceEndpoint.addURI("test", 10, "qxri");

			EppStatus eppStatus = new EppStatus("clientHold");

			EppXriSocialData eppSocialData = new EppXriSocialData();

			EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, "password4");

			EppCommandUpdateXriAuthority eppCommandUpdate = (EppCommandUpdateXriAuthority) EppCommand.update(EppObject.XRI_AUTHORITY, "=!(!!1001!123E!1235.1234.1244)", "xri-testcase17cmd");
			eppCommandUpdate.addServiceEndpoint(eppXriServiceEndpoint);
			eppCommandUpdate.addStatus(eppStatus);
			eppCommandUpdate.setNewSocialData(eppSocialData);
			eppCommandUpdate.setAuthInfo(eppAuthInfo);

			EppResponse eppResponse = eppChannel.send(eppCommandUpdate);
			EppResult eppResult = (EppResult) eppResponse.getResult().get(0);

			dumpCommand(17, eppCommandUpdate);
			dumpResponse(17, eppResponse);

			if (doChecks) {

				if (! (eppResponse.success())) alert("Test 17: Unsuccessful");
				if (! (eppResult.getCode() == 1000)) alert("Test 17: Unexpected result code: " + eppResult.getCode());
				if (! (eppResult.getMessage().getMessage().equals("Command completed successfully"))) alert("Test 17: Unexpected msg: " + eppResult.getMessage().getMessage());
				if (! (eppResponse.getTransactionId().getClientTransactionId().equals("xri-testcase17cmd"))) alert("Test 17: Unexpected clTRID: " + eppResponse.getTransactionId().getClientTransactionId());
				if (! (eppResponse.getTransactionId().getServiceTransactionId().equals("xri-testcase17res"))) alert("Test 17: Unexpected svTRID: " + eppResponse.getTransactionId().getServiceTransactionId());
			}
		} catch (Exception ex) {

			ex.printStackTrace(System.err);
			alert("Test 17: Exception: " + ex.getMessage());
		}

		// Test 18: Add Service Endpoints

		log("Starting Test 18");

		try {

			EppXriServiceEndpoint eppXriServiceEndpoint1 = new EppXriServiceEndpoint();
			eppXriServiceEndpoint1.setId("test7");
			eppXriServiceEndpoint1.setPriority(1);
			eppXriServiceEndpoint1.addType("xri://$res*auth.res/XRI", "null", Boolean.TRUE);
			eppXriServiceEndpoint1.addURI("test", 10, "qxri");

			EppXriServiceEndpoint eppXriServiceEndpoint2 = new EppXriServiceEndpoint();
			eppXriServiceEndpoint2.setId("test8");
			eppXriServiceEndpoint2.setPriority(1);
			eppXriServiceEndpoint2.addType("xri://+contact*($v*1.0)", null, Boolean.TRUE);
			eppXriServiceEndpoint2.addType(null, "null", null);
			eppXriServiceEndpoint2.addPath("(+contact)", null, Boolean.TRUE);
			eppXriServiceEndpoint2.addPath(null, "null", null);
			eppXriServiceEndpoint2.addURI("http://contact.2idi.com/(+contact)*($v*1.0)/(=!1234.5678.A1B2.C3D4)?xri=", 10, "qxri");

			EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, "password4");

			EppCommandUpdateXriAuthority eppCommandUpdate = (EppCommandUpdateXriAuthority) EppCommand.update(EppObject.XRI_AUTHORITY, "=!(!!1001!123E!1235.1234.1244)", "xri-testcase18cmd");
			eppCommandUpdate.addServiceEndpoint(eppXriServiceEndpoint1);
			eppCommandUpdate.addServiceEndpoint(eppXriServiceEndpoint2);
			eppCommandUpdate.setAuthInfo(eppAuthInfo);

			EppResponse eppResponse = eppChannel.send(eppCommandUpdate);
			EppResult eppResult = (EppResult) eppResponse.getResult().get(0);

			dumpCommand(18, eppCommandUpdate);
			dumpResponse(18, eppResponse);

			if (doChecks) {

				if (! (eppResponse.success())) alert("Test 18: Unsuccessful");
				if (! (eppResult.getCode() == 1000)) alert("Test 18: Unexpected result code: " + eppResult.getCode());
				if (! (eppResult.getMessage().getMessage().equals("Command completed successfully"))) alert("Test 18: Unexpected msg: " + eppResult.getMessage().getMessage());
				if (! (eppResponse.getTransactionId().getClientTransactionId().equals("xri-testcase18cmd"))) alert("Test 18: Unexpected clTRID: " + eppResponse.getTransactionId().getClientTransactionId());
				if (! (eppResponse.getTransactionId().getServiceTransactionId().equals("xri-testcase18res"))) alert("Test 18: Unexpected svTRID: " + eppResponse.getTransactionId().getServiceTransactionId());
			}
		} catch (Exception ex) {

			ex.printStackTrace(System.err);
			alert("Test 18: Exception: " + ex.getMessage());
		}

		// Test 19: Add and Remove Service Endpoints

		log("Starting Test 19");

		try {

			EppXriServiceEndpoint eppXriServiceEndpoint = new EppXriServiceEndpoint();
			eppXriServiceEndpoint.setId("test9");
			eppXriServiceEndpoint.setPriority(1);
			eppXriServiceEndpoint.addType("xri://+contact*($v*1.0)", null, Boolean.TRUE);
			eppXriServiceEndpoint.addType(null, "null", null);
			eppXriServiceEndpoint.addPath("(+contact)", null, Boolean.TRUE);
			eppXriServiceEndpoint.addPath(null, "null", null);
			eppXriServiceEndpoint.addURI("http://contact.encirca.com/(+contact)*($v*1.0)/(=!1234.5678.A1B2.C3D4)?xri=", 10, "qxri");

			EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, "password4");

			EppCommandUpdateXriAuthority eppCommandUpdate = (EppCommandUpdateXriAuthority) EppCommand.update(EppObject.XRI_AUTHORITY, "=!(!!1001!123E!1235.1234.1244)", "xri-testcase19cmd");
			eppCommandUpdate.removeServiceEndpoint("test8");
			eppCommandUpdate.addServiceEndpoint(eppXriServiceEndpoint);
			eppCommandUpdate.setAuthInfo(eppAuthInfo);

			EppResponse eppResponse = eppChannel.send(eppCommandUpdate);
			EppResult eppResult = (EppResult) eppResponse.getResult().get(0);

			dumpCommand(19, eppCommandUpdate);
			dumpResponse(19, eppResponse);

			if (doChecks) {

				if (! (eppResponse.success())) alert("Test 19: Unsuccessful");
				if (! (eppResult.getCode() == 1000)) alert("Test 19: Unexpected result code: " + eppResult.getCode());
				if (! (eppResult.getMessage().getMessage().equals("Command completed successfully"))) alert("Test 19: Unexpected msg: " + eppResult.getMessage().getMessage());
				if (! (eppResponse.getTransactionId().getClientTransactionId().equals("xri-testcase19cmd"))) alert("Test 19: Unexpected clTRID: " + eppResponse.getTransactionId().getClientTransactionId());
				if (! (eppResponse.getTransactionId().getServiceTransactionId().equals("xri-testcase19res"))) alert("Test 19: Unexpected svTRID: " + eppResponse.getTransactionId().getServiceTransactionId());
			}
		} catch (Exception ex) {

			ex.printStackTrace(System.err);
			alert("Test 19: Exception: " + ex.getMessage());
		}

		// Test 20: Create I-Number

		log("Starting Test 20");

		try {

			EppPeriod eppPeriod = new EppPeriod(2, EppPeriod.UNIT_YEAR);

			EppXriNumber eppXriNumber = new EppXriNumber(null);
			eppXriNumber.setReferenceId("test_ref_id_015");
			eppXriNumber.setAuthorityId("=!(!!1001!123E!1235.1234.1247)");
			eppXriNumber.setPriority(1);
			eppXriNumber.setPeriod(eppPeriod);

			EppCommandCreate eppCommandCreate = EppCommand.create(eppXriNumber, "xri-testcase20cmd");

			EppResponse eppResponse = eppChannel.send(eppCommandCreate);
			EppResult eppResult = (EppResult) eppResponse.getResult().get(0);
			EppResponseDataCreateXriNumber eppResponseData = (EppResponseDataCreateXriNumber) eppResponse.getResponseData();

			dumpCommand(20, eppCommandCreate);
			dumpResponse(20, eppResponse);

			if (doChecks) {

				if (! (eppResponse.success())) alert("Test 20: Unsuccessful");
				if (! (eppResult.getCode() == 1000)) alert("Test 20: Unexpected result code: " + eppResult.getCode());
				if (! (eppResult.getMessage().getMessage().equals("Command completed successfully"))) alert("Test 20: Unexpected msg: " + eppResult.getMessage().getMessage());
				if (! (eppResponse.getTransactionId().getClientTransactionId().equals("xri-testcase20cmd"))) alert("Test 20: Unexpected clTRID: " + eppResponse.getTransactionId().getClientTransactionId());
				if (! (eppResponse.getTransactionId().getServiceTransactionId().equals("xri-testcase20res"))) alert("Test 20: Unexpected svTRID: " + eppResponse.getTransactionId().getServiceTransactionId());
				if (! (eppResponseData.getINumber().equals("=!848E.BAF.29E0.D18A"))) alert("Test 20: Unexpected inumber in response data: " + eppResponseData.getINumber());
				if (! (eppResponseData.getReferenceId().equals("test_ref_id_015"))) alert("Test 20: Unexpected refId in response data: " + eppResponseData.getReferenceId());
				if (! (xmlDateFormat.format(eppResponseData.getDateCreated().getTime()).equals("2005-09-22T14:25:05.0Z"))) alert ("Test 20: Unexpected crDate in response data: " + eppResponseData.getDateCreated());
				if (! (xmlDateFormat.format(eppResponseData.getDateExpired().getTime()).equals("2007-09-21T23:59:59.0Z"))) alert ("Test 20: Unexpected exDate in response data: " + eppResponseData.getDateExpired());
			}
		} catch (Exception ex) {

			ex.printStackTrace(System.err);
			alert("Test 20: Exception: " + ex.getMessage());
		}

		// Test 21: Create I-Number with Maximum Length

		log("Starting Test 21");

		try {

			EppXriNumber eppXriNumber = new EppXriNumber(null);
			eppXriNumber.setReferenceId("max_length_of_128_max_length_of_128_max_length_of_128_max_length_of_128_max_length_of_128_max_length_of_128_12");
			eppXriNumber.setAuthorityId("@!(!!1001!123E!1235.1234.1249)");

			EppCommandCreate eppCommandCreate = EppCommand.create(eppXriNumber, "xri-testcase21cmd");

			EppResponse eppResponse = eppChannel.send(eppCommandCreate);
			EppResult eppResult = (EppResult) eppResponse.getResult().get(0);
			EppResponseDataCreateXriNumber eppResponseData = (EppResponseDataCreateXriNumber) eppResponse.getResponseData();

			dumpCommand(21, eppCommandCreate);
			dumpResponse(21, eppResponse);

			if (doChecks) {

				if (! (eppResponse.success())) alert("Test 21: Unsuccessful");
				if (! (eppResult.getCode() == 1000)) alert("Test 21: Unexpected result code: " + eppResult.getCode());
				if (! (eppResult.getMessage().getMessage().equals("Command completed successfully"))) alert("Test 21: Unexpected msg: " + eppResult.getMessage().getMessage());
				if (! (eppResponse.getTransactionId().getClientTransactionId().equals("xri-testcase21cmd"))) alert("Test 21: Unexpected clTRID: " + eppResponse.getTransactionId().getClientTransactionId());
				if (! (eppResponse.getTransactionId().getServiceTransactionId().equals("xri-testcase21res"))) alert("Test 21: Unexpected svTRID: " + eppResponse.getTransactionId().getServiceTransactionId());
				if (! (eppResponseData.getINumber().equals("@!2E7A.2513.F137.146D"))) alert("Test 21: Unexpected inumber in response data: " + eppResponseData.getINumber());
				if (! (eppResponseData.getReferenceId().equals("max_length_of_128_max_length_of_128_max_length_of_128_max_length_of_128_max_length_of_128_max_length_of_128_12"))) alert("Test 21: Unexpected refId in response data: " + eppResponseData.getReferenceId());
				if (! (xmlDateFormat.format(eppResponseData.getDateCreated().getTime()).equals("2005-10-07T14:05:51.0Z"))) alert ("Test 21: Unexpected crDate in response data: " + eppResponseData.getDateCreated());
				if (! (xmlDateFormat.format(eppResponseData.getDateExpired().getTime()).equals("2006-10-06T23:59:59.0Z"))) alert ("Test 21: Unexpected exDate in response data: " + eppResponseData.getDateExpired());
			}
		} catch (Exception ex) {

			ex.printStackTrace(System.err);
			alert("Test 21: Exception: " + ex.getMessage());
		}

		// Test 22: Check I-Number (I-Number Known)

		log("Starting Test 22");

		try {

			EppCommandCheck eppCommandCheck = EppCommand.check(EppObject.XRI_INUMBER, "xri-testcase22cmd");
			eppCommandCheck.add("=!9790.4145.7D68.94F3");

			EppResponse eppResponse = eppChannel.send(eppCommandCheck);
			EppResult eppResult = (EppResult) eppResponse.getResult().get(0);
			EppResponseDataCheckXriNumber eppResponseData = (EppResponseDataCheckXriNumber) eppResponse.getResponseData();

			dumpCommand(22, eppCommandCheck);
			dumpResponse(22, eppResponse);

			if (doChecks) {

				if (! (eppResponse.success())) alert("Test 22: Unsuccessful");
				if (! (eppResult.getCode() == 1000)) alert("Test 22: Unexpected result code: " + eppResult.getCode());
				if (! (eppResult.getMessage().getMessage().equals("Command completed successfully"))) alert("Test 22: Unexpected msg: " + eppResult.getMessage().getMessage());
				if (! (eppResponse.getTransactionId().getClientTransactionId().equals("xri-testcase22cmd"))) alert("Test 22: Unexpected clTRID: " + eppResponse.getTransactionId().getClientTransactionId());
				if (! (eppResponse.getTransactionId().getServiceTransactionId().equals("xri-testcase22res"))) alert("Test 22: Unexpected svTRID: " + eppResponse.getTransactionId().getServiceTransactionId());
				if (! (eppResponseData.getHashMap().containsKey("=!9790.4145.7D68.94F3"))) alert("Test 22: =!9790.4145.7D68.94F3 not found in response data");
				if (eppResponseData.isAvailable("=!9790.4145.7D68.94F3")) alert("Test 22: =!9790.4145.7D68.94F3 is available, but should not be");
			}
		} catch (Exception ex) {

			ex.printStackTrace(System.err);
			alert("Test 22: Exception: " + ex.getMessage());
		}

		// Test 23: Check I-Number (I-Number Unknown)

		log("Starting Test 23");

		try {

			EppCommandCheck eppCommandCheck = EppCommand.check(EppObject.XRI_INUMBER, "xri-testcase23cmd");
			eppCommandCheck.add("@!9790.4145.0909.94F3");

			EppResponse eppResponse = eppChannel.send(eppCommandCheck);
			EppResult eppResult = (EppResult) eppResponse.getResult().get(0);
			EppResponseDataCheckXriNumber eppResponseData = (EppResponseDataCheckXriNumber) eppResponse.getResponseData();

			dumpCommand(23, eppCommandCheck);
			dumpResponse(23, eppResponse);

			if (doChecks) {

				if (! (eppResponse.success())) alert("Test 23: Unsuccessful");
				if (! (eppResult.getCode() == 1000)) alert("Test 23: Unexpected result code: " + eppResult.getCode());
				if (! (eppResult.getMessage().getMessage().equals("Command completed successfully"))) alert("Test 23: Unexpected msg: " + eppResult.getMessage().getMessage());
				if (! (eppResponse.getTransactionId().getClientTransactionId().equals("xri-testcase23cmd"))) alert("Test 23: Unexpected clTRID: " + eppResponse.getTransactionId().getClientTransactionId());
				if (! (eppResponse.getTransactionId().getServiceTransactionId().equals("xri-testcase23res"))) alert("Test 23: Unexpected svTRID: " + eppResponse.getTransactionId().getServiceTransactionId());
				if (! (eppResponseData.getHashMap().containsKey("@!9790.4145.0909.94F3"))) alert("Test 23: @!9790.4145.0909.94F3 not found in response data");
				if (! (eppResponseData.isAvailable("@!9790.4145.0909.94F3)"))) alert("Test 23: @!9790.4145.0909.94F3 is not available, but should be");
			}
		} catch (Exception ex) {

			ex.printStackTrace(System.err);
			alert("Test 23: Exception: " + ex.getMessage());
		}

		// Test 24: The <info> I-Number Command

		log("Starting Test 24");

		try {

			EppCommandInfo eppCommandInfo = EppCommand.info(EppObject.XRI_INUMBER, "=!39C1.85BC.FCDD.A699", "xri-testcase24cmd");

			EppResponse eppResponse = eppChannel.send(eppCommandInfo);
			EppResult eppResult = (EppResult) eppResponse.getResult().get(0);
			EppResponseDataInfo eppResponseData = (EppResponseDataInfo) eppResponse.getResponseData();
			EppXriNumber eppXriNumber = eppResponseData == null ? null : (EppXriNumber) eppResponseData.getObject();
			EppStatus eppStatus = eppXriNumber == null ? null : (EppStatus) eppXriNumber.getStatus().get(0);

			dumpCommand(24, eppCommandInfo);
			dumpResponse(24, eppResponse);

			if (doChecks) {

				if (! (eppResponse.success())) alert("Test 24: Unsuccessful");
				if (! (eppResult.getCode() == 1000)) alert("Test 24: Unexpected result code: " + eppResult.getCode());
				if (! (eppResult.getMessage().getMessage().equals("Command completed successfully"))) alert("Test 24: Unexpected msg: " + eppResult.getMessage().getMessage());
				if (! (eppResponse.getTransactionId().getClientTransactionId().equals("xri-testcase24cmd"))) alert("Test 24: Unexpected clTRID: " + eppResponse.getTransactionId().getClientTransactionId());
				if (! (eppResponse.getTransactionId().getServiceTransactionId().equals("xri-testcase24res"))) alert("Test 24: Unexpected svTRID: " + eppResponse.getTransactionId().getServiceTransactionId());
				if (! (eppXriNumber.getINumber().equals("=!39C1.85BC.FCDD.A699"))) alert("Test 24: Unexpected inumber in response data: " + eppXriNumber.getINumber());
				if (! (eppXriNumber.getRoid().equals("NUM455-PER"))) alert ("Test 24: Unexpected roid in response data: " + eppXriNumber.getRoid());
				if (! (eppStatus.getStatus().equals("ok"))) alert ("Test 24: Unexpected status in response data: " + eppStatus.getStatus());
				if (! (eppXriNumber.getAuthorityId().equals("=!(!!1001!123E!1235.1234.1242)"))) alert ("Test 24: Unexpected authId in response data: " + eppXriNumber.getAuthorityId());
				if (! (eppXriNumber.getPriority() == 10)) alert ("Test 24: Unexpected priority in response data: " + eppXriNumber.getPriority());
				if (! (eppXriNumber.getClientId().equals("NEUSTAR"))) alert ("Test 24: Unexpected clID in response data: " + eppXriNumber.getClientId());
				if (! (eppXriNumber.getClientIdCreated().equals("NEUSTAR"))) alert ("Test 24: Unexpected crID in response data: " + eppXriNumber.getClientIdCreated());
				if (! (xmlDateFormat.format(eppXriNumber.getDateCreated().getTime()).equals("2005-09-21T19:09:33.0Z"))) alert ("Test 24: Unexpected crDate in response data: " + eppXriNumber.getDateCreated());
				if (! (xmlDateFormat.format(eppXriNumber.getDateExpired().getTime()).equals("2006-09-20T23:59:59.0Z"))) alert ("Test 24: Unexpected exDate in response data: " + eppXriNumber.getDateExpired());
			}
		} catch (Exception ex) {

			ex.printStackTrace(System.err);
			alert("Test 24: Exception: " + ex.getMessage());
		}

		// Test 25: Delete I-Number

		log("Starting Test 25");

		try {

			EppCommandDelete eppCommandDelete = EppCommand.delete(EppObject.XRI_INUMBER, "!!1002!2222", "xri-testcase25cmd");

			EppResponse eppResponse = eppChannel.send(eppCommandDelete);
			EppResult eppResult = (EppResult) eppResponse.getResult().get(0);

			dumpCommand(25, eppCommandDelete);
			dumpResponse(25, eppResponse);

			if (doChecks) {

				if (! (eppResponse.success())) alert("Test 25: Unsuccessful");
				if (! (eppResult.getCode() == 1000)) alert("Test 25: Unexpected result code: " + eppResult.getCode());
				if (! (eppResult.getMessage().getMessage().equals("Command completed successfully"))) alert("Test 25: Unexpected msg: " + eppResult.getMessage().getMessage());
				if (! (eppResponse.getTransactionId().getClientTransactionId().equals("xri-testcase25cmd"))) alert("Test 25: Unexpected clTRID: " + eppResponse.getTransactionId().getClientTransactionId());
				if (! (eppResponse.getTransactionId().getServiceTransactionId().equals("xri-testcase25res"))) alert("Test 25: Unexpected svTRID: " + eppResponse.getTransactionId().getServiceTransactionId());
			}
		} catch (Exception ex) {

			ex.printStackTrace(System.err);
			alert("Test 25: Exception: " + ex.getMessage());
		}

		// Test 26: Update I-Number

		log("Starting Test 26");

		try {

			EppCommandUpdateXriNumber eppCommandUpdate = (EppCommandUpdateXriNumber) EppCommand.update(EppObject.XRI_INUMBER, "=!D8C5.B2C.A03.3460", "xri-testcase26cmd");
			eppCommandUpdate.setNewPriority(2);

			EppResponse eppResponse = eppChannel.send(eppCommandUpdate);
			EppResult eppResult = (EppResult) eppResponse.getResult().get(0);

			dumpCommand(26, eppCommandUpdate);
			dumpResponse(26, eppResponse);

			if (doChecks) {

				if (! (eppResponse.success())) alert("Test 26: Unsuccessful");
				if (! (eppResult.getCode() == 1000)) alert("Test 26: Unexpected result code: " + eppResult.getCode());
				if (! (eppResult.getMessage().getMessage().equals("Command completed successfully"))) alert("Test 26: Unexpected msg: " + eppResult.getMessage().getMessage());
				if (! (eppResponse.getTransactionId().getClientTransactionId().equals("xri-testcase26cmd"))) alert("Test 26: Unexpected clTRID: " + eppResponse.getTransactionId().getClientTransactionId());
				if (! (eppResponse.getTransactionId().getServiceTransactionId().equals("xri-testcase26res"))) alert("Test 26: Unexpected svTRID: " + eppResponse.getTransactionId().getServiceTransactionId());
			}
		} catch (Exception ex) {

			ex.printStackTrace(System.err);
			alert("Test 26: Exception: " + ex.getMessage());
		}

		// Test 27: Renew I-Number

		log("Starting Test 27");

		try {

			Calendar curExpDate = Calendar.getInstance();
			curExpDate.setTime(xmlShortDateFormat.parse("2006-05-03"));

			EppPeriod eppPeriod = new EppPeriod(5, EppPeriod.UNIT_YEAR);

			EppCommandRenewXriNumber eppCommandRenew = (EppCommandRenewXriNumber) EppCommand.renew(EppObject.XRI_INUMBER, "=!1002.2222.3333.4444", "xri-testcase27cmd");
			eppCommandRenew.setCurrentExpireDate(curExpDate);
			eppCommandRenew.setPeriod(eppPeriod);

			EppResponse eppResponse = eppChannel.send(eppCommandRenew);
			EppResult eppResult = (EppResult) eppResponse.getResult().get(0);
			EppResponseDataRenewXriNumber eppResponseData = (EppResponseDataRenewXriNumber) eppResponse.getResponseData();

			dumpCommand(27, eppCommandRenew);
			dumpResponse(27, eppResponse);

			if (doChecks) {

				if (! (eppResponse.success())) alert("Test 27: Unsuccessful");
				if (! (eppResult.getCode() == 1000)) alert("Test 27: Unexpected result code: " + eppResult.getCode());
				if (! (eppResult.getMessage().getMessage().equals("Command completed successfully"))) alert("Test 27: Unexpected msg: " + eppResult.getMessage().getMessage());
				if (! (eppResponse.getTransactionId().getClientTransactionId().equals("xri-testcase27cmd"))) alert("Test 27: Unexpected clTRID: " + eppResponse.getTransactionId().getClientTransactionId());
				if (! (eppResponse.getTransactionId().getServiceTransactionId().equals("xri-testcase27res"))) alert("Test 27: Unexpected svTRID: " + eppResponse.getTransactionId().getServiceTransactionId());
				if (! (eppResponseData.getINumber()).equals("=!1002.2222.3333.4444")) alert("Test 27: Unexpected inumber in response data: " + eppResponseData.getINumber());
				if (! (xmlDateFormat.format(eppResponseData.getDateExpired().getTime()).equals("2011-05-03T22:00:00.0Z"))) alert ("Test 27: Unexpected exDate in response data: " + eppResponseData.getDateExpired());
			}
		} catch (Exception ex) {

			ex.printStackTrace(System.err);
			alert("Test 27: Exception: " + ex.getMessage());
		}

		// Test 28: Create I-Name

		log("Starting Test 28");

		try {

			EppXriName eppXriName = new EppXriName("=th.2.test-iname");
			eppXriName.setAuthorityId("=!(!!1001!123E!1235.1234.1241)");

			EppCommandCreate eppCommandCreate = EppCommand.create(eppXriName, "xri-testcase28cmd");

			EppResponse eppResponse = eppChannel.send(eppCommandCreate);
			EppResult eppResult = (EppResult) eppResponse.getResult().get(0);
			EppResponseDataCreateXriName eppResponseData = (EppResponseDataCreateXriName) eppResponse.getResponseData();

			dumpCommand(28, eppCommandCreate);
			dumpResponse(28, eppResponse);

			if (doChecks) {

				if (! (eppResponse.success())) alert("Test 28: Unsuccessful");
				if (! (eppResult.getCode() == 1000)) alert("Test 28: Unexpected result code: " + eppResult.getCode());
				if (! (eppResult.getMessage().getMessage().equals("Command completed successfully"))) alert("Test 28: Unexpected msg: " + eppResult.getMessage().getMessage());
				if (! (eppResponse.getTransactionId().getClientTransactionId().equals("xri-testcase28cmd"))) alert("Test 28: Unexpected clTRID: " + eppResponse.getTransactionId().getClientTransactionId());
				if (! (eppResponse.getTransactionId().getServiceTransactionId().equals("xri-testcase28res"))) alert("Test 28: Unexpected svTRID: " + eppResponse.getTransactionId().getServiceTransactionId());
				if (! (eppResponseData.getIName().equals("=th.2.test-iname"))) alert("Test 28: Unexpected iname in response data: " + eppResponseData.getIName());
				if (! (xmlDateFormat.format(eppResponseData.getDateCreated().getTime()).equals("2005-09-13T13:43:17.0Z"))) alert ("Test 28: Unexpected crDate in response data: " + eppResponseData.getDateCreated());
				if (! (xmlDateFormat.format(eppResponseData.getDateExpired().getTime()).equals("2006-09-12T23:59:59.0Z"))) alert ("Test 28: Unexpected exDate in response data: " + eppResponseData.getDateExpired());
			}
		} catch (Exception ex) {

			ex.printStackTrace(System.err);
			alert("Test 28: Exception: " + ex.getMessage());
		}

		// Test 29: Check I-Name (I-Name Known)

		log("Starting Test 29");

		try {

			EppCommandCheck eppCommandCheck = EppCommand.check(EppObject.XRI_INAME, "xri-testcase29cmd");
			eppCommandCheck.add("=th.46.test-ina%7Dme");

			EppResponse eppResponse = eppChannel.send(eppCommandCheck);
			EppResult eppResult = (EppResult) eppResponse.getResult().get(0);
			EppResponseDataCheckXriName eppResponseData = (EppResponseDataCheckXriName) eppResponse.getResponseData();

			dumpCommand(29, eppCommandCheck);
			dumpResponse(29, eppResponse);

			if (doChecks) {

				if (! (eppResponse.success())) alert("Test 29: Unsuccessful");
				if (! (eppResult.getCode() == 1000)) alert("Test 29: Unexpected result code: " + eppResult.getCode());
				if (! (eppResult.getMessage().getMessage().equals("Command completed successfully"))) alert("Test 29: Unexpected msg: " + eppResult.getMessage().getMessage());
				if (! (eppResponse.getTransactionId().getClientTransactionId().equals("xri-testcase29cmd"))) alert("Test 29: Unexpected clTRID: " + eppResponse.getTransactionId().getClientTransactionId());
				if (! (eppResponse.getTransactionId().getServiceTransactionId().equals("xri-testcase29res"))) alert("Test 29: Unexpected svTRID: " + eppResponse.getTransactionId().getServiceTransactionId());
				if (! (eppResponseData.getHashMap().containsKey("=th.46.test-ina%7dme"))) alert("Test 29: =th.46.test-ina%7dme not found in response data");
				if (eppResponseData.isAvailable("=th.46.test-ina%7dme")) alert("Test 29: =th.46.test-ina%7dme is available, but should not be");
			}
		} catch (Exception ex) {

			ex.printStackTrace(System.err);
			alert("Test 29: Exception: " + ex.getMessage());
		}

		// Test 30: Check I-Name (I-Name Known)

		log("Starting Test 30");

		try {

			EppCommandCheck eppCommandCheck = EppCommand.check(EppObject.XRI_INAME, "xri-testcase30cmd");
			eppCommandCheck.add("=th1.999.test-iname");

			EppResponse eppResponse = eppChannel.send(eppCommandCheck);
			EppResult eppResult = (EppResult) eppResponse.getResult().get(0);
			EppResponseDataCheckXriName eppResponseData = (EppResponseDataCheckXriName) eppResponse.getResponseData();

			dumpCommand(30, eppCommandCheck);
			dumpResponse(30, eppResponse);

			if (doChecks) {

				if (! (eppResponse.success())) alert("Test 30: Unsuccessful");
				if (! (eppResult.getCode() == 1000)) alert("Test 30: Unexpected result code: " + eppResult.getCode());
				if (! (eppResult.getMessage().getMessage().equals("Command completed successfully"))) alert("Test 30: Unexpected msg: " + eppResult.getMessage().getMessage());
				if (! (eppResponse.getTransactionId().getClientTransactionId().equals("xri-testcase30cmd"))) alert("Test 30: Unexpected clTRID: " + eppResponse.getTransactionId().getClientTransactionId());
				if (! (eppResponse.getTransactionId().getServiceTransactionId().equals("xri-testcase30res"))) alert("Test 30: Unexpected svTRID: " + eppResponse.getTransactionId().getServiceTransactionId());
				if (! (eppResponseData.getHashMap().containsKey("=th1.999.test-iname"))) alert("Test 30: =th1.999.test-iname not found in response data");
				if (! (eppResponseData.isAvailable("=th1.999.test-iname"))) alert("Test 30: =th1.999.test-iname is not available, but should be");
			}
		} catch (Exception ex) {

			ex.printStackTrace(System.err);
			alert("Test 30: Exception: " + ex.getMessage());
		}

		// Test 31: <info> Query I-Name

		log("Starting Test 31");

		try {

			EppCommandInfo eppCommandInfo = EppCommand.info(EppObject.XRI_INAME, "=th.146.test-ina:me", "xri-testcase31cmd");

			EppResponse eppResponse = eppChannel.send(eppCommandInfo);
			EppResult eppResult = (EppResult) eppResponse.getResult().get(0);
			EppResponseDataInfo eppResponseData = (EppResponseDataInfo) eppResponse.getResponseData();
			EppXriName eppXriName = eppResponseData == null ? null : (EppXriName) eppResponseData.getObject();
			EppStatus eppStatus = eppXriName == null ? null : (EppStatus) eppXriName.getStatus().get(0);

			dumpCommand(31, eppCommandInfo);
			dumpResponse(31, eppResponse);

			if (doChecks) {

				if (! (eppResponse.success())) alert("Test 31: Unsuccessful");
				if (! (eppResult.getCode() == 1000)) alert("Test 31: Unexpected result code: " + eppResult.getCode());
				if (! (eppResult.getMessage().getMessage().equals("Command completed successfully"))) alert("Test 31: Unexpected msg: " + eppResult.getMessage().getMessage());
				if (! (eppResponse.getTransactionId().getClientTransactionId().equals("xri-testcase31cmd"))) alert("Test 31: Unexpected clTRID: " + eppResponse.getTransactionId().getClientTransactionId());
				if (! (eppResponse.getTransactionId().getServiceTransactionId().equals("xri-testcase31res"))) alert("Test 31: Unexpected svTRID: " + eppResponse.getTransactionId().getServiceTransactionId());
				if (! (eppXriName.getIName().equals("=th.146.test-ina:me"))) alert("Test 31: Unexpected iname in response data: " + eppXriName.getIName());
				if (! (eppXriName.getRoid().equals("NAM399-PER"))) alert ("Test 31: Unexpected roid in response data: " + eppXriName.getRoid());
				if (! (eppStatus.getStatus().equals("ok"))) alert ("Test 31: Unexpected status in response data: " + eppStatus.getStatus());
				if (! (eppXriName.getAuthorityId().equals("=!(!!1001!123E!1235.1234.1241)"))) alert ("Test 31: Unexpected authId in response data: " + eppXriName.getAuthorityId());
				if (! (eppXriName.getClientId().equals("NEUSTAR"))) alert ("Test 31: Unexpected clID in response data: " + eppXriName.getClientId());
				if (! (eppXriName.getClientIdCreated().equals("NEUSTAR"))) alert ("Test 31: Unexpected crID in response data: " + eppXriName.getClientIdCreated());
				if (! (xmlDateFormat.format(eppXriName.getDateCreated().getTime()).equals("2005-09-13T15:00:05.0Z"))) alert ("Test 31: Unexpected crDate in response data: " + eppXriName.getDateCreated());
				if (! (xmlDateFormat.format(eppXriName.getDateExpired().getTime()).equals("2006-09-12T23:59:59.0Z"))) alert ("Test 31: Unexpected exDate in response data: " + eppXriName.getDateExpired());
			}
		} catch (Exception ex) {

			ex.printStackTrace(System.err);
			alert("Test 31: Exception: " + ex.getMessage());
		}

		// Test 32: Transfer I-Name

		log("Starting Test 32");

		try {

			EppAuthInfo eppAuthInfoTarget = new EppAuthInfo(EppAuthInfo.TYPE_PW, "password2");

			EppPeriod eppPeriod = new EppPeriod(2, EppPeriod.UNIT_YEAR);

			EppCommandTransferXriName eppCommandTransfer = (EppCommandTransferXriName) EppCommand.transfer(EppObject.XRI_INAME, "=th.146.test-ina:me", "xri-testcase32cmd");
			eppCommandTransfer.setOperation("request");
			eppCommandTransfer.setTarget("=!(!!1001!123E!1235.1234.1241)", eppAuthInfoTarget);
			eppCommandTransfer.setPeriod(eppPeriod);

			EppResponse eppResponse = eppChannel.send(eppCommandTransfer);
			EppResult eppResult = (EppResult) eppResponse.getResult().get(0);
			EppResponseDataTransferXriName eppResponseData = (EppResponseDataTransferXriName) eppResponse.getResponseData();

			dumpCommand(32, eppCommandTransfer);
			dumpResponse(32, eppResponse);

			if (doChecks) {

				if (! (eppResponse.success())) alert("Test 32: Unsuccessful");
				if (! (eppResult.getCode() == 1001)) alert("Test 32: Unexpected result code: " + eppResult.getCode());
				if (! (eppResult.getMessage().getMessage().equals("Command completed successfully; action pending"))) alert("Test 32: Unexpected msg: " + eppResult.getMessage().getMessage());
				if (! (eppResponse.getTransactionId().getClientTransactionId().equals("xri-testcase32cmd"))) alert("Test 32: Unexpected clTRID: " + eppResponse.getTransactionId().getClientTransactionId());
				if (! (eppResponse.getTransactionId().getServiceTransactionId().equals("xri-testcase32res"))) alert("Test 32: Unexpected svTRID: " + eppResponse.getTransactionId().getServiceTransactionId());
				if (! (eppResponseData.getIName().equals("=th.146.test-ina:me"))) alert("Test 32: Unexpected iname in response data: " + eppResponseData.getIName());
				if (! (eppResponseData.getSourceAuthorityId().equals("=!(!!1001!123E!1235.1234.1241)"))) alert("Test 32: Unexpected source in response data: " + eppResponseData.getSourceAuthorityId());
				if (! (eppResponseData.getTargetAuthorityId().equals("=!(!!1001!123E!1235.1234.1242)"))) alert("Test 32: Unexpected target in response data: " + eppResponseData.getSourceAuthorityId());
				if (! (eppResponseData.getTransferToken().equals("OneTimePass"))) alert("Test 32: Unexpected trToken in response data: " + eppResponseData.getTransferToken());
				if (! (eppResponseData.getTransferStatus().equals("pending"))) alert("Test 32: Unexpected trStatus in response data: " + eppResponseData.getTransferStatus());
				if (! (eppResponseData.getRequestClientId().equals("ClientX"))) alert("Test 32: Unexpected reID in response data: " + eppResponseData.getRequestClientId());
				if (! (xmlDateFormat.format(eppResponseData.getRequestDate().getTime()).equals("2005-05-06T22:00:00.0Z"))) alert ("Test 32: Unexpected reDate in response data: " + eppResponseData.getRequestDate());
				if (! (eppResponseData.getActionClientId().equals("ClientY"))) alert("Test 32: Unexpected acID in response data: " + eppResponseData.getActionClientId());
				if (! (xmlDateFormat.format(eppResponseData.getActionDate().getTime()).equals("2005-05-11T22:00:00.0Z"))) alert ("Test 32: Unexpected acDate in response data: " + eppResponseData.getActionDate());
				if (! (xmlDateFormat.format(eppResponseData.getDateExpired().getTime()).equals("2009-05-03T22:00:00.0Z"))) alert ("Test 32: Unexpected exDate in response data: " + eppResponseData.getDateExpired());
			}
		} catch (Exception ex) {

			ex.printStackTrace(System.err);
			alert("Test 32: Exception: " + ex.getMessage());
		}

		// Test 33: Query I-Name Transfer Status

		log("Starting Test 33");

		try {

			EppAuthInfo eppAuthInfo = new EppAuthInfo(EppAuthInfo.TYPE_PW, "password2");

			EppCommandTransferXriName eppCommandTransfer = (EppCommandTransferXriName) EppCommand.transfer(EppObject.XRI_INAME, "=th.146.test-ina:me", "xri-testcase33cmd");
			eppCommandTransfer.setOperation("query");
			eppCommandTransfer.setAuthInfo(eppAuthInfo);

			EppResponse eppResponse = eppChannel.send(eppCommandTransfer);
			EppResult eppResult = (EppResult) eppResponse.getResult().get(0);
			EppResponseDataTransferXriName eppResponseData = (EppResponseDataTransferXriName) eppResponse.getResponseData();

			dumpCommand(33, eppCommandTransfer);
			dumpResponse(33, eppResponse);

			if (doChecks) {

				if (! (eppResponse.success())) alert("Test 33: Unsuccessful");
				if (! (eppResult.getCode() == 1000)) alert("Test 33: Unexpected result code: " + eppResult.getCode());
				if (! (eppResult.getMessage().getMessage().equals("Command completed successfully"))) alert("Test 33: Unexpected msg: " + eppResult.getMessage().getMessage());
				if (! (eppResponse.getTransactionId().getClientTransactionId().equals("xri-testcase33cmd"))) alert("Test 33: Unexpected clTRID: " + eppResponse.getTransactionId().getClientTransactionId());
				if (! (eppResponse.getTransactionId().getServiceTransactionId().equals("xri-testcase33res"))) alert("Test 33: Unexpected svTRID: " + eppResponse.getTransactionId().getServiceTransactionId());
				if (! (eppResponseData.getIName().equals("=th.146.test-ina:me"))) alert("Test 33: Unexpected iname in response data: " + eppResponseData.getIName());
				if (! (eppResponseData.getSourceAuthorityId().equals("=!(!!1001!123E!1235.1234.1241)"))) alert("Test 33: Unexpected source in response data: " + eppResponseData.getSourceAuthorityId());
				if (! (eppResponseData.getTargetAuthorityId().equals("=!(!!1001!123E!1235.1234.1241)"))) alert("Test 33: Unexpected target in response data: " + eppResponseData.getSourceAuthorityId());
				if (! (eppResponseData.getTransferToken().equals("OneTimePass"))) alert("Test 33: Unexpected trToken in response data: " + eppResponseData.getTransferToken());
				if (! (eppResponseData.getTransferStatus().equals("pending"))) alert("Test 33: Unexpected trStatus in response data: " + eppResponseData.getTransferStatus());
				if (! (eppResponseData.getRequestClientId().equals("ClientX"))) alert("Test 33: Unexpected reID in response data: " + eppResponseData.getRequestClientId());
				if (! (xmlDateFormat.format(eppResponseData.getRequestDate().getTime()).equals("2005-05-06T22:00:00.0Z"))) alert ("Test 33: Unexpected reDate in response data: " + eppResponseData.getRequestDate());
				if (! (eppResponseData.getActionClientId().equals("ClientY"))) alert("Test 33: Unexpected acID in response data: " + eppResponseData.getActionClientId());
				if (! (xmlDateFormat.format(eppResponseData.getActionDate().getTime()).equals("2005-05-11T22:00:00.0Z"))) alert ("Test 33: Unexpected acDate in response data: " + eppResponseData.getActionDate());
				if (! (xmlDateFormat.format(eppResponseData.getDateExpired().getTime()).equals("2009-05-03T22:00:00.0Z"))) alert ("Test 33: Unexpected exDate in response data: " + eppResponseData.getDateExpired());
			}
		} catch (Exception ex) {

			ex.printStackTrace(System.err);
			alert("Test 33: Exception: " + ex.getMessage());
		}

		// Test 34: Update I-Name Status

		log("Starting Test 34");

		try {

			EppStatus eppStatus = new EppStatus("clientTransferProhibited");

			EppCommandUpdateXriName eppCommandUpdate = (EppCommandUpdateXriName) EppCommand.update(EppObject.XRI_INAME, "=th.2.test-iname", "xri-testcase34cmd");
			eppCommandUpdate.addStatus(eppStatus);

			EppResponse eppResponse = eppChannel.send(eppCommandUpdate);
			EppResult eppResult = (EppResult) eppResponse.getResult().get(0);

			dumpCommand(34, eppCommandUpdate);
			dumpResponse(34, eppResponse);

			if (doChecks) {

				if (! (eppResponse.success())) alert("Test 34: Unsuccessful");
				if (! (eppResult.getCode() == 1000)) alert("Test 34: Unexpected result code: " + eppResult.getCode());
				if (! (eppResult.getMessage().getMessage().equals("Command completed successfully"))) alert("Test 34: Unexpected msg: " + eppResult.getMessage().getMessage());
				if (! (eppResponse.getTransactionId().getClientTransactionId().equals("xri-testcase34cmd"))) alert("Test 34: Unexpected clTRID: " + eppResponse.getTransactionId().getClientTransactionId());
				if (! (eppResponse.getTransactionId().getServiceTransactionId().equals("xri-testcase34res"))) alert("Test 34: Unexpected svTRID: " + eppResponse.getTransactionId().getServiceTransactionId());
			}
		} catch (Exception ex) {

			ex.printStackTrace(System.err);
			alert("Test 34: Exception: " + ex.getMessage());
		}

		// Test 35: Renew I-Name

		log("Starting Test 35");

		try {

			Calendar curExpDate = Calendar.getInstance();
			curExpDate.setTime(xmlShortDateFormat.parse("2006-05-03"));

			EppPeriod eppPeriod = new EppPeriod(5, EppPeriod.UNIT_YEAR);

			EppCommandRenewXriName eppCommandRenew = (EppCommandRenewXriName) EppCommand.renew(EppObject.XRI_INAME, "=th.2.test-iname", "xri-testcase35cmd");
			eppCommandRenew.setCurrentExpireDate(curExpDate);
			eppCommandRenew.setPeriod(eppPeriod);

			EppResponse eppResponse = eppChannel.send(eppCommandRenew);
			EppResult eppResult = (EppResult) eppResponse.getResult().get(0);
			EppResponseDataRenewXriName eppResponseData = (EppResponseDataRenewXriName) eppResponse.getResponseData();

			dumpCommand(35, eppCommandRenew);
			dumpResponse(35, eppResponse);

			if (doChecks) {

				if (! (eppResponse.success())) alert("Test 35: Unsuccessful");
				if (! (eppResult.getCode() == 1000)) alert("Test 35: Unexpected result code: " + eppResult.getCode());
				if (! (eppResult.getMessage().getMessage().equals("Command completed successfully"))) alert("Test 35: Unexpected msg: " + eppResult.getMessage().getMessage());
				if (! (eppResponse.getTransactionId().getClientTransactionId().equals("xri-testcase35cmd"))) alert("Test 35: Unexpected clTRID: " + eppResponse.getTransactionId().getClientTransactionId());
				if (! (eppResponse.getTransactionId().getServiceTransactionId().equals("xri-testcase35res"))) alert("Test 35: Unexpected svTRID: " + eppResponse.getTransactionId().getServiceTransactionId());
				if (! (eppResponseData.getIName()).equals("=th.2.test-iname")) alert("Test 35: Unexpected iname in response data: " + eppResponseData.getIName());
				if (! (xmlDateFormat.format(eppResponseData.getDateExpired().getTime()).equals("2011-05-03T22:00:00.0Z"))) alert ("Test 35: Unexpected exDate in response data: " + eppResponseData.getDateExpired());
			}
		} catch (Exception ex) {

			ex.printStackTrace(System.err);
			alert("Test 35: Exception: " + ex.getMessage());
		}

		// Test 36: Delete I-Name

		log("Starting Test 36");

		try {

			EppCommandDelete eppCommandDelete = EppCommand.delete(EppObject.XRI_INAME, "=th.2.test-iname", "xri-testcase36cmd");

			EppResponse eppResponse = eppChannel.send(eppCommandDelete);
			EppResult eppResult = (EppResult) eppResponse.getResult().get(0);

			dumpCommand(36, eppCommandDelete);
			dumpResponse(36, eppResponse);

			if (doChecks) {

				if (! (eppResponse.success())) alert("Test 36: Unsuccessful");
				if (! (eppResult.getCode() == 1000)) alert("Test 36: Unexpected result code: " + eppResult.getCode());
				if (! (eppResult.getMessage().getMessage().equals("Command completed successfully"))) alert("Test 36: Unexpected msg: " + eppResult.getMessage().getMessage());
				if (! (eppResponse.getTransactionId().getClientTransactionId().equals("xri-testcase36cmd"))) alert("Test 36: Unexpected clTRID: " + eppResponse.getTransactionId().getClientTransactionId());
				if (! (eppResponse.getTransactionId().getServiceTransactionId().equals("xri-testcase36res"))) alert("Test 36: Unexpected svTRID: " + eppResponse.getTransactionId().getServiceTransactionId());
			}
		} catch (Exception ex) {

			ex.printStackTrace(System.err);
			alert("Test 36: Exception: " + ex.getMessage());
		}

		// Test 44: Logout

		log("Starting Test 44");

		try {

			EppResponse eppResponse = eppChannel.terminate();
			EppResult eppResult = (EppResult) eppResponse.getResult().get(0);

			dumpResponse(44, eppResponse);

			if (doChecks) {

				if (! (eppResponse.success())) alert("Test 44: Unsuccessful");
				if (! (eppResult.getCode() == 1500)) alert("Test 44: Unexpected result code: " + eppResult.getCode());
				if (! (eppResult.getMessage().getMessage().equals("Command completed successfully; ending session"))) alert("Test 44: Unexpected msg: " + eppResult.getMessage().getMessage());
				if (! (eppResponse.getTransactionId().getClientTransactionId().equals("xri-testcase44cmd"))) alert("Test 44: Unexpected clTRID: " + eppResponse.getTransactionId().getClientTransactionId());
				if (! (eppResponse.getTransactionId().getServiceTransactionId().equals("xri-testcase44res"))) alert("Test 44: Unexpected svTRID: " + eppResponse.getTransactionId().getServiceTransactionId());
			}
		} catch (Exception ex) {

			ex.printStackTrace(System.err);
			alert("Test 44: Exception: " + ex.getMessage());
		}

		// Done

		eppSession.close();

		log("All done");
	}
}
