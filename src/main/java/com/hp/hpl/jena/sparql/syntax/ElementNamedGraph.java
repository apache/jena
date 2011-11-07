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

package com.hp.hpl.jena.sparql.syntax;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;

/** Evaluate a query element based on source information in a named collection. */

public class ElementNamedGraph extends Element
{
    private Node sourceNode ;
    private Element element ;

    // GRAPH * (not in SPARQL)
    public ElementNamedGraph(Element el)
    {
        this(null, el) ;
    }

    // GRAPH <uri> or GRAPH ?var
    public ElementNamedGraph(Node n, Element el)
    {
        sourceNode = n ;
        element = el ;
    }
    
    public Node getGraphNameNode() { return sourceNode ; }
    
    /** @return Returns the element. */
    public Element getElement()
    {
        return element ;
    }
    
    @Override
    public int hashCode() { return element.hashCode() ^ sourceNode.hashCode() ; }

    @Override
    public boolean equalTo(Element el2, NodeIsomorphismMap isoMap)
    {
        if ( el2 == null ) return false ;

        if ( ! ( el2 instanceof ElementNamedGraph ) ) 
            return false ;
        ElementNamedGraph g2 = (ElementNamedGraph)el2 ;
        if ( ! this.getGraphNameNode().equals(g2.getGraphNameNode()) )
            return false ;
        if ( ! this.getElement().equalTo(g2.getElement(), isoMap) )
            return false ;
        return true ;
    }

    
    @Override
    public void visit(ElementVisitor v) { v.visit(this) ; }
}
