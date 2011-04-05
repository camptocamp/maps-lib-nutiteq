package com.nutiteq.cache;

import java.util.LinkedHashMap;
import java.util.Map;

import android.os.Debug;

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

    // private LinkedHashMap<String, SoftReference<byte[]>> cache;
    private static final int DEFAULT_LENGTH = 50;
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
        // cache = new LinkedHashMap<String, SoftReference<byte[]>>(mCacheSize + 1, loadFactor,
        // true) {
        cache = new LinkedHashMap<String, byte[]>(mCacheLength, loadFactor, true) {
            private static final long serialVersionUID = 1;

            @Override
            // protected boolean removeEldestEntry(Map.Entry<String, SoftReference<byte[]>> eldest)
            // {
            protected boolean removeEldestEntry(Map.Entry<String, byte[]> eldest) {
                android.util.Log.e("TEST", "mem=" + size + " (" + Debug.getNativeHeapFreeSize()
                        + ")");
                if (size() > mCacheLength || size > mCacheSize) {
                    size -= eldest.getValue().length;
                    // final SoftReference<byte[]> softRef = eldest.getValue();
                    // if (softRef != null) {
                    // if (softRef.get() == null) {
                    // return true;
                    // }
                    // size -= softRef.get().length;
                    // }
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
        // byte[] result = null;
        // final SoftReference<byte[]> softRef = cache.get(cacheId);
        // if (softRef != null) {
        // result = softRef.get();
        // if (result == null) {
        // cache.remove(cacheId);
        // }
        // }
        // return result;
        return cache.get(cacheId);
    }

    public void cache(final String cacheId, final byte[] data, final int cacheLevel) {
        if ((cacheLevel & CACHE_LEVEL_MEMORY) != CACHE_LEVEL_MEMORY || data == null
                || data.length == 0) {
            return;
        }
        size += data.length;
        synchronized (cache) {
            // cache.put(cacheId, new SoftReference<byte[]>(data));
            cache.put(cacheId, data);
        }
    }

    public boolean contains(final String cacheKey) {
        if (cache == null) {
            return false;
        }
        // return cache.containsKey(cacheKey) && !cache.get(cacheKey).isEnqueued();
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
        // final Collection<SoftReference<byte[]>> e = (Collection<SoftReference<byte[]>>) cache
        // .values();
        // final Iterator<SoftReference<byte[]>> i = e.iterator();
        int result = 0;
        // while (i.hasNext()) {
        // final byte[] item = i.next().get();
        // result += item.length;
        // }
        return result;
    }

    protected CacheItem getMRU() {
        CacheItem ci = new CacheItem();
        // Iterator<Entry<String, SoftReference<byte[]>>> i = cache.entrySet().iterator();
        // Entry<String, SoftReference<byte[]>> e = null;
        // while (i.hasNext()) {
        // e = i.next();
        // }
        // ci.key = e.getKey();
        // ci.data = e.getValue().get();
        return ci;
    }
}
