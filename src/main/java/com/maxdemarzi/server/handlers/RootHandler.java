package com.maxdemarzi.server.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

import java.nio.ByteBuffer;
import java.util.HashMap;

import static com.maxdemarzi.server.Server.JSON_UTF8;

public class RootHandler implements HttpHandler {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final HashMap<String, String> results = new HashMap<String, String>(){{
        put("version","0.0.1");
    }};

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, JSON_UTF8);
        exchange.getResponseSender().send(ByteBuffer.wrap(objectMapper.writeValueAsBytes(results)));
    }
}
