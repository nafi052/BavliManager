package bavli;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

/**
 * LRU cache for page content (max 100 entries). Used by {@link FileManager#loadPage}.
 */
public class CacheManager {

    private static final int MAX_SIZE = 100;
    private static final Map<String, CacheEntry> cache = new LinkedHashMap<>(MAX_SIZE, 0.75f, true) {
        
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, CacheEntry> eldest) {
            return size() > MAX_SIZE;
        }
    };
    
    private static class CacheEntry { // מחלקה פנימית לשמירת מידע נוסף על כל פריט במטמון
        private final String data;
        private int hits;
        private final long creationTime;
        
        public CacheEntry(String data) {
            this.data = data;
            this.hits = 1;
            this.creationTime = System.currentTimeMillis();
        }
        
        public String getData() {
            hits++;
            return data;
        }
        
        public int getHits() {
            return hits;
        }
        
        public long getCreationTime() {
            return creationTime;
        }
        
        public long getAge() {
            return System.currentTimeMillis() - creationTime;
        }
    }

    /** Returns cached value for key, or null if absent. */
    public static String get(String key) {
        CacheEntry entry = cache.get(key);
        return entry != null ? entry.getData() : null;
    }

    /** Puts a key-value pair into the cache. */
    public static void put(String key, String value) {
        cache.put(key, new CacheEntry(value));
    }

    /** Returns true if the key is in the cache. */
    public static boolean contains(String key) {
        return cache.containsKey(key);
    }
    
    /** Removes the entry for the given key. */
    public static void remove(String key) {
        if (key != null) {
            cache.remove(key);
        }
    }
    
    /** Clears all cache entries. */
    public static void clear() {
        cache.clear();
    }
    
    /** Current number of entries in the cache. */
    public static int getSize() {
        return cache.size();
    }

    /** Maximum cache capacity. */
    public static int getMaxSize() {
        return MAX_SIZE;
    }
    
    /** Returns map of cache key to hit count. */
    public static Map<String, Integer> getHitsStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        for (Map.Entry<String, CacheEntry> entry : cache.entrySet()) {
            stats.put(entry.getKey(), entry.getValue().getHits());
        }
        return stats;
    }
    
    /** Returns up to {@code count} keys with highest hit count. */
    public static List<String> getPopularItems(int count) {
        List<Map.Entry<String, CacheEntry>> sorted = new ArrayList<>(cache.entrySet());
        sorted.sort((e1, e2) -> Integer.compare(e2.getValue().getHits(), e1.getValue().getHits()));
        
        List<String> result = new ArrayList<>();
        int limit = Math.min(count, sorted.size());
        for (int i = 0; i < limit; i++) {
            result.add(sorted.get(i).getKey());
        }
        return result;
    }
}
