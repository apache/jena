/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.store;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.util.*;

/** A Feature is a name/value pair */

public class Feature extends PrintableBase implements Named, Printable 
{
    public static class Name extends Symbol
    {
        public Name(Symbol other)       { super(other) ; }
        public Name(String nameStr)     { super(nameStr) ; }
    }

    Name name ;
    Object value ;
    private Log log = LogFactory.getLog(Feature.class) ;
    
    private Feature(Name name) { this.name = name ; }
    
    public Feature(Name name, String value)
    { this(name) ; this.value = value ; }
    
    public Feature(Name name, long value)
    { this(name) ; this.value = value ; }

    public Object getAsObject()         { return value ; }
    
    public String getAsString()
    {
        if ( value instanceof String )
            return (String)value ;
        log.warn("Not a string: "+this) ;
        return null ;
    }
    
    public long getAsInteger()
    {
        if ( value instanceof Long )
            return (Long)value ;
        log.warn("Not a long: "+this) ;
        return -1 ;
    }

    @Override
    public int hashCode()
    { return name.hashCode() | value.hashCode() ; }
    
    @Override
    public boolean equals(Object other)
    {
        if ( this == other ) return true ;
        if ( ! ( other instanceof Feature ) ) return false ;
        Feature f = (Feature)other ;
        return f.name.equals(name) && f.value.equals(value) ;
    }
    
    // Interface Named
    public String getName()
    {
        return name.getSymbol() ; 
    }

    // Printable
    public void output(IndentedWriter out)
    {
        out.print(toString()) ;
    }
    
    @Override
    public String toString() { return name+"="+value ; }
}

//public enum Feature //implements Named
//{
//    // What about layout and DBType?  Aren't these "features"?
//    
//    // Some placeholders
//    
//    // The Jena2 database layout
//    LayoutRDB          { public String getName() { return "RDB" ; } } ,          
//    // A database layout that uses a single triple table, with entries being SPARQL-syntax RDF-terms
//    LayoutSimple       { public String getName() { return "Layout1" ; } } ,   
//    // The Triple table/Node table layout 
//    LayoutTripleNodes  { public String getName() { return "TriplesNodes" ; } } ,
//    
//    // Layout2
//    // Name of the column that identifies nodes ("id" or "hash") 
//    Layout2_NodeKeyColName { public String getName() { return "NodeKeyColName" ; } } ,
//    ;
//

//    public /*abstract*/ String getURI() { return "" ; }
//
//    public String getName()
//    {
//        return null ;
//    }
//    }

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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