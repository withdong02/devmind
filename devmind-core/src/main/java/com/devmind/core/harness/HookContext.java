package com.devmind.core.harness;

import java.util.HashMap;
import java.util.Map;

/**
 * Mutable context passed through the hook chain.
 */
public class HookContext {

    private final HookType type;
    private final Map<String, Object> data;
    private boolean cancelled;

    public HookContext(HookType type) {
        this.type = type;
        this.data = new HashMap<>();
        this.cancelled = false;
    }

    public HookType getType() { return type; }

    public void put(String key, Object value) { data.put(key, value); }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        Object value = data.get(key);
        return value != null ? (T) value : null;
    }

    public void cancel() { this.cancelled = true; }
    public boolean isCancelled() { return cancelled; }
}
