package org.apache.jena.fuseki.cache;

import com.hp.hpl.jena.util.cache.Cache;

/**
 * Created by saikat on 30/01/15.
 */
public class CacheAction {

    public CacheAction.Type type;

    private String key;

    public CacheAction(String key,CacheAction.Type type){
        this.key = key;
        this.type = type;
    }

    public enum Type{
        READ_CACHE,
        WRITE_CACHE,
        IDLE
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
