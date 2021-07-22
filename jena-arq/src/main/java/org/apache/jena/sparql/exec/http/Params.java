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

package org.apache.jena.sparql.exec.http;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.http.HttpLib;

/** A collection of parameters for HTTP protocol use. */
public class Params
{
    // As seen.
    private List<Param> paramList = new ArrayList<>();

    // string -> list -> string
    private Map<String, List<String>> params = new HashMap<>();

    /** Create a Params object */
    private Params() { }

    /** Pattern: {@code String URL = Params.create().add("name", "value")... .httpString(endpoint);} */
    public static Params create() { return new Params(); }

    /** Pattern: {@code String URL = Params.create(baseParams).add("name", "value")... .httpString(endpoint);} */
    public static Params create(Params other) { return new Params(other); }

    /**
     * Create a Params object, initialized from another one. A copy is made so the
     * initial values of the Params object are as of the time this constructor was
     * called.
     *
     * @param other
     */
    private Params(Params other) {
        merge(other);
    }

    public void merge(Params other) {
        params.putAll(other.params);
        paramList.addAll(other.paramList);
    }

    /**
     * Add a parameter.
     *
     * @param name Name of the parameter
     * @param value Value - May be null to indicate none - the name still goes.
     * @return this Params for continued operation
     */
    public Params add(String name, String value) {
        Param p = new Param(name, value);
        paramList.add(p);
        params.computeIfAbsent(name, n -> new ArrayList<>()).add(value);
        return this;
    }

    /** Valueless parameter */
    public Params add(String name) { return add(name, null); }

    /** @deprecated Use {@link #add(String,String)} */
    @Deprecated public Params addParam(String name, String value) { return add(name, value); }

    /** @deprecated Use {@link #add(String)} */
    @Deprecated public Params addParam(String name) { return add(name); }

    public boolean containsParam(String name) { return params.containsKey(name); }

    public String getValue(String name) {
        List<String> x = getMV(name);
        if ( x == null )
            return null;
        if ( x.size() != 1 )
            throw new MultiValueException("Multiple value (" + x.size() + " when exactly one requested");
        return x.get(0);
    }

    public List<String> getValues(String name) {
        return getMV(name);
    }

    public void remove(String name) {
        // Absolute record
        paramList.removeIf(p -> p.getName().equals(name));
        // Map
        params.remove(name);
    }

    /** Exactly as seen */
    public List<Param> pairs() {
        return paramList;
    }

    public int count() { return paramList.size(); }

    /** Get the names of parameters - one occurrence */
    public List<String> names() {
        return paramList.stream().map(Param::getName).distinct().collect(toList());
    }

    /** URL query string, without leading "?" */
    public String httpString() {
        return format(new StringBuilder(), paramList).toString();
    }

//    /** URL query string, without leading "?" */
//    public String httpString(String endpoint) {
//        if ( count() == 0 )
//            return endpoint;
//        Objects.requireNonNull(endpoint);
//        StringBuilder sBuff = new StringBuilder(endpoint);
//        String sep =  endpoint.contains("?") ? "&" : "?";
//        sBuff.append(sep);
//        return format(sBuff, paramList).toString();
//    }

    /**
     * Return a string that is suitable for HTTP use.
     */
    private static StringBuilder format(StringBuilder result, List <Param> parameters) {
        parameters.forEach(param->{
            String encodedName = encode(param.getName());
            String encodedValue = encode(param.getValue());
            if ( result.length() > 0 )
                result.append("&");
            result.append(encodedName);
            if (encodedValue != null) {
                result.append("=");
                result.append(encodedValue);
            }
        });
        return result;
    }

    private static String encode(String name) {
        if ( name == null )
            return name;
        return HttpLib.urlEncodeQueryString(name);
    }

    @Override
    public String toString() {
        return paramList.toString();
    }

    private List<String> getMV(String name) {
        return params.get(name);
    }

    static class MultiValueException extends RuntimeException {
        MultiValueException(String msg) {
            super(msg);
        }
    }

    // Pair, with more appropriate method names.
    static class Param extends org.apache.jena.atlas.lib.Pair<String, String> {
        public Param(String name, String value) { super(name, value); }
        public String getName()  { return getLeft();  }
        public String getValue() { return getRight(); }
    }
}
