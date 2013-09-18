package ibrokerkit.iservicefront.locator.webapplication;

import org.apache.wicket.application.IClassResolver;

public class LocatorClassResolver implements IClassResolver {

	public Class<?> resolveClass(final String classname) {

		try {

			return(Class.forName(classname));
		} catch (ClassNotFoundException ex) {
			
			throw new RuntimeException("Unable to load class with name: " + classname);
		}
	}
}
