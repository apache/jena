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

package com.hp.hpl.jena.graph.query.test;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.graph.test.GraphTestBase;
import com.hp.hpl.jena.util.iterator.Map1;

/**
 	Various things that are common to some of the query/expression
 	tests, so pulled up here into a shared superclass.
 	
 	@author hedgehog
*/
public abstract class QueryTestBase extends GraphTestBase
    {
    public QueryTestBase( String name ) 
        { super( name ); }
    
    /**
     	An expression that is true if x and y differ. Variable nodes
     	are treated as variables, non-variable nodes as constants.
    */
    protected Expression notEqual( Node x, Node y )
        { 
        return new Dyadic( asExpression( x ), "http://jena.hpl.hp.com/constraints/NE", asExpression( y ) )
        	{
            @Override public boolean evalBool( Object x, Object y )
                { return !x.equals( y ); }
        	};  
        }
        
    /**
     	An expression that is true if x and y are equal. Variable nodes
     	are treated as variables, non-variable nodes as constants.
    */
    protected Expression areEqual( Node x, Node y )
        { 
        return new Dyadic( asExpression( x ), "http://jena.hpl.hp.com/constraints/EQ", asExpression( y ) ) 
        	{            
            @Override public boolean evalBool( Object x, Object y )
                { return x.equals( y ); }
        	};  
        }
        
    /**
     	An expression that is true if x and y "match", in the sense
     	that x contains y. Variable nodes are treated as variables, 
     	non-variable nodes as constants.
    */
    protected Expression matches( Node x, Node y )
	    {
	    return new Dyadic( asExpression( x ), "http://jena.hpl.hp.com/constraints/MATCHES", asExpression( y ) ) 
	        {
	        @Override public boolean evalBool( Object L, Object R )
	            {
                Node l = (Node) L, r = (Node) R;
                return l.toString( false ).indexOf( r.toString( false ) ) > -1; 
                }       
	        };    
	    }

    /**
        Answer a filter that selects the <code>index</code>th element of the
        list it's given.
    */
    protected Map1<Domain, Node> select( final int index )
        {
        return new Map1<Domain, Node>() 
            { @Override
            public Node map1( Domain o ) { return o.get( index ); } };
        }

    /**
     	Answer an expression that evaluates the node <code>x</code>,
     	treating variable nodes as variables (<i>quelle surprise</i>) and other
     	nodes as constants.
    */
    public static Expression asExpression( final Node x )
	    {
	    if( x.isVariable()) return new Expression.Variable()
	        {
	        @Override
            public String getName()
	            { return x.getName(); }
	    
	        @Override
            public Valuator prepare( VariableIndexes vi )
	            { return new SlotValuator( vi.indexOf( x.getName() ) ); }
	        };
	    return new Expression.Fixed( x );
	    }

    /**
     	A Map1 (suitable for a .mapWith iterator conversion) which 
     	assumes the elements are lists and extracts their first elements.
    */
    protected static Map1<Domain, Node> getFirst = new Map1<Domain, Node>() 
    	{ @Override
        public Node map1( Domain x ) { return x.get(0); } };  
    
    /**
       An IndexValues with no elements - ever slot maps to null
    */
    protected static final IndexValues noIVs = new IndexValues() 
        { @Override
        public Object get( int i ) { return null; } };

    /**
         A mapping with no elements pre-defined
    */
    protected static final Mapping emptyMapping = new Mapping( new Node[0] );

    /**
     	A mapping from variables to their indexes where no variables
     	are defined (all names map to the non-existant slot -1).
     */
    protected VariableIndexes noVariables = new VariableIndexes()
        { @Override
        public int indexOf( String name ) { return -1; } };
        
    /**
     	A Node with spelling "X".
    */
    protected static final Node X = GraphQuery.X;

    /**
 		A Node with spelling "Y".
    */
    protected static final Node Y = GraphQuery.Y;
    
    /**
 		A Node with spelling "Z".
    */
    protected static final Node Z = GraphQuery.Z;

    /**
        A convenient way to refer to Node.ANY
    */
    protected static final Node ANY = Node.ANY;
    
    /**
     	An array containing just the node X.
    */
    protected final Node [] justX = new Node [] {X};
    }
