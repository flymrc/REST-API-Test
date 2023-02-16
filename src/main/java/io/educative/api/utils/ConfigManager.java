package io.educative.api.utils;

import java.io.InputStream;
import java.util.Properties;

public final class ConfigManager {

	private static ConfigManager manager;

	private static final Properties PROPS = new Properties();

	private ConfigManager() {
		try (InputStream stream = ConfigManager.class.getResourceAsStream("/config.properties")) {

			PROPS.load(stream);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static ConfigManager getInstance() {
		if (manager == null) {
			synchronized (ConfigManager.class) {
				manager = new ConfigManager();
			}
		}
		return manager;
	}

	public String getString(String key) {
		return System.getProperty(key, PROPS.getProperty(key));
	}

}
