package com.devmind.common.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Utility for parsing JSON responses from LLMs.
 * Handles common LLM quirks: markdown fences, trailing commas, extra text.
 */
public final class LlmJsonParser {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private LlmJsonParser() {}

    /**
     * Strip markdown code fences from LLM output.
     */
    public static String cleanMarkdownFences(String text) {
        String clean = text.strip();
        if (clean.startsWith("```")) {
            clean = clean.replaceAll("```json\\s*", "").replaceAll("```\\s*", "");
        }
        return clean.strip();
    }

    /**
     * Parse a JSON array string into a list of JsonNode objects.
     * Returns empty list if parsing fails.
     */
    public static List<JsonNode> parseArray(String json) {
        List<JsonNode> result = new ArrayList<>();
        try {
            String clean = cleanMarkdownFences(JsonExtractor.findArray(json));
            JsonNode node = MAPPER.readTree(clean);
            if (node.isArray()) {
                for (JsonNode item : node) {
                    result.add(item);
                }
            }
        } catch (Exception ignored) {
        }
        return result;
    }

    /**
     * Parse a JSON object string into a JsonNode.
     * Returns empty if parsing fails.
     */
    public static Optional<JsonNode> parseObject(String json) {
        try {
            String clean = cleanMarkdownFences(json);
            return Optional.of(MAPPER.readTree(clean));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Extract a string field from a JSON object string.
     * Falls back to manual extraction if Jackson fails.
     */
    public static String extractField(String json, String field) {
        try {
            JsonNode node = MAPPER.readTree(json);
            JsonNode value = node.get(field);
            if (value != null && value.isTextual()) {
                return value.asText();
            }
        } catch (Exception ignored) {
        }
        return manualExtractField(json, field);
    }

    /**
     * Extract all string items from a named array field in a JSON string.
     */
    public static List<String> extractStringArray(String json, String field) {
        List<String> result = new ArrayList<>();
        try {
            JsonNode node = MAPPER.readTree(json);
            JsonNode array = node.get(field);
            if (array != null && array.isArray()) {
                for (JsonNode item : array) {
                    if (item.isTextual()) {
                        result.add(item.asText());
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return result;
    }

    /**
     * Extract all object items from a named array field in a JSON string.
     */
    public static List<JsonNode> extractObjectArray(String json, String field) {
        List<JsonNode> result = new ArrayList<>();
        try {
            JsonNode node = MAPPER.readTree(json);
            JsonNode array = node.get(field);
            if (array != null && array.isArray()) {
                for (JsonNode item : array) {
                    if (item.isObject()) {
                        result.add(item);
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return result;
    }

    private static String manualExtractField(String json, String field) {
        int idx = json.indexOf("\"" + field + "\"");
        if (idx < 0) return null;
        int colon = json.indexOf(":", idx + field.length() + 2);
        if (colon < 0) return null;
        int start = json.indexOf("\"", colon + 1);
        if (start < 0) return null;
        int end = json.indexOf("\"", start + 1);
        if (end < 0) return null;
        return json.substring(start + 1, end);
    }

    /**
     * Helper to find a JSON array in a string that may contain extra text.
     */
    private static class JsonExtractor {
        static String findArray(String text) {
            int start = text.indexOf('[');
            int end = text.lastIndexOf(']');
            if (start >= 0 && end > start) {
                return text.substring(start, end + 1);
            }
            return text;
        }
    }
}
