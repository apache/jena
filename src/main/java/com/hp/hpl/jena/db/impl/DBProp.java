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

package com.hp.hpl.jena.db.impl;

import java.net.UnknownHostException;
import java.rmi.server.UID;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.vocabulary.DB;


/**
 *
 * A base class for DB property information in a persistent store.
 * 
 * This is written in the style of enhanced nodes - no state is
 * stored in the DBStoreDesc, instead all state is in the
 * underlying graph and this is just provided as a convenience.
 * 
 * (We don't use enhanced nodes because, since we control everything
 * in the persistent store system description, we can avoid any
 * need to handle polymorhphism).
 * 
 * 
 * @author csayers
 * @version $Revision: 1.1 $
 */
public abstract class DBProp {

	protected SpecializedGraph graph = null;
	protected Node self = null;
	
	public DBProp( SpecializedGraph g) {
		graph = g;
		self = generateNodeURI();
	}			
	
	public DBProp( SpecializedGraph g, Node n) {
		graph = g;
		self = n;
	}			
	
	public Node getNode() { return self; }
    
    /**
        Utility method for creating a CompletionFlag; its value lies in having a short name!
        @return a new SpecializedGraph.CompletionFlag() set to <code>false</code>.
    */
    protected static SpecializedGraph.CompletionFlag newComplete()
        { return new SpecializedGraph.CompletionFlag(); }
	
    /**
        Add (self, predicate, literal(value)) to the graph.
    */
	protected void putPropString( Node_URI predicate, String value ) {
		putPropNode( predicate, Node.createLiteral( value ) );
	}		
	
    /**
        Add (self, predicate, node) to the graph.
    */
	protected void putPropNode( Node_URI predicate, Node node ) {
		graph.add( Triple.create( self, predicate, node ), newComplete() );
	}			
	
    /**
        Answer the single string s such that (self, predicate, literal(s)) is in the
        graph, or null if there's no such s.
    */
	protected String getPropString( Node predicate ) {
        Node n = getPropNode( predicate );
        return n == null ? null : n.getLiteralLexicalForm();
	}			   
    
    /**
        Answer the single node n such that (subject, predicate, n) is in the
        graph, or null if there's no such n.
    */
    protected Node getPropNode( Node subject, Node predicate ) {
        ClosableIterator<Triple> it = graph.find( subject, predicate, Node.ANY, newComplete() );
        Node result = it.hasNext() ? it.next().getObject() : null;
        it.close();
        return result;
    }               

    /**
        Answer the single node n such that (self, predicate, n) is in the
        graph, or null if there's no such n.
    */
    protected Node getPropNode( Node predicate ) {
        return getPropNode( self, predicate );
    }           
	
	protected void remove() {
		SpecializedGraph.CompletionFlag complete = newComplete();
		ClosableIterator<Triple> it = graph.find( self, null, null, complete);
		while( it.hasNext() ) graph.delete( it.next(), complete );
		it.close();
		self = null;
		graph = null;
	}
    
    void showGraph()
        {
        SpecializedGraph.CompletionFlag complete = newComplete();
        ExtendedIterator<Triple> it = graph.find( self, null, null, complete );
        while (it.hasNext()) System.err.println( ">> " + it.next() );
        }
	
	public static ExtendedIterator<Triple> listTriples( SpecializedGraph g, Node self ) {
		// Get all the triples about the requested node.
		return g.find( self, null, null, newComplete() );
	}
    
	public static String generateUniqueID() {
		UID uid = new UID();
		String hostname;
		try {
			hostname = java.net.InetAddress.getLocalHost().getHostAddress().toString();
		} catch (UnknownHostException e) {
			hostname = "localhost";
		}
		return (hostname + uid.toString()).replace('.','_').replace(':','_').replace('-','_');
	}

	public static Node generateNodeURI() {
		String generateUniqueID = null;
		return Node.createURI( DB.uri + generateUniqueID() );
	}
	

}
