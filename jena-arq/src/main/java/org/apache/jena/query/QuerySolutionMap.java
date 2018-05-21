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

package org.apache.jena.query;

import java.util.HashMap ;
import java.util.Iterator ;
import java.util.Map ;

import org.apache.jena.rdf.model.RDFNode ;
import org.apache.jena.sparql.core.QuerySolutionBase ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.util.FmtUtils ;

/** Implementation of QuerySolution that is backed by an in-memory map. */ 

public class QuerySolutionMap extends QuerySolutionBase
{
    private Map<String, RDFNode> map = new HashMap<>() ;
    
    public QuerySolutionMap() { } 

    public void add(String name, RDFNode node)
    { map.put(Var.canonical(name), node) ; }

    @Override
    protected RDFNode _get(String varName)          { return map.get(varName) ; } 

    @Override
    protected boolean _contains(String varName)     { return map.containsKey(varName) ; } 

    @Override
    public Iterator<String> varNames()                   { return map.keySet().iterator() ; }
    
    /** Add all of the solutions from one QuerySolutionMap into this QuerySolutionMap */
    public void addAll(QuerySolutionMap other)
    { 
        map.putAll(other.map);
    }

    /** Return a <code>Map&lt;String, RDFNode&gt;</code> representing the current
     * state of this <code>QuerySolutionMap</code>. The map is not connected
     * to the <code>QuerySolutionMap</code> and later changes to either <code>Map</code>
     * or <code>QuerySolutionMap</code> will not reflected in the other.  
     * 
     * @return {@literal Map<String, RDFNode>}
     */
    public Map<String, RDFNode> asMap() { 
        return new HashMap<>(map) ;
    }
    
    /** Add all of the solutions from one QuerySolution into this QuerySolutionMap */
    public void addAll(QuerySolution other)
    { 
        Iterator<String> iter = other.varNames() ;
        for ( ; iter.hasNext(); )
        {
            String vName = iter.next() ;
            RDFNode rdfNode = other.get(vName) ; 
            map.put(vName, rdfNode);
        }
    }

    /** Clear this QuerySolutionMap */
    public void clear() { map.clear(); }
    
    @Override
    public String toString()
    {
        String tmp = "" ;
        String sep = "" ;
        for ( Iterator<String> iter = varNames() ; iter.hasNext() ; )
        {
            String varName = iter.next() ;
            RDFNode n = _get(varName) ;
            String nStr = FmtUtils.stringForRDFNode(n) ;
            tmp = tmp+sep+"( ?"+varName+", "+nStr+" )" ;
        }
        return tmp ;
    }
}
