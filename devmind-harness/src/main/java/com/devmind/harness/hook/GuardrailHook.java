package com.devmind.harness.hook;

import com.devmind.core.harness.Hook;
import com.devmind.core.harness.HookContext;
import com.devmind.core.harness.HookType;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Blocks dangerous commands/patterns. Configurable via profile.
 */
@Component
public class GuardrailHook implements Hook {

    private volatile List<String> blockedPatterns = List.of(
            "rm -rf /", "DROP TABLE", "git push --force", "format c:", "shutdown"
    );

    @Override
    public String getName() { return "guardrail"; }

    @Override
    public HookType getType() { return HookType.PRE_TOOL_CALL; }

    @Override
    public int getOrder() { return 10; }

    @Override
    public boolean execute(HookContext context) {
        String input = context.get("input", String.class);
        if (input == null) return true;

        String lowerInput = input.toLowerCase();
        for (String pattern : blockedPatterns) {
            if (lowerInput.contains(pattern.toLowerCase())) {
                context.put("blockedReason", "Blocked by guardrail: contains '" + pattern + "'");
                context.cancel();
                return false;
            }
        }
        return true;
    }

    public void setBlockedPatterns(List<String> patterns) {
        this.blockedPatterns = patterns;
    }
}
