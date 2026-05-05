package com.devmind.harness.hook;

import com.devmind.core.harness.Hook;
import com.devmind.core.harness.HookContext;
import com.devmind.core.harness.HookType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingHook implements Hook {

    private static final Logger log = LoggerFactory.getLogger(LoggingHook.class);

    @Override
    public String getName() { return "logging"; }

    @Override
    public HookType getType() { return HookType.PRE_TOOL_CALL; }

    @Override
    public int getOrder() { return 100; }

    @Override
    public boolean execute(HookContext context) {
        String toolName = context.get("toolName", String.class);
        String input = context.get("input", String.class);
        log.info("[Hook:PRE_TOOL] tool={} input={}", toolName,
                input != null && input.length() > 200 ? input.substring(0, 200) + "..." : input);
        return true;
    }
}
