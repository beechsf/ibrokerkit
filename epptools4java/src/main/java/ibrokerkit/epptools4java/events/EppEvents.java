package ibrokerkit.epptools4java.events;

import java.util.ArrayList;
import java.util.List;

public class EppEvents {

	private final List<EppListener> eppListeners;

	public EppEvents() {

		this.eppListeners = new ArrayList<EppListener> ();
	}

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
}
