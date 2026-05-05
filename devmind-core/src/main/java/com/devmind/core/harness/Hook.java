package com.devmind.core.harness;

/**
 * Interface for hooks that can intercept and modify agent behavior.
 */
public interface Hook {

    /**
     * Name of this hook for identification and logging.
     */
    String getName();

    /**
     * The type of hook point this hook attaches to.
     */
    HookType getType();

    /**
     * Execution order (lower runs first).
     */
    int getOrder();

    /**
     * Executes the hook. Returns true to continue the chain, false to short-circuit.
     */
    boolean execute(HookContext context);
}
