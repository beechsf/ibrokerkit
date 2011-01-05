package ibrokerkit.ibrokerfront.validators;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.StringValidator;
import org.openxri.XRISubSegment;

public class SubSegmentNamePatternValidator extends StringValidator {

	private static final long serialVersionUID = -4750976789242621069L;

	@Override
	public void onValidate(IValidatable validatable) {

		String value = (String) validatable.getValue();

		if (value == null) return;

		// check if we can construct an XRI subsegment
		
		try {

			new XRISubSegment('*' + value);
		} catch (Exception ex) {

			validatable.error(new ValidationError());
		}
	}
}
