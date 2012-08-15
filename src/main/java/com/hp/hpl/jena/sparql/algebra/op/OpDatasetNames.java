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

public class OpDatasetNames extends Op0
{
    Node graphNode ;

    public OpDatasetNames(Node gn) { this.graphNode = gn ; }
    
    public Node getGraphNode()
    {
        return graphNode ;
    }

    @Override
    public String getName()                 { return Tags.tagDatasetNames ; } 

    @Override
    public Op apply(Transform transform)    { return transform.transform(this) ; } 
    @Override
    public void visit(OpVisitor opVisitor)  { opVisitor.visit(this) ; }
    @Override
    public Op0 copy()                        { return new OpDatasetNames(graphNode) ; }

    @Override
    public int hashCode()
    { return graphNode.hashCode() ; }

    @Override
    public boolean equalTo(Op op2, NodeIsomorphismMap labelMap)
    {
        if ( ! ( op2 instanceof OpDatasetNames) ) return false ;
        OpDatasetNames other = (OpDatasetNames)op2 ;
        return graphNode.equals(other.graphNode) ;
    }
}
