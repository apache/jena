/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.sparql.util;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.ARQException;
import org.apache.jena.sparql.core.DatasetGraph;

/**
 * A class for setting and keeping named values. Used to pass
 * implementation-specific parameters across general interfaces.
 */

public class Context {
    private static final Context EMPTY = new Context(true);

    protected Map<Symbol, Object>    context      = new ConcurrentHashMap<>();

    protected boolean                readonly     = false;

    /** Empty, immutable context */
    public static final Context emptyContext() { return EMPTY; }

    /** Create an empty context */
    public static Context create() { return new Context(); }

    /** Create an empty context */
    public Context() {}

    /* Create an empty context, mark its read-only state */
    private Context(boolean readonly) {
        this.readonly = readonly;
    }

    /**
     * Create a context and initialize it with a copy of the named values of
     * another one. Shallow copy: the values themselves are not copied
     */
    public Context(Context cxt) {
        putAll(cxt);
    }

    // All access to the underlying goes via map*.

    protected Object mapGet(Symbol property) {
        return context.get(property);
    }

    protected void mapPut(Symbol property, Object value) {
        if ( readonly )
            throw new ARQException("Context is readonly");
        if ( property == null )
            throw new ARQException("Context key is null");
        if ( value == null ) {
            mapRemove(property);
            return;
        }
        context.put(property, value);
    }

    protected void mapRemove(Symbol property) {
        context.remove(property);
    }

    protected boolean mapContains(Symbol property) {
        return context.containsKey(property);
    }

    protected Set<Symbol> mapKeySet() {
        return context.keySet();
    }

    protected int mapSize() {
        return context.size();
    }

    protected void mapForEach(BiConsumer<Symbol, Object> action) {
        context.forEach(action);
    }

    /**
     * Return a copy of this context. Modifications of the copy do not affect
     * the original context.
     */
    public Context copy() {
        return new Context(this);
    }

    // -- basic operations

    /** Get the object value of a property or null */
    @SuppressWarnings("unchecked")
    public <T> T get(Symbol property) {
        return (T) mapGet(property);
    }

    /**
     * Get the object value of a property - return the default value if not
     * present .
     */
    public <T> T get(Symbol property, T defaultValue) {
        T x = get(property);
        if ( x == null )
            return defaultValue;
        return x;
    }

    /** Store a named value - overwrites any previous set value */
    public void put(Symbol property, Object value) {
        mapPut(property, value);
    }

    /** Store a named value - overwrites any previous set value. Returns "this". */
    public Context set(Symbol property, Object value) {
        mapPut(property, value);
        return this;
    }

    // All access to the underlying goes via map*.

    /** Store a named value - overwrites any previous set value. Returns "this". */
    public Context set(Symbol property, boolean value) {
        if ( value )
            setTrue(property);
        else
            setFalse(property);
        return this;
    }

    /** Store a named value only if it is not currently set. Returns "this". */
    public Context setIfUndef(Symbol property, Object value) {
        Object x = mapGet(property);
        if ( x == null )
            put(property, value);
        return this;
    }

    /** Set property value to be true. Returns "this". */
    public Context setTrue(Symbol property) {
        return set(property, Boolean.TRUE);
    }

    /** Set property value to be false. Returns "this". */
    public Context setFalse(Symbol property) {
        return set(property, Boolean.FALSE);
    }

    public void putAll(Context other) {
        if ( readonly )
            throw new ARQException("Context is readonly");
        if ( other != null )
            other.mapForEach(this::put);
    }

    /** Remove any value associated with a property */
    public void remove(Symbol property) {
        mapRemove(property);
    }

    /** Remove any value associated with a property - alternative method name */
    public void unset(Symbol property) {
        remove(property);
    }

    // ---- Helpers

    // -- Existence

    /** Is a property set? */
    public boolean isDefined(Symbol property) {
        return mapContains(property);
    }

    /** Is a property not set? */
    public boolean isUndef(Symbol property) {
        return !isDefined(property);
    }

    // -- as string

    /**
     * Get the value a string (uses .toString() if the value is not null) -
     * supply a default string value
     */
    public String getAsString(Symbol property, String defaultValue) {
        String x = getAsString(property);
        if ( x == null )
            return defaultValue;
        return x;
    }

    /** Get the value a string (uses .toString() if the value is not null) */
    public String getAsString(Symbol property) {
        Object x = mapGet(property);
        if ( x == null )
            return null;
        return x.toString();
    }

    /** Get the value as a long value. The context entry can be a string, Integer or Long. */
    public int getInt(Symbol symbol, int defaultValue) {
        if (  isUndef(symbol) )
            return defaultValue;
        Object obj = mapGet(symbol);
        if ( obj instanceof String ) {
            return Integer.parseInt((String)obj);
        } else if ( obj instanceof Integer ) {
            return ((Integer)obj).intValue();
        } else {
            throw new ARQException("Value for "+symbol+" is not a recoginized class: "+Lib.className(obj));
        }
    }

    /** Get the value as a long value. The context entry can be a string, Integer or Long. */
    public long getLong(Symbol symbol, long defaultValue) {
        if (  isUndef(symbol) )
            return defaultValue;
        Object obj = mapGet(symbol);
        if ( obj instanceof String ) {
            return Long.parseLong((String)obj);
        } else if ( obj instanceof Integer ) {
            return ((Integer)obj).intValue();
        } else if ( obj instanceof Long ) {
            return ((Long)obj);
        } else {
            throw new ARQException("Value for "+symbol+" is not a recognized class: "+Lib.className(obj));
        }
    }

    /** Is the value 'true' (either set to the string "true" or Boolean.TRUE) */
    public boolean isTrue(Symbol property) {
        return isTrue(property, false);
    }

    /**
     * Is the value 'true' (either set to the string "true" or Boolean.TRUE) or
     * undefined?
     */
    public boolean isTrueOrUndef(Symbol property) {
        return isTrue(property, true);
    }

    private boolean isTrue(Symbol property, boolean dft) {
        Object x = get(property);
        if ( x == null )
            return dft;
        if ( x instanceof String ) {
            String s = (String)x;
            if ( s.equalsIgnoreCase("true") )
                return true;
            return false;
        }
        return x.equals(Boolean.TRUE);
    }

    /** Is the value 'false' (either set to the string "false" or Boolean.FALSE) */
    public boolean isFalse(Symbol property) {
        return isFalse(property, false);
    }

    /**
     * Is the value 'false' (either set to the string "false" or Boolean.FALSE)
     * or undefined
     */
    public boolean isFalseOrUndef(Symbol property) {
        return isFalse(property, true);
    }

    private boolean isFalse(Symbol property, boolean dft) {
        Object x = get(property);
        if ( x == null )
            return dft;
        if ( x instanceof String ) {
            String s = (String)x;
            if ( s.equalsIgnoreCase("false") )
                return true;
            return false;
        }
        return x.equals(Boolean.FALSE);
    }

    // -- Test for value

    /** Test whether a named value is a specific value (.equals) */

    public boolean hasValue(Symbol property, Object value) {
        Object x = get(property);
        if ( x == null && value == null )
            return true;
        if ( x == null || value == null )
            return false;
        return x.equals(value);
    }

    /** Test whether a named value (as a string) has a specific string form */

    public boolean hasValueAsString(Symbol property, String value) {
        return hasValueAsString(property, value, false);
    }

    /**
     * Test whether a named value (as a string) has a specific string form - can
     * ignore case
     */
    public boolean hasValueAsString(Symbol property, String value, boolean ignoreCase) {
        String s = getAsString(property);
        if ( s == null && value == null )
            return true;
        if ( s == null || value == null )
            return false;

        if ( ignoreCase )
            return s.equalsIgnoreCase(value);
        return s.equals(value);
    }

    /** Set of properties (as Symbols) currently defined */
    public Set<Symbol> keys() {
        return mapKeySet();
    }

    /** Return the number of context items */
    public int size() {
        return mapSize();
    }

    public void clear() {
        context.clear();
    }

    @Override
    public String toString() {
        String x = "";
        String sep = "";
        for ( Symbol s : keys() ) {
            Object value = get(s);
            x = x + sep + s + " = " + value;
            sep = "\n";
        }
        return x;
    }

    /** Return the context of the dataset (not copied); if the dataset is null, return null */
    public static Context fromDataset(DatasetGraph dataset) {
        if ( dataset == null )
            return null;
        return dataset.getContext();
    }

    /** Setup a context using another context and a dataset.*/
    public static Context setupContextForDataset(Context globalContext, DatasetGraph dataset) {
        // Copy per-dataset settings.
        Context dsgCxt = ( dataset != null )
            ? dataset.getContext()
            : null;
        Context context = mergeCopy(globalContext, dsgCxt);
        return context;
    }

    public static void setCurrentDateTime(Context context) {
        context.set(ARQConstants.sysCurrentTime, NodeFactoryExtra.nowAsDateTime());
    }

    /** Merge an outer (defaults to the system global context)
     *  and local context to produce a new context
     *  The new context is always a separate copy.
     */
    public static Context mergeCopy(Context contextGlobal, Context contextLocal) {
        if ( contextGlobal == null )
            contextGlobal = ARQ.getContext();
        Context context = contextGlobal.copy();
        if ( contextLocal != null )
            context.putAll(contextLocal);
        return context;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((context == null) ? 0 : context.hashCode());
        result = prime * result + (readonly ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        Context other = (Context)obj;
        if ( context == null ) {
            if ( other.context != null )
                return false;
        } else if ( !context.equals(other.context) )
            return false;
        if ( readonly != other.readonly )
            return false;
        return true;
    }
}
