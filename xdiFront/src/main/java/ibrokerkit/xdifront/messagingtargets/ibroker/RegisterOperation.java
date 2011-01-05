package ibrokerkit.xdifront.messagingtargets.ibroker;

import java.util.Iterator;

import org.eclipse.higgins.xdi4j.Predicate;
import org.eclipse.higgins.xdi4j.Subject;
import org.eclipse.higgins.xdi4j.addressing.Addressing;
import org.eclipse.higgins.xdi4j.constants.DictionaryConstants;
import org.eclipse.higgins.xdi4j.messaging.AddOperation;
import org.eclipse.higgins.xdi4j.messaging.Message;
import org.eclipse.higgins.xdi4j.xri3.impl.XRI3;
import org.eclipse.higgins.xdi4j.xri3.impl.XRI3Segment;

public class RegisterOperation extends AddOperation {

	private static final long serialVersionUID = 1977812329217770776L;

	protected RegisterOperation(Message message, Predicate predicate) {

		super(message, predicate);
	}

	public static boolean isValid(Predicate predicate) {

		if (! AddOperation.isValid(predicate)) return false;

		if (! predicate.containsInnerGraph()) return false;

		RegisterOperation registerOperation = new RegisterOperation(null, predicate);
		if (registerOperation.getIname() == null) return false;
		if (registerOperation.getPassword() == null) return false;
		if (registerOperation.getEmail() == null) return false;

		return true;
	}

	public static RegisterOperation fromPredicate(Predicate predicate) {

		if (! isValid(predicate)) return(null);

		return(new RegisterOperation(null, predicate));
	}

	private Subject getInameSubject() {

		for (Iterator<Subject> subjects = this.getOperationGraph().getSubjects(); subjects.hasNext(); ) {

			Subject subject = subjects.next();

			if (this.getMessage().getMessageEnvelope().getGraph().containsStatement(subject.getSubjectXri(), DictionaryConstants.XRI_IS_A, new XRI3Segment("=")) ||
					this.getMessage().getMessageEnvelope().getGraph().containsStatement(subject.getSubjectXri(), DictionaryConstants.XRI_IS_A, new XRI3Segment("@"))) {

				return subject;
			}
		}

		return null;
	}

	public XRI3Segment getIname() {

		Subject inameSubject = this.getInameSubject();
		if (inameSubject == null) return null;

		return inameSubject.getSubjectXri();
	}

	public String getPassword() {

		Subject inameSubject = this.getInameSubject();
		if (inameSubject == null) return null;

		return Addressing.findLiteralData(inameSubject, new XRI3("$password"));
	}

	public String getEmail() {

		Subject inameSubject = this.getInameSubject();
		if (inameSubject == null) return null;

		return Addressing.findLiteralData(inameSubject, new XRI3("+email"));
	}
}
