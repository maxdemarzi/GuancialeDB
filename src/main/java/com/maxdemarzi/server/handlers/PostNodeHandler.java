package com.maxdemarzi.server.handlers;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxdemarzi.GuancialeDB;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import shaded.org.apache.commons.io.IOUtils;

import java.util.HashMap;

import static com.maxdemarzi.server.Server.JSON_UTF8;

public class PostNodeHandler  implements HttpHandler {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static GuancialeDB db;
    TypeReference<HashMap<String,Object>> typeRef
            = new TypeReference<HashMap<String,Object>>() {};

    public PostNodeHandler(GuancialeDB db) {
        this.db = db;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if(exchange.isInIoThread()){
            exchange.dispatch(this);
            return;
        }
        try {
            exchange.startBlocking();
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, JSON_UTF8);
            String body = new String(IOUtils.toByteArray(exchange.getInputStream()));
            String id = exchange.getAttachment(io.undertow.util.PathTemplateMatch.ATTACHMENT_KEY)
                    .getParameters().get("id");
            if(body.isEmpty()) {
                if (db.addNode(id, body)) {
                    exchange.setStatusCode(201);
                }
            } else {
                try {
                    HashMap<String, Object> properties = objectMapper.readValue(body, typeRef);
                    if (db.addNode(id, properties)) {
                        exchange.setStatusCode(201);
                    }
                } catch (JsonParseException e) {
                    if (db.addNode(id, body)) {
                        exchange.setStatusCode(201);
                    }
                }
            }

            exchange.getResponseSender().close();
        } catch (Exception e) {
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            exchange.getResponseSender().send("{\"status\":\"failed\",\"comment\":\"" + e.getLocalizedMessage() + "\"}");
        }
    }
}
