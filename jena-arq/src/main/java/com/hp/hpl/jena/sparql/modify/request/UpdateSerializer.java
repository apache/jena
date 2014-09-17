package com.hp.hpl.jena.sparql.modify.request;

import java.util.Iterator;

import org.apache.jena.atlas.lib.Closeable;

import com.hp.hpl.jena.update.Update;

public interface UpdateSerializer extends Closeable {

    /**
     * Must be called prior to passing updates to the serializer
     */
    public abstract void open();

    /**
     * Serializes the given update
     * 
     * @param update
     *            Update
     */
    public abstract void update(Update update);

    /**
     * Serializes a sequence of updates
     * 
     * @param updates
     *            Updates
     */
    public abstract void update(Iterable<? extends Update> updates);

    /**
     * Serializes a sequence of updates
     * 
     * @param updates
     *            Updates
     */
    public abstract void update(Iterator<? extends Update> updateIter);

}