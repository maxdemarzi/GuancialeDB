package com.maxdemarzi.server;

import com.google.common.net.MediaType;
import com.maxdemarzi.GuancialeDB;
import com.maxdemarzi.server.handlers.GetNodeHandler;
import com.maxdemarzi.server.handlers.RootHandler;
import io.undertow.Undertow;
import io.undertow.server.RoutingHandler;

import java.io.IOException;
import java.util.Properties;

public class Server {

    public static final String JSON_UTF8 = MediaType.JSON_UTF_8.toString();
    public static final String TEXT_PLAIN = MediaType.PLAIN_TEXT_UTF_8.toString();

    private static Undertow server;
    private static GuancialeDB db;

    public Server() {
        Properties properties = ServerProperties.getProperties();

        db = new GuancialeDB(Integer.parseInt(properties.getProperty("max.nodes")),
                             Integer.parseInt(properties.getProperty("max.rels")));

        server = Undertow.builder()
                .addHttpListener(Integer.parseInt(properties.getProperty("web.port")),
                        properties.getProperty("web.host") )
                .setBufferSize(1024 * 16 )
                .setIoThreads(Runtime.getRuntime().availableProcessors() * 2)
                .setHandler(new RoutingHandler()
                        .add("GET", "/db/root", new RootHandler())
                        .add("GET", "/db/root/node/{id}", new GetNodeHandler(db))
                )
                .setWorkerThreads(200).build();
    }

    public static void main(final String[] args) throws IOException {
        server.start();
    }

    public static void stop() {
        server.stop();
    }
}
