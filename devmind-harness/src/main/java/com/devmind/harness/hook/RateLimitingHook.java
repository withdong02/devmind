package com.devmind.harness.hook;

import com.devmind.core.harness.Hook;
import com.devmind.core.harness.HookContext;
import com.devmind.core.harness.HookType;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate limiting hook: max N calls per minute per tool.
 */
@Component
public class RateLimitingHook implements Hook {

    private volatile int maxCallsPerMinute = 30;
    private final ConcurrentHashMap<String, CallCounter> counters = new ConcurrentHashMap<>();

    @Override
    public String getName() { return "rate-limiting"; }

    @Override
    public HookType getType() { return HookType.PRE_TOOL_CALL; }

    @Override
    public int getOrder() { return 5; }

    @Override
    public boolean execute(HookContext context) {
        String toolName = context.get("toolName", String.class);
        if (toolName == null) return true;

        CallCounter counter = counters.computeIfAbsent(toolName, k -> new CallCounter());
        int count = counter.increment();
        if (count > maxCallsPerMinute) {
            context.put("blockedReason", "Rate limit exceeded for " + toolName + " (" + maxCallsPerMinute + "/min)");
            context.cancel();
            return false;
        }
        return true;
    }

    public void setMaxCallsPerMinute(int max) {
        this.maxCallsPerMinute = max;
    }

    private static class CallCounter {
        private final AtomicInteger count = new AtomicInteger(0);
        private volatile long windowStart = System.currentTimeMillis();

        int increment() {
            long now = System.currentTimeMillis();
            if (now - windowStart > 60_000) {
                count.set(0);
                windowStart = now;
            }
            return count.incrementAndGet();
        }
    }
}
