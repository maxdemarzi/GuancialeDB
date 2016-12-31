package com.maxdemarzi.server.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxdemarzi.GuancialeDB;
import io.undertow.Undertow;
import io.undertow.server.RoutingHandler;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.HashMap;

import static com.maxdemarzi.server.Server.JSON_UTF8;

public class GetNodeHandlerTest {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static Undertow undertow;
    private static final JerseyClient client = JerseyClientBuilder.createClient();
    private static final GuancialeDB db = new GuancialeDB(1000000, 10000000);

    @Before
    public void setUp() {
        undertow = Undertow.builder()
                .addHttpListener( 9090, "localhost" )
                .setHandler(new RoutingHandler()
                        .add("GET", "/db/node/{id}", new GetNodeHandler(db))
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
                .get();

        int code = response.getStatus();
        HashMap actual = objectMapper.readValue( response.readEntity( String.class), HashMap.class );

        Assert.assertEquals(200, code);
        Assert.assertEquals(new HashMap(), actual);
    }

    @Test
    public void shouldRespondToNodeWithPropertiesRequest() throws IOException {
        db.addNode("max", properties);
        Response response = client.target( "http://localhost:9090" )
                .register( HashMap.class )
                .path( "/db/node/max" )
                .request( JSON_UTF8 )
                .get();

        int code = response.getStatus();
        HashMap actual = objectMapper.readValue( response.readEntity( String.class), HashMap.class );

        Assert.assertEquals(200, code);
        Assert.assertEquals(properties, actual);
    }
    private static final HashMap<String, String> properties = new HashMap<String, String>(){{
        put("name","max");
    }};

}
