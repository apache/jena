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

package com.hp.hpl.jena.rdf.model.impl;


import java.util.*;



import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.*;

import com.hp.hpl.jena.graph.*;


/**
 *  Builds Jena Iterators and other things (RDFNode and Statement)
 *  needed in a Model.
 */
public final class IteratorFactory
    {
    private IteratorFactory(){}

	/**
	 * 
	 */
	static public StmtIterator asStmtIterator( Iterator<Triple> i, final ModelCom m ) 
	    {
	    Map1<Triple, Statement> asStatement = new Map1<Triple, Statement>() 
	        { @Override
            public Statement map1( Triple t ) { return m.asStatement( t ); }};
	    return new StmtIteratorImpl( WrappedIterator.create( i ).mapWith( asStatement ) );
	   }

	/**
	 * 
	 */
	static public ResIterator asResIterator( Iterator<Node> i, final ModelCom m) 
	    {
		Map1<Node, Resource> asResource = new Map1<Node, Resource>() 
		    { @Override
            public Resource map1( Node o) { return (Resource) m.asRDFNode( o ); }};
		return new ResIteratorImpl( WrappedIterator.create( i ).mapWith( asResource ), null );
	    }

	/**
	 * 
	 */
	static public NodeIterator asRDFNodeIterator( Iterator<Node> i, final ModelCom m) 
	    {      
	    Map1<Node, RDFNode> asRDFNode = new Map1<Node, RDFNode>() 
	        { @Override
            public RDFNode map1( Node o) { return m.asRDFNode( o ); }};
	    return new NodeIteratorImpl( WrappedIterator.create( i ).mapWith( asRDFNode ), null );
	    }
	    
    static  Resource asResource( Node n, ModelCom m )  
        { return asResource( n, Resource.class, m );  }	    
    
    static Property asProperty( Node n, ModelCom m ) 
        { return (Property)asResource( n, Property.class, m ); }
    
    static Literal asLiteral(Node n,ModelCom m) 
        { return m.getNodeAs( n, Literal.class ); }
    
    static <X extends RDFNode> Resource asResource( Node n, Class<X> cl, ModelCom m ) 
        { return (Resource) m.getNodeAs( n, cl ); }
    }
