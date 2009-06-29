/*
  (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
  [See end of file]
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

/*
 *  (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.

 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */