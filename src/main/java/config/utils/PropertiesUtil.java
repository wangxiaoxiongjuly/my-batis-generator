package config.utils;

import java.io.IOException;
import java.util.Properties;

/**
 * @author wenxuan.wong
 */
public class PropertiesUtil {
	private static Properties prop = new Properties();

	private PropertiesUtil() {
	}

	static {
		try {
			prop.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("generator.properties"));
		} catch (IOException e) {
		}
	}

	public static String getProperty(String key) {
		try {
			return prop.getProperty(key);
		}catch (Exception e){
			return "";
		}

	}

	public static String getProperty(String key, String defaultValue) {
		return isEmpty(prop.getProperty(key)) ? defaultValue : prop.getProperty(key);
	}

	public static boolean isEmpty(CharSequence cs) {
		return cs == null || cs.length() == 0;
	}

	public static String getPropertyReplaced(String key, String[] replacements) {
		try {
			return String.format(prop.getProperty(key), (Object[]) replacements);
		} catch (Exception e) {
			return null;
		}
	}
}
