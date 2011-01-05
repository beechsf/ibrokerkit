package ibrokerkit.ibrokerfront.validators;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.StringValidator;

public class URIPatternValidator extends StringValidator {

	private static final long serialVersionUID = 1926812545272003854L;

	private boolean requireScheme;

	public URIPatternValidator(boolean requireScheme) {

		this.requireScheme = requireScheme;
	}

	@Override
	public void onValidate(IValidatable validatable) {

		String value = (String) validatable.getValue();

		if (value == null) return;

		// check if we can construct a URI

		URI uri;

		try {

			uri = new URI(value);
		} catch (URISyntaxException ex) {

			validatable.error(new ValidationError());
			return;
		}

		// also require that the URI has a scheme (http://, etc.)

		if (this.requireScheme && (uri.getScheme() == null || uri.getScheme().equals(""))) {

			validatable.error(new ValidationError());
		}
	}
}
