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

package com.hp.hpl.jena.tdb.solver.stats;

import java.util.Map ;

import com.hp.hpl.jena.graph.Node ;

/** Statistics collector */
public class StatsResults 
{
    private final Map<Node, Integer> predicates ;
    private final Map<Node, Integer> types ;
    private final long count ;

    StatsResults(Map<Node, Integer> predicates, Map<Node, Integer> types, long count)
    {
        this.count = count ;
        this.predicates = predicates ;
        this.types = types ;
    }

    public Map<Node, Integer> getPredicates()
    {
        return predicates ;
    }

    public Map<Node, Integer> getTypes()
    {
        return types ;
    }

    public long getCount()
    {
        return count ;
    }
}
