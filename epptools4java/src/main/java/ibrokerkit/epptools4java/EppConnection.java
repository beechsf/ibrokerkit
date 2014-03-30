package ibrokerkit.epptools4java;

import ibrokerkit.epptools4java.events.EppEvent;
import ibrokerkit.epptools4java.events.EppEvents;
import ibrokerkit.epptools4java.store.Store;
import ibrokerkit.epptools4java.store.StoreException;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neulevel.epp.core.EppError;
import com.neulevel.epp.core.EppGreeting;
import com.neulevel.epp.core.command.EppCommand;
import com.neulevel.epp.core.command.EppCommandLogin;
import com.neulevel.epp.core.command.EppCreds;
import com.neulevel.epp.core.response.EppResponse;
import com.neulevel.epp.core.response.EppResult;
import com.neulevel.epp.transport.EppChannel;
import com.neulevel.epp.transport.EppSession;
import com.neulevel.epp.transport.tcp.EppSessionTcp;

public class EppConnection {

	private static final String DEFAULT_NUM_SESSIONS = "5";

	private static final Logger log = LoggerFactory.getLogger(EppConnection.class.getName());

	private char gcs;
	private String eppHost;
	private Integer eppPort;
	private Properties properties;
	private Store store;
	private EppTransactionIdGenerator eppTransactionIdGenerator;
	private EppEvents eppEvents;

	private String eppUsername;
	private String eppPassword;
	private int numSessions;
	private boolean useTls;

	private EppSession[] eppSession;
	private EppChannel[] eppChannel;
	private Boolean[] eppBlocked;

	public EppConnection(char gcs, String eppHost, Integer eppPort, Properties properties, Store store, EppTransactionIdGenerator eppTransactionIdGenerator, EppEvents eppEvents) {

		this.gcs = gcs;
		this.eppHost = eppHost;
		this.eppPort = eppPort;
		this.properties = properties;
		this.store = store;
		this.eppTransactionIdGenerator = eppTransactionIdGenerator;
		this.eppEvents = eppEvents;

		this.eppUsername = this.properties.getProperty("epp-username");
		this.eppPassword = this.properties.getProperty("epp-password");
		this.numSessions = Integer.parseInt(properties.getProperty("num-sessions", DEFAULT_NUM_SESSIONS));
		this.useTls = Boolean.parseBoolean(this.properties.getProperty("epp-usetls"));

		this.eppSession = new EppSession[this.numSessions];
		this.eppChannel = new EppChannel[this.numSessions];
		this.eppBlocked = new Boolean[this.numSessions];
	}

	public synchronized void init() {

		// init the first EPP session. additional sessions can be opened later.

		try {

			this.beginSession(null, 0);
		} catch (Exception ex) {

			log.warn("Cannot initialize " + this.gcs + " session 0.");
		}
	}

	public void close() {

		for (int i=0; i<this.eppSession.length; i++) {

			this.endSession(i);
		}
	}

	/**
	 * Start the session and log in.
	 */
	private synchronized void beginSession(String newPassword, int i) throws Exception {

		if (this.eppSession == null) this.eppSession = new EppSession[this.numSessions];
		if (this.eppChannel == null) this.eppChannel = new EppChannel[this.numSessions];

		// end session first if it's open

		if (this.eppChannel[i] != null || this.eppSession[i] != null) this.endSession(i);

		// open session and channel

		EppGreeting eppGreeting;

		try {

			if (this.useTls) {

				this.eppSession[i] = new EppSessionTcp();
				this.eppSession[i].init(this.properties);
			} else {

				this.eppSession[i] = new EppSessionTcp(false);
			}

			log.info("{" + this.gcs + " " + i + "} Trying to connect to " + this.eppHost + ":" + this.eppPort + " for " + this.gcs + " services.");

			eppGreeting = this.eppSession[i].connect(this.eppHost, this.eppPort);
			if (eppGreeting == null) throw new EppToolsException("{" + this.gcs + " " + i + "} No greeting on connect: " + this.eppSession[i].getException().getMessage(), this.eppSession[i].getException());

			this.eppChannel[i] = this.eppSession[i].getChannel();
		} catch (Exception ex) {

			this.eppSession[i] = null;
			this.eppChannel[i] = null;
			log.error(ex.getMessage(), ex);
			throw ex;
		}

		// log in

		try {

			EppCommandLogin eppCommandLogin = new EppCommandLogin(eppGreeting.getServiceMenu());
			eppCommandLogin.setClientTransactionId(this.eppTransactionIdGenerator.generateTransactionId());
			eppCommandLogin.setCreds(new EppCreds(this.eppUsername, this.eppPassword));
			if (newPassword != null) eppCommandLogin.setNewPassword(newPassword);

			EppResponse eppResponse = this.eppChannel[i].start(eppCommandLogin);
			EppResult eppResult = (EppResult) eppResponse.getResult().get(0);

			if (eppResult == null) throw new EppToolsException("No result");
			if (! (eppResponse.success())) throw EppToolsException.makeEppToolsUnsuccessfulException(eppResult);
			if (eppResponse.getTransactionId() == null || eppResponse.getTransactionId().getClientTransactionId() == null || ! (eppResponse.getTransactionId().getClientTransactionId().equals(this.eppTransactionIdGenerator.getLastTransactionId()))) throw new EppToolsException("Unexpected clTRID: " + eppResponse.getTransactionId().getClientTransactionId());
		} catch (Exception ex) {

			this.eppSession = null;
			this.eppChannel = null;
			log.error(ex.getMessage(), ex);
			throw ex;
		}
	}

	/**
	 * End the session.
	 */
	private synchronized void endSession(int i) {

		// shut down channel and session

		/*		log.debug("{=" + " " + i + "} Terminating channel to = server.");

		if (this.eppChannelEqual[i] != null) {

			this.eppChannelEqual[i].terminate();
		}*/

		log.debug("{=" + " " + i + "} Closing session to = server.");

		if (this.eppSession[i] != null) {

			try {

				if (((EppSessionTcp) this.eppSession[i]).getSocket() != null) ((EppSessionTcp) this.eppSession[i]).getSocket().close();
			} catch (IOException ex) { }
		}

		this.eppSession[i] = null;
		this.eppChannel[i] = null;

		log.debug("{=" + " " + i + "} Session ended to = server.");
	}

	/**
	 * Change the password
	 * @param newPassword
	 */
	public synchronized void changePassword(String newPassword) throws EppToolsException {

		try {

			this.endSession(0);
			this.beginSession(newPassword, 0);
		} catch (Exception ex) {

			throw new EppToolsException(ex.getMessage(), ex);
		}
	}

	/**
	 * Check if the session and channel are alive.
	 * If no, reconnect.
	 */
	public EppChannel checkChannel(int i) throws EppToolsException {

		EppChannel eppChannel = this.eppChannel[i];

		synchronized (eppChannel) {

			try {

				EppGreeting eppGreeting = eppChannel.hello();
				if (eppGreeting == null) throw new NullPointerException();
			} catch (Exception ex) {

				log.warn("{" + this.gcs + " " + i + "} Channel to = server seems to have gone away: " + ex.getMessage() + " -> Trying to restore.");

				try {

					this.endSession(i);
					this.beginSession(null, i);
				} catch (Exception ex2) {

					log.error(ex2.getMessage(), ex2);
					throw new EppToolsException("{" + this.gcs + " " + i + "} Cannot restore channel: " + ex.getMessage(), ex);
				}

				log.info("{" + this.gcs + " " + i + "} Successfully restored channel after: " + ex.getMessage());
			}

			return eppChannel;
		}
	}

	/**
	 * Sends an EPP command and returns the EPP response.
	 * Everything is logged to our action store.
	 */
	public EppResponse send(EppCommand eppCommand, int i) throws EppToolsException {

		// timestamp

		Date beginTimestamp = new Date();

		// make sure our session and channel are still alive

		EppChannel eppChannel = null;

		try {

			if (this.eppChannel == null || this.eppChannel.length <= i || this.eppChannel[i] == null) {

				this.beginSession(null, i);
			}

			eppChannel = this.eppChannel[i];

			if (eppChannel == null) throw new IOException("{" + this.gcs + " " + i + "} No channel.");
		} catch (EppToolsException ex) {

			throw ex;
		} catch (Exception ex) {

			throw new EppToolsException("Cannot initialize channel " + i + ": " + ex.getMessage(), ex);
		}

		// try to send the command and read the response

		EppResponse eppResponse = null;
		EppResult eppResult = null;

		synchronized (eppChannel) {

			log.debug("{" + this.gcs + " " + i + "} Blocking channel.");
			this.eppBlocked[i] = Boolean.TRUE;

			try {

				log.debug("{" + this.gcs + " " + i + "} Attempting to send transaction " + eppCommand.getClientTransactionId() + " to " + this.gcs + " server.");

				eppResponse = eppChannel.send(eppCommand);
				if (eppChannel.getException() != null) throw eppChannel.getException();
				if (eppResponse == null) throw new IOException("{" + this.gcs + " " + i + "} No response.");

				eppResult = (EppResult) eppResponse.getResult().get(0);
				if (eppResult == null) throw new IOException("{" + this.gcs + " " + i + "} No result.");

				if (eppResult.getCode() == EppError.CODE_SESSION_LIMIT_EXCEEDED_SERVER_CLOSING_CONNECTION) throw new IOException("{" + this.gcs + " " + i + "} Session limit exceeded.");

				log.debug("{" + this.gcs + " " + i + "} Transaction " + eppCommand.getClientTransactionId() + " sent to " + this.gcs + " server.");
			} catch (Exception ex) {

				log.warn("{" + this.gcs + " " + i + "} Channel to " + this.gcs + " server seems to have gone away: " + ex.getMessage(), ex);

				// if there's just a problem with the socket, we try to restore and send the command again

				if (ex instanceof IOException) {

					log.debug("{" + this.gcs + " " + i + "} Trying to restore channel to " + this.gcs + " server.");

					// try to restore the channel

					try {

						this.endSession(i);
						this.beginSession(null, i);
						eppChannel = this.eppChannel[i];

						if (eppChannel == null) throw new IOException("{" + this.gcs + " " + i + "} Channel has gone away.");
					} catch (Exception ex2) {

						log.error("{" + this.gcs + " " + i + "} Cannot restore channel: " + ex2.getMessage(), ex2);
						ex = ex2;
						eppChannel = null;
					}

					// after restoring the channel, try to re-send the command

					if (eppChannel != null) {

						try {

							log.debug("{" + this.gcs + " " + i + "} Trying to re-send transaction " + eppCommand.getClientTransactionId() + " to " + this.gcs + " server."); 

							eppResponse = eppChannel.send(eppCommand);
							if (eppChannel.getException() != null) throw eppChannel.getException();
							if (eppResponse == null) throw new IOException("{" + this.gcs + " " + i + "} No response.");

							eppResult = (EppResult) eppResponse.getResult().get(0);
							if (eppResult == null) throw new IOException("{" + this.gcs + " " + i + "} No result.");

							if (eppResult.getCode() == EppError.CODE_SESSION_LIMIT_EXCEEDED_SERVER_CLOSING_CONNECTION) throw new IOException("{" + this.gcs + " " + i + "} Session limit exceeded.");

							log.debug("{" + this.gcs + " " + i + "} Transaction " + eppCommand.getClientTransactionId() + " sent to " + this.gcs + " server.");

							ex = null;
						} catch (Exception ex2) {

							log.warn("{" + this.gcs + " " + i + "} Still cannot send transaction " + eppCommand.getClientTransactionId() + " to " + this.gcs + " server: " + ex2.getMessage(), ex2);

							ex = ex2;
						}
					}
				}

				// if we couldn't handle the exception, we log and throw it

				if (ex != null)  {

					log.error("{" + this.gcs + " " + i + "} Failed to send transaction " + eppCommand.getClientTransactionId() + " to " + this.gcs + " server: " + ex.getMessage(), ex);

					// log the failed action

					try {

						this.store.createAction(Character.valueOf(this.gcs), eppCommand.getClientTransactionId(), eppCommand.toString(), ex.getMessage());
					} catch (StoreException ex2) {

						log.error("{" + this.gcs + " " + i + "} Cannot store failed EPP action: " + ex2.getMessage(), ex2);
					}

					log.debug("{" + this.gcs + " " + i + "} Unblocking channel.");
					this.eppBlocked[i] = Boolean.FALSE;

					throw new EppToolsException("{" + this.gcs + " " + i + "} Cannot send transaction " + eppCommand.getClientTransactionId() + " to " + this.gcs + " server: " + ex.getMessage(), ex);
				}
			}

			log.debug("{" + this.gcs + " " + i + "} Unblocking channel.");
			this.eppBlocked[i] = Boolean.FALSE;
		}

		// log the successful action

		try {

			this.store.createAction(Character.valueOf(this.gcs), eppCommand.getClientTransactionId(), eppCommand.toString(), eppResponse.toString());
		} catch (StoreException ex) {

			log.error("{" + this.gcs + " " + i + "} Cannot store successful EPP action:" + ex.getMessage(), ex);
		}

		// timestamp

		Date endTimestamp = new Date();

		// event

		EppEvent eppEvent = new EppEvent(this, Character.valueOf(this.gcs), beginTimestamp, endTimestamp, eppChannel, eppCommand, eppResponse);

		this.eppEvents.fireEppEvent(eppEvent);

		// check the EPP response

		if (! (eppResponse.success())) throw EppToolsException.makeEppToolsUnsuccessfulException(eppResult);
		if (eppResponse.getTransactionId() == null || eppResponse.getTransactionId().getClientTransactionId() == null || ! (eppResponse.getTransactionId().getClientTransactionId().equals(eppCommand.getClientTransactionId()))) throw new EppToolsException("Unexpected clTRID: " + eppResponse.getTransactionId().getClientTransactionId() + " (expected " + eppCommand.getClientTransactionId() + ")");

		log.info("{" + this.gcs + " " + i + "} Successfully completed transaction " + eppCommand.getClientTransactionId() + " with " + this.gcs + " server.");

		return(eppResponse);
	}

	/**
	 * Sends an EPP command and returns the EPP response.
	 * Everything is logged to our action store.
	 */
	public EppResponse send(EppCommand eppCommand) throws EppToolsException {

		int i = 0;

		while (this.eppBlocked[i] != null && this.eppBlocked[i].equals(Boolean.TRUE) && i < this.numSessions) i++;

		if (i == this.numSessions) throw new EppToolsException("All channels to " + this.gcs + " registry are blocked. Please try again later.");

		log.debug("Sending to " + this.gcs + " channel " + i);

		return(this.send(eppCommand, i));
	}

	/*
	 * Object methods
	 */

	public String toString() {

		return "" + this.gcs + " " + this.eppHost + ":" + this.eppPort;
	}
}
