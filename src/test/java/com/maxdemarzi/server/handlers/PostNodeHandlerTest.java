package com.maxdemarzi.server.handlers;

import com.maxdemarzi.GuancialeDB;
import io.undertow.Undertow;
import io.undertow.server.RoutingHandler;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.HashMap;

import static com.maxdemarzi.server.Server.JSON_UTF8;

public class PostNodeHandlerTest {
    private static Undertow undertow;
    private static final JerseyClient client = JerseyClientBuilder.createClient();
    private static final GuancialeDB db = new GuancialeDB(1000000, 10000000);

    @Before
    public void setUp() {
        undertow = Undertow.builder()
                .addHttpListener( 9090, "localhost" )
                .setHandler(new RoutingHandler()
                        .add("POST", "/db/node/{id}", new PostNodeHandler(db))
                )
                .build();

        undertow.start();
    }

    @After
    public void tearDown() throws Exception {
        undertow.stop();
    }

    @Test
    public void shouldRespondToEmptyNodeRequest() throws IOException {
        Response response = client.target( "http://localhost:9090" )
                .register( HashMap.class )
                .path( "/db/node/max" )
                .request( JSON_UTF8 )
                .post(null);

        int code = response.getStatus();
        Assert.assertEquals(201, code);
    }

    @Test
    public void shouldRespondToNodeWithValueRequest() throws IOException {
        Response response = client.target( "http://localhost:9090" )
                .register( HashMap.class )
                .path( "/db/node/max" )
                .request( JSON_UTF8 )
                .post(Entity.text("Chicago"));

        int code = response.getStatus();
        Assert.assertEquals(201, code);
        Object actual = db.getNode("max");
        Assert.assertEquals("Chicago", actual);
    }

    @Test
    public void shouldRespondToNodeWithPropertiesRequest() throws IOException {
        Response response = client.target( "http://localhost:9090" )
                .register( HashMap.class )
                .path( "/db/node/max" )
                .request( JSON_UTF8 )
                .post(Entity.json(properties));

        int code = response.getStatus();
        Assert.assertEquals(201, code);
        Object actual = db.getNode("max");
        Assert.assertEquals(properties, actual);
    }

    private static final HashMap<String, Object> properties = new HashMap<String, Object>(){{
        put("name","max");
    }};
}
