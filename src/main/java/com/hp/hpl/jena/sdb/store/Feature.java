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

package com.hp.hpl.jena.sdb.store;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.io.Printable ;
import org.apache.jena.atlas.io.PrintableBase ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.sparql.util.Named ;
import com.hp.hpl.jena.sparql.util.Symbol ;

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
    private Logger log = LoggerFactory.getLogger(Feature.class) ;
    
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
    @Override
    public String getName()
    {
        return name.getSymbol() ; 
    }

    // Printable
    @Override
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
