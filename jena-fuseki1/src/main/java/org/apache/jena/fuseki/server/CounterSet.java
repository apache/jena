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

package org.apache.jena.fuseki.server ;

import java.util.Collection ;
import java.util.HashMap ;
import java.util.Map ;

import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/** A collection of counters */
public class CounterSet {
    private static Logger             log      = LoggerFactory.getLogger(CounterSet.class) ;

    private Map<CounterName, Counter> counters = new HashMap<CounterName, Counter>() ;

    public CounterSet() {}

    public Collection<CounterName> counters() {
        return counters.keySet() ;
    }

    public void inc(CounterName c) {
        get(c).inc() ;
    }

    public void dec(CounterName c) {
        get(c).dec() ;
    }

    public long value(CounterName c) {
        return get(c).value() ;
    }

    public void add(CounterName counterName) {
        if ( counters.containsKey(counterName) ) {
            log.warn("Duplicate counter in counter set: " + counterName) ;
            return ;
        }
        counters.put(counterName, new Counter()) ;
    }

    public boolean contains(CounterName cn) {
        return counters.containsKey(cn) ;
    }

    public Counter get(CounterName cn) {
        Counter c = counters.get(cn) ;
        if ( c == null )
            log.warn("No counter in counter set: " + cn) ;
        return c ;
    }
}
