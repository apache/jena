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

package com.hp.hpl.jena.sparql.path;

import java.util.ArrayList ;
import java.util.List ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;

public class P_NegPropSet extends PathBase
{
    List<P_Path0> nodes ;
    List<Node> forwardNodes ;
    List<Node> backwardNodes ;
    
    public P_NegPropSet()
    {
        nodes = new ArrayList<>() ;
        forwardNodes = new ArrayList<>() ;
        backwardNodes = new ArrayList<>() ;
    }
    
    // addFwd, addBkwd?
    public void add(P_Path0 p)
    {
        nodes.add(p) ;
        if ( p.isForward() )
            forwardNodes.add(p.getNode()) ;
        else
            backwardNodes.add(p.getNode()) ;
    }

    //public List<Node> getExcludedNodes() { return forwardNodes ; }

    public List<P_Path0> getNodes() { return nodes ; }
    public List<Node> getFwdNodes() { return forwardNodes ; }
    public List<Node> getBwdNodes() { return backwardNodes ; }

    @Override
    public void visit(PathVisitor visitor)
    { visitor.visit(this) ; }

    @Override
    public boolean equalTo(Path path2, NodeIsomorphismMap isoMap)
    {
        if ( ! ( path2 instanceof P_NegPropSet ) ) return false ;
        P_NegPropSet other = (P_NegPropSet)path2 ;
        return nodes.equals(other.nodes) ;
    }

    @Override
    public int hashCode()
    {
        return nodes.hashCode() ^ hashNegPropClass  ;
    }
}
