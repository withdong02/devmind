package com.devmind.harness.engine;

import com.devmind.core.harness.Hook;
import com.devmind.core.harness.HookContext;
import com.devmind.core.harness.HookType;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Executes hook chains for each HookType. Hooks are sorted by order.
 */
@Component
public class HookExecutor {

    private final Map<HookType, List<Hook>> hookChains = new ConcurrentHashMap<>();

    public HookExecutor(List<Hook> allHooks) {
        for (HookType type : HookType.values()) {
            List<Hook> chain = allHooks.stream()
                    .filter(h -> h.getType() == type)
                    .sorted(Comparator.comparingInt(Hook::getOrder))
                    .toList();
            hookChains.put(type, chain);
        }
    }

    /**
     * Execute all hooks for the given type. Returns false if any hook cancelled.
     */
    public boolean execute(HookType type, HookContext context) {
        List<Hook> chain = hookChains.get(type);
        if (chain == null || chain.isEmpty()) return true;

        for (Hook hook : chain) {
            try {
                boolean continueChain = hook.execute(context);
                if (!continueChain || context.isCancelled()) {
                    return false;
                }
            } catch (Exception e) {
                context.put("hookError", hook.getName() + ": " + e.getMessage());
            }
        }
        return true;
    }

    public Map<HookType, List<Hook>> getHookChains() {
        return Map.copyOf(hookChains);
    }
}
