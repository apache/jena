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
import com.hp.hpl.jena.graph.NodeFactory ;

import org.apache.jena.atlas.logging.Log ;

import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;

/** A SERVICE pattern - access a remote SPARQL service. */

public class ElementService extends Element
{
    private final Node serviceNode ;
    private final Element element ;
    private final boolean silent ;

    public ElementService(String serviceURI, Element el)
    { 
        this(NodeFactory.createURI(serviceURI), el, false) ;
    }
    
    public ElementService(String serviceURI, Element el, boolean silent)
    { 
        this(NodeFactory.createURI(serviceURI), el, silent) ;
    }
    
    // Variable?
    public ElementService(Node n, Element el, boolean silent)
    {
        if ( ! n.isURI() && ! n.isVariable() )
            Log.fatal(this, "Must be a URI (or variable which will be bound) for a service endpoint") ;
        this.serviceNode = n ;
        this.element = el ;
        this.silent = silent ;
    }
    
    public Element getElement() { return element ; } 
    public Node getServiceNode() { return serviceNode ; }
    public boolean getSilent() { return silent ; } 
    
    @Override
    public int hashCode()
    { return serviceNode.hashCode() ^ element.hashCode(); }

    @Override
    public boolean equalTo(Element el2, NodeIsomorphismMap isoMap)
    {
        if ( ! ( el2 instanceof ElementService ) )
            return false ;
        ElementService service = (ElementService)el2 ;
        if ( ! serviceNode.equals(service.serviceNode) )
            return false ;
        if ( service.getSilent() != getSilent() )
            return false ;
        if ( ! this.getElement().equalTo(service.getElement(), isoMap) )
            return false ;
        return true ;
    }
    
    @Override
    public void visit(ElementVisitor v) { v.visit(this) ; }
}
