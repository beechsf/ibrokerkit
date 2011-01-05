package ibrokerkit.iservicefront.components;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.resource.IStringResourceStream;

public abstract class MyVelocityContributor extends AbstractBehavior {

	private static final long serialVersionUID = -4251182453305948352L;

	private final IModel model;

	public MyVelocityContributor(IModel model) {

		super();

		this.model = model;
	}

	@Override
	public void renderHead(IHeaderResponse response) {

		Map<?, ?> map = (Map<?, ?>) this.model.getObject();
		IStringResourceStream stringResourceStream = this.getTemplateResource();
		String string = stringResourceStream.asString();
		Reader reader = new StringReader(string);
		StringWriter writer = new StringWriter();

		final VelocityContext velocityContext = new VelocityContext(map);

		try {

			Velocity.evaluate(velocityContext, writer, this.getClass().getName(), reader);
		} catch (Exception ex) {

			throw new RuntimeException(ex);
		}

		String result = writer.getBuffer().toString();

		StringBuffer buffer = new StringBuffer();
		Pattern hrefPattern = Pattern.compile("href=\"(.+)\"", Pattern.CASE_INSENSITIVE);
		Matcher matcher = hrefPattern.matcher(result);

		while (matcher.find()) {

			String location = matcher.group(1);
			matcher.appendReplacement(buffer, "href=\"" + returnRelativePath(location) + "\"");
		}

		matcher.appendTail(buffer);

		response.renderString(buffer.toString());
	}

	private static final String returnRelativePath(String location) {

		if (location.startsWith("http://") || location.startsWith("https://") || location.startsWith("/")) {

			return(location);
		} else {

			return(RequestCycle.get().getRequest().getRelativePathPrefixToContextRoot() + location);
		}
	}

	protected abstract IStringResourceStream getTemplateResource();
}
