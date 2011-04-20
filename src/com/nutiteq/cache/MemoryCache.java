package com.nutiteq.cache;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * <p>
 * Memory cache implementing LRU (least recently used) strategy. If cache is full, least recently
 * used items will be pushed out.
 * </p>
 * 
 * <p>
 * Current implementation uses only actual data size. Objects/keys overhead is not calculated in
 * cache size.
 * </p>
 */
public class MemoryCache implements Cache {

    protected static final String TAG = "MemoryCache";
    private static final int DEFAULT_LENGTH = 60;
    private LinkedHashMap<String, byte[]> cache;
    private static final float loadFactor = 1.1f;
    private final int mCacheSize;
    private int mCacheLength;
    private int size;

    /**
     * Create a new MemoryCache instance.
     * 
     * @param mCacheSize
     *            cache size in element number.
     * @param mCacheLength
     *            cache size in bytes.
     */
    public MemoryCache(final int cl, final int cs) {
        mCacheLength = cl;
        mCacheSize = cs;
    }

    /**
     * Create a new MemoryCache instance.
     * 
     * @param mCacheSize
     *            cache size in element number.
     */
    public MemoryCache(final int cs) {
        mCacheLength = DEFAULT_LENGTH;
        mCacheSize = cs;
    }

    public void initialize() {
        size = 0;
        cache = new LinkedHashMap<String, byte[]>(mCacheLength, loadFactor, true) {
            private static final long serialVersionUID = 1;

            @Override
            protected boolean removeEldestEntry(Map.Entry<String, byte[]> eldest) {
                if (size() > mCacheLength || size > mCacheSize) {
                    size -= eldest.getValue().length;
                    return true;
                }
                return false;
            }
        };
    }

    public void deinitialize() {
        if (cache != null) {
            cache.clear();
        }
        cache = null;
    }

    public byte[] get(final String cacheId) {
        return cache.get(cacheId);
    }

    public void cache(final String cacheId, final byte[] data, final int cacheLevel) {
        if ((cacheLevel & CACHE_LEVEL_MEMORY) != CACHE_LEVEL_MEMORY || data == null
                || data.length == 0) {
            return;
        }
        size += data.length;
        synchronized (cache) {
            cache.put(cacheId, data);
        }
    }

    public boolean contains(final String cacheKey) {
        if (cache == null) {
            return false;
        }
        return cache.containsKey(cacheKey);
    }

    public boolean contains(final String cacheKey, final int cacheLevel) {
        if ((cacheLevel & CACHE_LEVEL_MEMORY) != CACHE_LEVEL_MEMORY) {
            return false;
        }
        return contains(cacheKey);
    }

    // TEST METHODS
    protected int getCalculatedSize() {
        return size;
    }

    protected int getActualElementsSize() {
        final Collection<byte[]> e = (Collection<byte[]>) cache.values();
        final Iterator<byte[]> i = e.iterator();
        int result = 0;
        while (i.hasNext()) {
            final byte[] item = i.next();
            result += item.length;
        }
        return result;
    }

    protected CacheItem getMRU() {
        CacheItem ci = new CacheItem();
        Iterator<Entry<String, byte[]>> i = cache.entrySet().iterator();
        Entry<String, byte[]> e = null;
        while (i.hasNext()) {
            e = i.next();
        }
        ci.key = e.getKey();
        ci.data = e.getValue();
        return ci;
    }
}
