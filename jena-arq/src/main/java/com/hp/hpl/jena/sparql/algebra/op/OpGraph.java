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

package com.hp.hpl.jena.sparql.algebra.op;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.OpVisitor ;
import com.hp.hpl.jena.sparql.algebra.Transform ;
import com.hp.hpl.jena.sparql.sse.Tags ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;

public class OpGraph extends Op1
{
    private final Node node ;

    public OpGraph(Node node, Op pattern)
    { 
        super(pattern) ; 
        this.node = node ;
    }
    
    public Node getNode() { return node ; }
    
    @Override
    public String getName()                         { return Tags.tagGraph ; }

    @Override
    public Op apply(Transform transform, Op op)     { return transform.transform(this, op) ; } 
    @Override
    public void visit(OpVisitor opVisitor)          { opVisitor.visit(this) ; }
    @Override
    public Op1 copy(Op newOp)                        { return new OpGraph(node, newOp) ; }
    
    @Override
    public int hashCode()
    { return node.hashCode() ^ getSubOp().hashCode() ; }
    
    @Override
    public boolean equalTo(Op other, NodeIsomorphismMap labelMap)
    {
        if ( ! (other instanceof OpGraph) ) return false ;
        OpGraph opGraph = (OpGraph)other ;
        if ( ! ( node.equals(opGraph.node) ) )
            return false ;
        return getSubOp().equalTo(opGraph.getSubOp(), labelMap) ;
    }
}
