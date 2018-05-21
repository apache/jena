/**
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

package org.apache.jena.fuseki.server;

import java.util.Objects;

/** Names for all counters */ 
public class CounterName {
 // Create intern'ed symbols. 
    static private NameMgr<CounterName> mgr = new NameMgr<>();
    static public CounterName register(String name, String hierarchicalName) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(hierarchicalName, "hierarchicalName");
        return mgr.register(name, (n)->new CounterName(name, hierarchicalName));
    }
    
    // The "name" is used as a JSON key string.
    // Legacy from when this was an enum and the name() was used for the UI.
    // The better hierarchicalName is not used but becuse this has
    // leaked to the jaavscript, we're a bit stuck. 

    private final String name ;
    private final String hierarchicalName ;
    
    // There are generic names - apply to all services and datasets.
    // Total request received
    public static final CounterName Requests         = register("Requests", "requests");
    // .. of which some and "good" and some are "bad".
    // #"good" + #"bad" roughly equals #"requests"
    // except that the total is incremented at the start, and the outcome at the end.
    // There may also be short term consistency issues.
    public static final CounterName RequestsGood     = register("RequestsGood", "requests.good");
    public static final CounterName RequestsBad      = register("RequestsBad", "requests.bad");

    // SPARQL Protocol - query and update - together with upload.

    // Query - standard and ...
    public static final CounterName QueryTimeouts    = register("QueryTimeouts", "query.timeouts");
    public static final CounterName QueryExecErrors  = register("QueryExecErrors", "query.execerrors");
    public static final CounterName QueryIOErrors    = register("QueryIOErrors", "query.ioerrors");

    // Update - standard and ...
    public static final CounterName UpdateExecErrors = register("UpdateExecErrors", "update.execerrors");

    // Upload ... standard counters

    // Graph Store Protocol. uses HTTP codes.

    // For each HTTP method

    public static final CounterName HTTPget          = register("HTTPget", "http.get.requests");
    public static final CounterName HTTPgetGood      = register("HTTPgetGood", "http.get.requests.good");
    public static final CounterName HTTPgetBad       = register("HTTPGetBad", "http.get.requests.bad");

    public static final CounterName HTTPpost         = register("HTTPpost", "http.post.requests");
    public static final CounterName HTTPpostGood     = register("HTTPpostGood", "http.post.requests.good");
    public static final CounterName HTTPpostBad      = register("HTTPpostBad", "http.post.requests.bad");

    public static final CounterName HTTPdelete       = register("HTTPdelete", "http.delete.requests");
    public static final CounterName HTTPdeleteGood   = register("HTTPdeleteGood", "http.delete.requests.good");
    public static final CounterName HTTPdeleteBad    = register("HTTPdeleteBad", "http.delete.requests.bad");

    public static final CounterName HTTPput          = register("HTTPput", "http.put.requests");
    public static final CounterName HTTPputGood      = register("HTTPputGood", "http.put.requests.good");
    public static final CounterName HTTPputBad       = register("HTTPputBad", "http.put.requests.bad");

    public static final CounterName HTTPhead         = register("HTTPhead", "http.head.requests");
    public static final CounterName HTTPheadGood     = register("HTTPheadGood", "http.head.requests.good");
    public static final CounterName HTTPheadBad      = register("HTTPheadBad", "http.head.requests.bad");

    public static final CounterName HTTPpatch        = register("HTTPpatch", "http.patch.requests");
    public static final CounterName HTTPpatchGood    = register("HTTPpatchGood", "http.patch.requests.good");
    public static final CounterName HTTPpatchBad     = register("HTTPpatchBad", "http.patch.requests.bad");

    public static final CounterName HTTPoptions      = register("HTTPoptions", "http.options.requests");
    public static final CounterName HTTPoptionsGood  = register("HTTPoptionsGood", "http.options.requests.good");
    public static final CounterName HTTPoptionsBad   = register("HTTPoptionsBad", "http.options.requests.bad");
    
    private CounterName(String name, String hierarchicalName) {
        this.name = name;
        this.hierarchicalName = hierarchicalName;
    }
    
    public String getName() {
        return name;
    }
    
    public String getFullName() {
        return hierarchicalName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        CounterName other = (CounterName)obj;
        if ( name == null ) {
            if ( other.name != null )
                return false;
        } else if ( !name.equals(other.name) )
            return false;
        return true;
    }
}
