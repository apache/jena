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

package org.apache.jena.sparql.engine.http;

import static java.util.stream.Collectors.toList;

import java.nio.charset.StandardCharsets ;
import java.util.* ;
import org.apache.http.NameValuePair ;
import org.apache.http.client.utils.URLEncodedUtils ;

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
     * @return this Params for continued operation
     */
    public Params addParam(String name, String value)
    {
        Pair p = new Pair(name, value) ;
        paramList.add(p) ;
        params.computeIfAbsent(name, n -> new ArrayList<>()).add(value);
        return this;
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
        paramList.removeIf(p -> p.getName().equals(name));
        // Map
        params.remove(name) ;
    }

    /** Exactly as seen */
    public List<Pair> pairs()
    {
        return paramList ;
    }

    public int count() { return paramList.size() ; }

    /** Get the names of parameters - one occurrence */
    public List<String> names()
    {
        return paramList.stream().map(Pair::getName).distinct().collect(toList());
    }

    /** Query string, without leading "?" */
    public String httpString() {
        return URLEncodedUtils.format(paramList, StandardCharsets.UTF_8) ;
    }

    @Override
    public String toString() {
        return paramList.toString();
    }

    private List<String> getMV(String name)
    {
        return params.get(name) ;
    }

    static class MultiValueException extends RuntimeException
    {
        MultiValueException(String msg) { super(msg) ; }
    }

    public static class Pair extends org.apache.jena.atlas.lib.Pair<String, String> implements NameValuePair {
        public Pair(String name, String value) { super(name, value); }
        @Override
        public String getName()  { return getLeft() ;  }
        @Override
        public String getValue() { return getRight() ; }
    }
}
