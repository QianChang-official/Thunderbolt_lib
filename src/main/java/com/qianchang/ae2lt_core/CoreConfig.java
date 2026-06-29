package com.qianchang.ae2lt_core;

/**
 * Lightweight feature switches. The fast path is a behavior-preserving optimization that falls back
 * to AE2 whenever it is out of scope, but a global kill switch is kept for safe rollout / A-B
 * comparison against vanilla AE2.
 */
public final class CoreConfig {

    /** System property: {@code -Dae2lt_core.fastPath=false} disables the fast-path planner. */
    public static final boolean FAST_PATH_ENABLED =
            !"false".equalsIgnoreCase(System.getProperty("ae2lt_core.fastPath", "true"));

    private CoreConfig() {
    }
}
