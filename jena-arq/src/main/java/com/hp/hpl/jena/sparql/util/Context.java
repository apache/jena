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

package com.hp.hpl.jena.sparql.util;

import java.util.* ;

import org.apache.jena.atlas.lib.Callback ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.sparql.ARQConstants ;
import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;

/** A class for setting and keeping named values.  Used to pass 
 *  implementation-specific parameters across general
 *  interfaces. */  

public class Context
{
    public static final Context emptyContext = new Context(true) ;
    
    protected Map<Symbol, Object> context = new HashMap<>() ;
    protected List<Callback<Symbol>> callbacks = new ArrayList<>() ;
    protected boolean readonly = false ;
    
    /** Create an empty context */
    public Context()
    { }
    
    /* Create an empty context, mark it's readonly state */
    private Context(boolean readonly)
    { 
        this.readonly = readonly ;
    }

    /** Create a context and initialize it with a copy of the named values of another one.
     *  Shallow copy: the values themselves are not copied
     */ 
    public Context(Context cxt)
    { putAll(cxt) ; }
    
    /** Return a copy of this context.  Modifications of the copy 
     * do not affect the original context.
     */ 
    public Context copy() { return new Context(this) ; }
    
    // -- basic operations
    
    /** Get the object value of a property or null */ 
    public Object get(Symbol property) { return context.get(property) ; }
    
    /** Get the object value of a property - return the default value if not present . */ 
    public Object get(Symbol property, Object defaultValue)
    { 
        Object x = context.get(property) ;
        if ( x == null )
            return defaultValue ;
        return x ;
    }
    
    /** Store a named value - overwrites any previous set value */
    public void put(Symbol property, Object value) { _put(property, value) ; doCallbacks(property) ; }
    
    /** Store a named value - overwrites any previous set value */
    public void set(Symbol property, Object value) { _put(property, value) ; doCallbacks(property) ; }
    
    private void _put(Symbol property, Object value)
    {
        if ( readonly )
            throw new ARQException("Context is readonly") ;
        context.put(property, value) ; 
    }

    /** Store a named value - overwrites any previous set value */
    public void   set(Symbol property, boolean value)
    { 
        if ( value )
            setTrue(property) ;
        else
            setFalse(property) ;
    }

    /** Store a named value only if it is not currently set */
    public void  setIfUndef(Symbol property, Object value)
    { 
        Object x = context.get(property) ;
        if ( x == null )
            put(property, value) ;
    }

    /** Remove any value associated with a property */
    public void remove(Symbol property) { context.remove(property) ; doCallbacks(property) ; }

    /** Remove any value associated with a property - alternative method name */
    public void unset(Symbol property) { context.remove(property) ; doCallbacks(property) ; }
    
    // ---- Helpers
    
    // -- Existence
    
    /** Is a property set? */
    public boolean isDefined(Symbol property) { return context.containsKey(property) ; }

    /** Is a property not set? */
    public boolean isUndef(Symbol property) { return ! isDefined(property) ; }
    
    // -- as string

    /** Get the value a string (uses .toString() if the value is not null) - supply a default string value */
    public String getAsString(Symbol property, String defaultValue)
    {
        String x = getAsString(property) ;
        if ( x == null )
            return defaultValue ;
        return x ;
    }

    /** Get the value a string (uses .toString() if the value is not null) */
    public String getAsString(Symbol property)
    { 
        Object x = context.get(property) ;
        if ( x == null )
            return null ;
        return x.toString() ;
    }

    public void putAll(Context other)
    {
        if ( readonly )
            throw new ARQException("Context is readonly") ;
        if ( other != null )
        {
            for ( Map.Entry<Symbol, Object> e : other.context.entrySet() )
                put(e.getKey(), e.getValue()) ;
        }
    }
    
    // -- true/false
    
    /** Set propety value to be true */
    public void setTrue(Symbol property) { set(property, Boolean.TRUE) ; } 
    
    /** Set propety value to be false */
    public void setFalse(Symbol property) { set(property, Boolean.FALSE) ; } 

    /** Is the value 'true' (either set to the string "true" or Boolean.TRUE) */
    public boolean isTrue(Symbol property)
    { return isTrue(property, false) ; } 
    
    /** Is the value 'true' (either set to the string "true" or Boolean.TRUE) 
     * or undefined? 
     */
    public boolean isTrueOrUndef(Symbol property)
    { return isTrue(property, true) ; }
    
    private boolean isTrue(Symbol property, boolean dft)
    {
        Object x = get(property) ;
        if ( x == null )
            return dft ;
        if ( x instanceof String )
        {
            String s = (String)x ;
            if ( s.equalsIgnoreCase("true") ) 
                return true ;
        }
        return x.equals(Boolean.TRUE) ;
    }
    
    /** Is the value 'false' (either set to the string "false" or Boolean.FALSE) */
    public boolean isFalse(Symbol property)
    { return isFalse(property, false) ; } 
    
    /** Is the value 'false' (either set to the string "false" or Boolean.FALSE) 
     * or undefined 
     */
    public boolean isFalseOrUndef(Symbol property)
    { return isFalse(property, true) ; }
    
    private boolean isFalse(Symbol property, boolean dft)
    {
        Object x = get(property) ;
        if ( x == null )
            return dft ;
        if ( x instanceof String )
        {
            String s = (String)x ;
            if ( s.equalsIgnoreCase("false") ) 
                return true ;
        }
        return x.equals(Boolean.FALSE) ;
    }

    // -- Test for value 

    /** Test whether a named value is a specific value (.equals) */
    
    public boolean hasValue(Symbol property, Object value)
    {
        Object x = get(property) ;
        if ( x == null && value == null )
            return true ;
        if ( x == null || value == null )
            return false ;
        return x.equals(value) ;
    }

    /** Test whether a named value (as a string) has a specific string form */
    
    public boolean hasValueAsString(Symbol property, String value)
    {
        return  hasValueAsString(property, value, false) ;
    }
    
    /** Test whether a named value (as a string) has a specific string form - can ignore case */
    public boolean hasValueAsString(Symbol property, String value, boolean ignoreCase)
    {
        String s = getAsString(property) ;
        if ( s == null && value == null )
            return true ;
        if ( s == null || value == null )
            return false ;
        
        if ( ignoreCase )
            return s.equalsIgnoreCase(value) ;
        return s.equals(value) ;
    }
    
    /** Set of properties (as Symbols) currently defined */  
    public Set<Symbol> keys() { return context.keySet() ; }

    /** Return the number of context items */ 
    public int size() { return context.size() ; }

    
//    @Override
//    public int hashCode()
//    {
//        return context.hashCode() ;
//    }
//    
//    @Override
//    public boolean equals(Object other)
//    {
//        if ( this == other ) return true ;
//
//        if ( ! ( other instanceof Context ) ) return false ;
//        Context cxt = (Context)other ;
//        return context.equals(cxt.context) ;
//    }
    
    // ---- Callbacks
    public void addCallback(Callback<Symbol> m) { callbacks.add(m) ; }
    public void removeCallback(Callback<Symbol> m) { callbacks.remove(m) ; }
    public List<Callback<Symbol>> getCallbacks() { return callbacks ; }
    
    private void doCallbacks(Symbol symbol)
    {
        for ( Callback<Symbol> c : callbacks )
        {
            c.proc(symbol) ;
        }
    }
    
    @Override
    public String toString()
    {
        String x = "" ;
        String sep = "" ;
        for ( Symbol s : keys() )
        {
            Object value = get(s) ;
            x = x + sep + s + " = " + value ;
            sep = "\n" ;
        }
        return x ;
    }
    
    // Put any per-dataset execution global configuration state here.
    public static Context setupContext(Context context, DatasetGraph dataset)
    {
        if ( context == null )
            context = ARQ.getContext() ;    // Already copied?
        context = context.copy() ;

        if ( dataset != null && dataset.getContext() != null )
            // Copy per-dataset settings.
            context.putAll(dataset.getContext()) ;
        
        context.set(ARQConstants.sysCurrentTime, NodeFactoryExtra.nowAsDateTime()) ;
        
        // Allocators.
//        context.set(ARQConstants.sysVarAllocNamed, new VarAlloc(ARQConstants.allocVarMarkerExec)) ;
//        context.set(ARQConstants.sysVarAllocAnon,  new VarAlloc(ARQConstants.allocVarAnonMarkerExec)) ;
        // Add VarAlloc for variables and bNodes (this is not the parse name). 
        // More added later e.g. query (if there is a query), algebra form (in setOp)
        
        return context ; 
    }

}
