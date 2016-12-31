package com.maxdemarzi.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;

class ServerProperties {
    static public Properties getProperties()  {
        File file = new File("conf/server.properties");
        return getProperties(file);
    }

    static public Properties getProperties(File file)  {
        Properties properties = new Properties();
        FileInputStream fis;
        try {
            fis = new FileInputStream(file);
            if (file.exists()) {
                properties.load(fis);
            } else {
                throw new FileNotFoundException("server.properties file not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        properties.putIfAbsent("max.nodes", "1000000");
        properties.putIfAbsent("max.rels", "10000000");

        return properties;
    }
}
