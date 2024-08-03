package ru.astondevs.servletrestservice.util;

import ru.astondevs.servletrestservice.listener.AppServletContextListener;

import java.io.IOException;
import java.util.Properties;

public class PropertiesFilePropertyLoader implements PropertyLoader {
    private final Properties properties;

    public PropertiesFilePropertyLoader(String path) {
        properties = new Properties();
        try {
            properties.load(AppServletContextListener.class.getResourceAsStream(path));
        } catch (IOException e) {
            throw new RuntimeException("cant initialize context", e);
        }
    }

    @Override
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}
