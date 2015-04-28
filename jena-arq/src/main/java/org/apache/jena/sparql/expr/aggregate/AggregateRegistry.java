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

package org.apache.jena.sparql.expr.aggregate;

import java.util.HashMap ;
import java.util.Map ;

import org.apache.jena.graph.Node ;

/** Registry of custom aggregates
 * There is only a single global registry of aggregates - it affects parsing
 * and parsing happens before Context or Execution makes sense.   
 */
public class AggregateRegistry {
    
    private static Map<String, AccumulatorFactory> registry = new HashMap<>() ;
    private static Map<String, Node>               noGroupValues = new HashMap<>() ;
    
    /**
     * Register a custom aggregate, with its associated factory for accumulators.
     */
    public static void register(String uri, AccumulatorFactory accFactory) {
        register(uri, accFactory, null) ;
    }

    public static void register(String uri, AccumulatorFactory accFactory, Node noGroupValue) {
        registry.put(uri, accFactory) ;
        noGroupValues.put(uri, noGroupValue) ;
    }

    /**
     * Remove a registration.
     */
    public static void unregister(String uri) {
        registry.remove(uri) ;
        noGroupValues.remove(uri) ;
    }
    
    /** Return the AccumulatorFactory for a registered custom aggregate. */
    public static AccumulatorFactory getAccumulatorFactory(String uri) {
        return registry.get(uri) ;
    }
    
    /** Return the AccumulatorFactory for a registered custom aggregate. */
    public static Node getNoGroupValue(String uri) {
        return noGroupValues.get(uri) ;
    }

    /** Return the AccumulatorFactory for a registered custom aggregate. */
    public static boolean isRegistered(String uri) {
        return registry.containsKey(uri) ;
    }
}

