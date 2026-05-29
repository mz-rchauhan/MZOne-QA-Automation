package core.base;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe scenario-scoped state bag.
 * Used by AutoRecoveryEngine and any step that needs to pass data between steps
 * without using instance fields (which break parallel execution).
 *
 * Usage:
 *   TestContext.store("lastUrl", driver.getCurrentUrl());
 *   String url = TestContext.retrieve("lastUrl");
 *
 * Always call TestContext.clear() in @After hook.
 */
public final class TestContext {

    private static final ThreadLocal<Map<String, Object>> CONTEXT =
            ThreadLocal.withInitial(ConcurrentHashMap::new);


    public static void store(String key, Object value) {
        CONTEXT.get().put(key, value);
    }

    public static Object retrieve(String key) {
        return CONTEXT.get().get(key);
    }

    public static <T> T retrieve(String key, Class<T> clazz) {
        Object val = CONTEXT.get().get(key);
        if (clazz.isInstance(val)) {
            return clazz.cast(val);
        }
        return null;
    }

    public static String retrieveString(String key) {
        Object val = CONTEXT.get().get(key);
        return val == null ? null : val.toString();
    }

    public static boolean contains(String key) {
        return CONTEXT.get().containsKey(key);
    }

    public static void remove(String key) {
        CONTEXT.get().remove(key);
    }

    public static void clear() {
        CONTEXT.get().clear();
        CONTEXT.remove();
    }

    public static Map<String, Object> getAll() {
        return Map.copyOf(CONTEXT.get());
    }
}
