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

package com.hp.hpl.jena.sparql.engine.http;

import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Map ;

import com.hp.hpl.jena.sparql.util.Convert ;

/** A collection of parameters for protocol use. */

public class Params
{
    // As seen.
    private List<Pair> paramList = new ArrayList<>() ;
    
    // string -> list -> string
    private Map<String, List<String>> params = new HashMap<>() ;
    
    
    /** Create a Params object */
    
    public Params() { }
    
    /** Create a Params object, initialized from another one.  A copy is made
     * so the initial values of the Params object are as of the time this constructor
     * was called.
     *  
     * @param other
     */
    public Params(Params other)
    {
        merge(other) ;
    }
    
    public void merge(Params other)
    {
        params.putAll(other.params) ;
        paramList.addAll(other.paramList) ;
    }

    
    /** Add a parameter.
     * @param name  Name of the parameter
     * @param value Value - May be null to indicate none - the name still goes.
     */
    
    public void addParam(String name, String value)
    {
        Pair p = new Pair(name, value) ;
        paramList.add(p) ;
        List<String> x = params.get(name) ;
        if ( x == null )
        {
            x = new ArrayList<>() ;
            params.put(name, x) ;
        }
        x.add(value) ;
    }

    /** Valueless parameter */
    public void addParam(String name) { addParam(name, null) ; }
    
    public boolean containsParam(String name) { return params.containsKey(name) ; }
    
    public String getValue(String name)
    {
        List<String> x = getMV(name) ;
        if ( x == null )
            return null ;
        if ( x.size() != 1 )
            throw new MultiValueException("Multiple value ("+x.size()+" when exactly one requested") ; 
        return x.get(0) ;
    }
    
    public List<String> getValues(String name)
    {
        return getMV(name) ;
    }
        
    public void remove(String name)
    {
        // Absolute record
        for ( Iterator<Pair> iter = paramList.iterator() ; iter.hasNext() ; )
        {
            Pair p = iter.next() ;
            if ( p.getName().equals(name) )
                iter.remove() ;
        }
        // Map
        params.remove(name) ;
    }
    
    /** Exactly as seen */
    public List<Pair> pairs()
    {
        return paramList ;
    }
    
    public int count() { return paramList.size() ; }
    
    /** Get the names of parameters - one ocurrence */ 
    public List<String> names()
    {
        List<String> names = new ArrayList<>() ;
        for (Pair pair : paramList)
        {
            String s = pair.getName() ;
            if ( names.contains(s) )
                continue ;
            names.add(s) ;
        }
        return names ; 
    }
    
    public String httpString()
    {
        StringBuilder sbuff = new StringBuilder() ;
        boolean first = true ;
        for (Pair p : pairs())
        {
            if ( !first )
                sbuff.append('&') ;
            sbuff.append(p.getName()) ;
            sbuff.append('=') ;
            String x = p.getValue() ;
            x = Convert.encWWWForm(x) ;
            sbuff.append(x) ;
            first = false ;
        }
        return sbuff.toString() ;
    }
    
    private List<String> getMV(String name)
    {
        return params.get(name) ;
    }

    static class MultiValueException extends RuntimeException
    {
        MultiValueException(String msg) { super(msg) ; }
    }
        
    public static class Pair
    { 
        String name ;
        String value ;

        Pair(String name, String value) { setName(name) ; setValue(value) ; }
        public String getName()  { return name ;  }
        public String getValue() { return value ; }

        void setName(String name)   { this.name = name ; }
        void setValue(String value) { this.value = value ; }
        
    }
}
