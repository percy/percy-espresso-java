package io.percy.espresso.lib;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CacheTest {

    @Test
    public void testCacheMapIsInitialized() {
        assertNotNull(Cache.CACHE_MAP);
    }

    @Test
    public void testCacheMapStoresValues() {
        Cache.CACHE_MAP.put("cacheTestKey", "cacheTestValue");
        assertEquals("cacheTestValue", Cache.CACHE_MAP.get("cacheTestKey"));
        Cache.CACHE_MAP.remove("cacheTestKey");
    }

    @Test
    public void testCacheCanBeInstantiated() {
        // Covers the implicit default constructor and forces the static
        // initializer (CACHE_MAP) to be recorded on the JVM classloader.
        Cache cache = new Cache();
        assertNotNull(cache);
        assertNotNull(Cache.CACHE_MAP);
    }
}
