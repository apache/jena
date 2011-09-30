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

package org.openjena.riot.pipeline;

import org.openjena.atlas.lib.Sink ;
import org.openjena.atlas.lib.SinkWrapper ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.graph.NodeTransform ;

/** Apply a node transform to each node in a triple */ 
public class SinkTripleNodeTransform extends SinkWrapper<Triple>
{
    private final NodeTransform subjTransform ;
    private final NodeTransform predTransform ;
    private final NodeTransform objTransform ;

    /** Apply the nodeTransform to each of S, P and O */
    public SinkTripleNodeTransform(Sink<Triple> sink, NodeTransform nodeTransform)
    {
        this(sink, nodeTransform, nodeTransform, nodeTransform) ;
    }
    
    /** Apply the respective nodeTransform to the slot in the triple */
    public SinkTripleNodeTransform(Sink<Triple> sink, NodeTransform subjTransform, NodeTransform predTransform, NodeTransform objTransform)
    {
        super(sink) ;
        this.subjTransform = subjTransform ;
        this.predTransform = predTransform ;
        this.objTransform = objTransform ;
        
    }

    @Override
    public void send(Triple triple)
    {
        Node s = triple.getSubject() ;
        Node p = triple.getPredicate() ;
        Node o = triple.getObject() ;
        
        Node s1 = apply(subjTransform, s) ;
        Node p1 = apply(predTransform, p) ;
        Node o1 = apply(objTransform, o) ;

        if ( s != s1 || p != p1 || o != o1 )
            triple = new Triple(s1, p1, o1) ;
        
        super.send(triple) ;
    }
    
    private static Node apply(NodeTransform nodeTransform, Node node)
    {
        if ( nodeTransform == null ) return node ;
        Node n2 = nodeTransform.convert(node) ;
        if ( n2 == null ) return node ;
        return n2 ;
    }
}
