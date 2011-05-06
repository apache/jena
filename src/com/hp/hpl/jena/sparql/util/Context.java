/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.util;

import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Map ;
import java.util.Set ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.sparql.ARQConstants ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;

/** A class for setting and keeping named values.  Used to pass 
 *  implementation-specific parameters across general
 *  interfaces. */  

public class Context
{
    protected Map<Symbol, Object> context = new HashMap<Symbol, Object>() ;
    protected List<Callback> callbacks = new ArrayList<Callback>() ;
    
    /** Create an empty context */
    public Context()
    { }
    
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
    public void put(Symbol property, Object value) { context.put(property, value) ; doCallbacks(property) ; }
    
    /** Store a named value - overwrites any previous set value */
    public void set(Symbol property, Object value) { context.put(property, value) ; doCallbacks(property) ; }

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
            context.put(property, value) ;
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

//    @Deprecated
//    public void setAll(Context other)
//    {
//        if ( other != null )
//        {
//            context.putAll(other.context) ;
//            callbacks.addAll(other.callbacks) ;
//        }
//    }
    
    public void putAll(Context other)
    {
        if ( other != null )
            // Does not copy callbacks
            context.putAll(other.context) ;
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
    public void addCallback(Callback m) { callbacks.add(m) ; }
    public void removeCallback(Callback m) { callbacks.remove(m) ; }
    public List<Callback> getCallbacks() { return callbacks ; }
    
    private void doCallbacks(Symbol symbol)
    {
        for ( Iterator<Callback> iter = callbacks.iterator() ; iter.hasNext() ; )
        {
            Callback m = iter.next() ;
            m.event(symbol) ;
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
        
        context.set(ARQConstants.sysCurrentTime, NodeFactory.nowAsDateTime()) ;
        
        // Allocators.
//        context.set(ARQConstants.sysVarAllocNamed, new VarAlloc(ARQConstants.allocVarMarkerExec)) ;
//        context.set(ARQConstants.sysVarAllocAnon,  new VarAlloc(ARQConstants.allocVarAnonMarkerExec)) ;
        // Add VarAlloc for variables and bNodes (this is not the parse name). 
        // More added later e.g. query (if there is a query), algebra form (in setOp)
        
        return context ; 
    }

}

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */