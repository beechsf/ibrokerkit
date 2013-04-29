package ibrokerkit.epptools4java;

import java.util.EventObject;

import com.neulevel.epp.core.command.EppCommand;
import com.neulevel.epp.core.response.EppResponse;

public class EppEvent extends EventObject {

	private static final long serialVersionUID = 5301716219045375638L;

	private Character cs;
	private String transactionId;
	private EppCommand eppCommand;
	private EppResponse eppResponse;

	public EppEvent(Object source, Character cs, String transactionId, EppCommand eppCommand, EppResponse eppResponse) {

		super(source);

		this.cs = cs;
		this.transactionId = transactionId;
		this.eppCommand = eppCommand;
		this.eppResponse = eppResponse;
	}

	@Override
	public EppTools getSource() {

		return (EppTools) super.getSource();
	}

	public Character getCs() {

		return this.cs;
	}

	public void setCs(Character cs) {

		this.cs = cs;
	}

	public String getTransactionId() {

		return this.transactionId;
	}

	public void setTransactionId(String transactionId) {

		this.transactionId = transactionId;
	}

	public EppCommand getEppCommand() {

		return this.eppCommand;
	}

	public void setEppCommand(EppCommand eppCommand) {

		this.eppCommand = eppCommand;
	}

	public EppResponse getEppResponse() {

		return this.eppResponse;
	}

	public void setEppResponse(EppResponse eppResponse) {

		this.eppResponse = eppResponse;
	}
}
