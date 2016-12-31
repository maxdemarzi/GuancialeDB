package com.maxdemarzi.server.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.undertow.Undertow;
import io.undertow.server.RoutingHandler;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.HashMap;

import static com.maxdemarzi.server.Server.JSON_UTF8;
import static org.junit.Assert.assertEquals;

public class RootHandlerTest {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static Undertow undertow;
    private static final JerseyClient client = JerseyClientBuilder.createClient();

    @Before
    public void setUp() {

        undertow = Undertow.builder()
                .addHttpListener( 9090, "localhost" )
                .setHandler(new RoutingHandler()
                        .add("GET", "/db/root", new RootHandler())
                )
                .build();
        undertow.start();
    }

    @After
    public void tearDown() throws Exception {
        undertow.stop();
    }

    @Test
    public void shouldRespondToRootRequest() throws IOException {
        Response response = client.target( "http://localhost:9090" )
                .register( HashMap.class )
                .path( "/db/root" )
                .request( JSON_UTF8 )
                .get();


        int code = response.getStatus();
        HashMap actual = objectMapper.readValue( response.readEntity( String.class ), HashMap.class );

        assertEquals( 200, code );
        assertEquals( expected, actual );
    }

    private static final HashMap<String, String> expected = new HashMap<String, String>(){{
        put("version","0.0.1");
    }};
}
