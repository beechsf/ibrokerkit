package ibrokerkit.epptools4java.events;

import ibrokerkit.epptools4java.EppTools;

import java.util.Date;
import java.util.EventObject;

import com.neulevel.epp.core.command.EppCommand;
import com.neulevel.epp.core.response.EppResponse;
import com.neulevel.epp.transport.EppChannel;

public class EppEvent extends EventObject {

	private static final long serialVersionUID = 5301716219045375638L;

	private Character gcs;
	private Date beginTimestamp;
	private Date endTimestamp;
	private EppChannel eppChannel;
	private EppCommand eppCommand;
	private EppResponse eppResponse;

	public EppEvent(Object source, Character gcs, Date beginTimestamp, Date endTimestamp, EppChannel eppChannel, EppCommand eppCommand, EppResponse eppResponse) {

		super(source);

		this.gcs = gcs;
		this.beginTimestamp = beginTimestamp;
		this.endTimestamp = endTimestamp;
		this.eppChannel = eppChannel;
		this.eppCommand = eppCommand;
		this.eppResponse = eppResponse;
	}

	@Override
	public EppTools getSource() {

		return (EppTools) super.getSource();
	}

	public Character getGcs() {

		return this.gcs;
	}

	public Date getBeginTimestamp() {

		return this.beginTimestamp;
	}

	public Date getEndTimestamp() {

		return this.endTimestamp;
	}

	public EppChannel getEppChannel() {

		return this.eppChannel;
	}

	public EppCommand getEppCommand() {

		return this.eppCommand;
	}

	public EppResponse getEppResponse() {

		return this.eppResponse;
	}
}
