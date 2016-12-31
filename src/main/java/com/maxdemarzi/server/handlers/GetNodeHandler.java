package com.maxdemarzi.server.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxdemarzi.GuancialeDB;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

import java.nio.ByteBuffer;

import static com.maxdemarzi.server.Server.JSON_UTF8;

public class GetNodeHandler implements HttpHandler {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static GuancialeDB db;

    public GetNodeHandler(GuancialeDB db) {
        this.db = db;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, JSON_UTF8);

        String id = exchange.getAttachment(io.undertow.util.PathTemplateMatch.ATTACHMENT_KEY)
                .getParameters().get("id");


        exchange.getResponseSender().send(ByteBuffer.wrap(objectMapper.writeValueAsBytes(db.getNode(id))));
    }}
