package com.maxdemarzi.server.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;

public class HandlerUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static HashMap getHashMapFromInput(String body) throws IOException {
        HashMap input = new HashMap();

        // Parse the input
        try {
            input = objectMapper.readValue(body, HashMap.class);
        } catch (Exception e) {

        }

        return input;
    }
}
