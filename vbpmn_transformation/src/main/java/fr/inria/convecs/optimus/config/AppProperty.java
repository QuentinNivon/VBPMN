/**
 * 
 */

package fr.inria.convecs.optimus.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author silverquick
 *
 */
public class AppProperty {

	private static final Logger logger = LoggerFactory.getLogger(AppProperty.class);

	private static AppProperty appProperty = null;

	private Properties properties = null;

	// private constructor
	private AppProperty() {
		try {
			// trying to load the properties
			properties = new Properties();
			InputStream in = AppProperty.class.getResourceAsStream("/transformation.properties");
			properties.load(in);
		} catch (IOException e) {
			logger.error("Unable to load the application properties", e);
			throw new RuntimeException("Unable to load the application properties", e);
		}
	}

	/**
	 * Get instance of properties
	 * @return
	 */
	// thread safe static sync
	public static synchronized AppProperty getInstance() {
		if (null == appProperty) {
			appProperty = new AppProperty();
		}

		return appProperty;
	}

	/**
	 * 
	 * @param propertyName
	 * @return
	 */
	public String getValue(String propertyName) {
		String value = properties.getProperty(propertyName);
		String tmp = System.getProperty("java.io.tmpdir");

		if(value == null || value == "")
		{
			StringBuilder folder = new StringBuilder(tmp)
					.append(File.separator)
					.append("vbpmn");
			if(propertyName.equals("INPUT_PATH"))
			{
				folder.append(File.separator)
				.append("input");
			}

			else if(propertyName.equals("OUTPUT_PATH"))
			{
				folder.append(File.separator)
				.append("output");
			}
			else {
				folder.append(File.separator)
				.append(propertyName);
			}
			value = folder.toString();
		}
		return value;
	}

}
