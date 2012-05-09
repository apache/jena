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

package com.hp.hpl.jena.graph.impl;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.shared.ReificationStyle;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
	WrappedReifier: a class that wraps a reifier [needed by WrappedGraph].

	@author kers
*/
public class WrappedReifier implements Reifier 
    {
    private Reifier base;
    private Graph parent;
	/**
	 * 
	*/
	public WrappedReifier( Reifier base, Graph parent )  
        { this.base = base; this.parent = parent; }

	/** 
	 	@see com.hp.hpl.jena.graph.Reifier#getStyle()
	*/
	@Override
    public ReificationStyle getStyle() { return base.getStyle(); }
	/** 
	 	@see com.hp.hpl.jena.graph.Reifier#getParentGraph()
	*/
	@Override
    public Graph getParentGraph() { return parent; }
    
    @Override
    public ExtendedIterator<Triple> find( TripleMatch m ) { return base.find( m ); }
    
    @Override
    public ExtendedIterator<Triple> findExposed( TripleMatch m ) { return base.findExposed( m ); }
    
    @Override
    public ExtendedIterator<Triple> findEither( TripleMatch m, boolean showHidden ) 
        { return base.findEither( m, showHidden ); }
    
    @Override
    public int size() { return base.size(); }

	/** 
	 	@see com.hp.hpl.jena.graph.Reifier#reifyAs(com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.graph.Triple)
	*/
	@Override
    public Node reifyAs( Node n, Triple t ) { return base.reifyAs( n, t ); }

	/** 
	 	@see com.hp.hpl.jena.graph.Reifier#hasTriple(com.hp.hpl.jena.graph.Node)
	*/
	@Override
    public boolean hasTriple( Node n ) { return base.hasTriple( n ); }

	/** 
	 	@see com.hp.hpl.jena.graph.Reifier#hasTriple(com.hp.hpl.jena.graph.Triple)
	*/
	@Override
    public boolean hasTriple( Triple t ) { return base.hasTriple( t ); }

	/** 
	 	@see com.hp.hpl.jena.graph.Reifier#allNodes()
	*/
	@Override
    public ExtendedIterator<Node> allNodes() { return base.allNodes(); }

	/** 
	 	@see com.hp.hpl.jena.graph.Reifier#allNodes(com.hp.hpl.jena.graph.Triple)
	*/
	@Override
    public ExtendedIterator<Node> allNodes( Triple t ) { return base.allNodes( t ); }
	/** 
	 	@see com.hp.hpl.jena.graph.Reifier#remove(com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.graph.Triple)
	*/
	@Override
    public void remove( Node n, Triple t ) { base.remove( n, t ); }

	/** 
	 	@see com.hp.hpl.jena.graph.Reifier#remove(com.hp.hpl.jena.graph.Triple)
	*/
	@Override
    public void remove( Triple t ) { base.remove( t ); }
	/** 
	 	@see com.hp.hpl.jena.graph.Reifier#handledAdd(com.hp.hpl.jena.graph.Triple)
	*/
	@Override
    public boolean handledAdd( Triple t ) { return base.handledAdd( t ); }

	/** 
	 	@see com.hp.hpl.jena.graph.Reifier#handledRemove(com.hp.hpl.jena.graph.Triple)
	*/
	@Override
    public boolean handledRemove( Triple t ) { return base.handledRemove( t ); }

	/** 
	 	@see com.hp.hpl.jena.graph.GetTriple#getTriple(com.hp.hpl.jena.graph.Node)
	*/
	@Override
    public Triple getTriple( Node n ) { return base.getTriple( n ); }

    /**
     	@see com.hp.hpl.jena.graph.Reifier#close()
    */
    @Override
    public void close() { base.close(); }

    }
