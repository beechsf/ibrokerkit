package ibrokerkit.iservicefront.components;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.StringHeaderItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;

public abstract class MyVelocityContributor extends Behavior {

	private static final long serialVersionUID = -4251182453305948352L;

	private final IModel<HashMap<String, Object>> model;

	public MyVelocityContributor(IModel<HashMap<String, Object>> model) {

		super();

		this.model = model;
	}

	@Override
	public void renderHead(Component component, IHeaderResponse headerResponse) {

		super.renderHead(component, headerResponse);

		HashMap<String, Object> map = this.model.getObject();
		StringWriter writer = new StringWriter();

		final VelocityContext velocityContext = new VelocityContext(map);

		try {

			Reader templateReader = this.getTemplateReader();

			Velocity.setProperty("input.encoding", "UTF-8");
			Velocity.setProperty("output.encoding", "UTF-8");
			Velocity.evaluate(velocityContext, writer, this.getClass().getName(), templateReader);
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

		headerResponse.render(new StringHeaderItem(buffer.toString()));
	}

	private static final String returnRelativePath(String location) {

		if (location.startsWith("http://") || location.startsWith("https://") || location.startsWith("/")) {

			return(location);
		} else {

			return(RequestCycle.get().getRequest().getPrefixToContextPath() + location);
		}
	}

	protected abstract Reader getTemplateReader() throws IOException;
}
