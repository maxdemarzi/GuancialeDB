package com.maxdemarzi.server;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

public class ServerPropertiesTest {

    private URL url;
    private File file;

    @Before
    public void setup() throws IOException {
        url = this.getClass().getResource("/conf/server.properties");
        file = new File(url.getFile());
    }
    @Test
    public void shouldLoadProperties() {
        Properties properties = ServerProperties.getProperties(file);
        Assert.assertEquals(properties.getProperty("web.port"), "8080");
    }
}
