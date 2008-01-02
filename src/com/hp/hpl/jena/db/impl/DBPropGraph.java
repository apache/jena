/*
  (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
  [See end of file]
*/

package com.hp.hpl.jena.db.impl;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.vocabulary.DB;

import java.util.*;

/**
 *
 * A wrapper to assist in getting and setting DB information from 
 * a persistent store.
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
 * @version $Revision: 1.29 $
 * @since Jena 2.0
 */
public class DBPropGraph extends DBProp {

	public static Node_URI graphName = (Node_URI)DB.graphName.asNode();
	public static Node_URI graphType = (Node_URI)DB.graphType.asNode();
	public static Node_URI graphLSet = (Node_URI)DB.graphLSet.asNode();
	public static Node_URI graphPrefix = (Node_URI)DB.graphPrefix.asNode();
	public static Node_URI graphId = (Node_URI)DB.graphId.asNode();
	public static Node_URI stmtTable = (Node_URI)DB.stmtTable.asNode();
	public static Node_URI reifTable = (Node_URI)DB.reifTable.asNode();


	
	public DBPropGraph( SpecializedGraph g, String symbolicName, String type) {
		super(g);
		
		putPropString(graphName, symbolicName);
		putPropString(graphType, type);
	}
	
	public DBPropGraph( SpecializedGraph g, Node n) {
		super(g,n);
	}	
	
	public DBPropGraph( SpecializedGraph g, String newSymbolicName, Graph oldProperties) {
		super(g);
		
		putPropString(graphName, newSymbolicName);
		// only copy user-configurable properties
		Iterator it = oldProperties.find( Node.ANY, Node.ANY, Node.ANY);
		while ( it.hasNext() ) {
			Triple t = (Triple) it.next();
			if ( t.getPredicate().equals(graphName) ||
				t.getPredicate().equals(graphId) ||
				t.getPredicate().equals(stmtTable) ||
				t.getPredicate().equals(reifTable) )
				continue;
			putPropNode((Node_URI)t.getPredicate(),t.getObject());
		}
	}

    /**
        Answer false if already within a transaction, otherwise start a transaction
        and answer true; use in conjunction with <code>conditionalCommit</code>.
     */
	public boolean begin()
        { return getDriver().xactOp( DriverRDB.xactBeginIfNone ); }

    /**
        If <code>commit</code> is true, commit the current transaction; to be used
        in conjunction with <code>begin</code>. 
    */
    public void conditionalCommit( boolean commit )
        { if (commit) getDriver().xactOp( DriverRDB.xactCommit ); }
    
    private DriverRDB getDriver()
        { return (DriverRDB) graph.getPSet().driver(); }
    
	public void addLSet( DBPropLSet lset ) {
		putPropNode( graphLSet, lset.getNode() );
	}

    public void addPrefix( String prefix, String uri ) {
        Node prefixNode = Node.createLiteral( prefix ), uriNode = Node.createLiteral( uri );
        boolean commit = begin();
        Node B = bnodeForPrefix( prefixNode );
        if (B == null) 
            addPrefixMaplet( prefixNode, uriNode );
        else 
            updatePrefixMaplet( B, uriNode );
        conditionalCommit( commit );
    }

    /**
     	Update the existing prefix maplet off bnode <code>B</code> so that its
        prefixURI is now <code>uriNode</code>.
    */
    private void updatePrefixMaplet( Node B, Node uriNode )
        {
        Node current = getPropNode( B, DBPropPrefix.prefixURI );
        if (!uriNode.equals( current ))
            {
            delete( B, DBPropPrefix.prefixURI, current );
            add( B, DBPropPrefix.prefixURI, uriNode );
            }
        }

    /**
     	Add a new prefix maplet <code>[prefixValue prefixNode; prefixURI uriNode]</code>
        to the graph.
    */
    private void addPrefixMaplet( Node prefixNode, Node uriNode )
        {
        Node BB = Node.createAnon();
        add( self, graphPrefix, BB );
        add( BB, DBPropPrefix.prefixURI, uriNode );
        add( BB, DBPropPrefix.prefixValue, prefixNode );
        }  
    
    /**
        Add the triple <code>(S, P, O)</code> to the graph.
    */
    private void add( Node S, Node P, Node O )
        { graph.add( Triple.create( S, P, O ), newComplete() ); }
    
    /**
        Remove the triple <code>(S, P, O)</code> from the graph.
    */    
    private void delete( Node S, Node P, Node O )
        { graph.delete( Triple.create( S, P, O ), newComplete() ); }
    
    /**
         Answer the bnode which gives the prefix and uri in this graph for the given 
         literal<code>prefixNode</code>, or null if there isn't one.
    */
    public Node bnodeForPrefix( Node prefixNode )
        {
        ExtendedIterator A = graph.find( self, graphPrefix, Node.ANY, newComplete() );
        try
            {
            while (A.hasNext())
                {
                Node B = ((Triple) A.next()).getObject();
                if (graph.contains( Triple.create( B, DBPropPrefix.prefixValue, prefixNode ), newComplete() ) ) return B;
                }
            return null;
            }
        finally
            { A.close(); }
        }
    
    /**
         @deprecated this method should never have been publically visible
        @param prefix
     */
	public void addPrefix( DBPropPrefix prefix ) {
		// First drop existing uses of prefix or URI
		DBPropPrefix existing = getPrefix( prefix.getValue() );
		if( existing != null)
            {
			removePrefix( existing);
            }
		putPropNode( graphPrefix, prefix.getNode() );
	}
    
	/**
        @deprecated this method should never habve been publically visible 
	*/
	public void removePrefix( DBPropPrefix prefix ) {
		SpecializedGraph.CompletionFlag complete = newComplete();
		Iterator matches = graph.find( self, graphPrefix, prefix.getNode(), complete);
		if( matches.hasNext() ) {
			graph.delete( (Triple)(matches.next()), complete );
			prefix.remove();
		}
	}
    
    public void removePrefix( String prefix ) {
        Node prefixNode = Node.createLiteral( prefix );
        boolean commit = begin();
        Node B = bnodeForPrefix( prefixNode );
        if (B != null)
            {
            Node uriNode = getPropNode( B, DBPropPrefix.prefixURI );
            delete( self, graphPrefix, B );
            delete( B, DBPropPrefix.prefixURI, prefixNode );
            delete( B, DBPropPrefix.prefixValue, uriNode );
            }
        conditionalCommit( commit );
    }
	
	public void addGraphId( int id ) {
		putPropString(graphId, Integer.toString(id));
	}

	public void addStmtTable( String table ) {
		putPropString(stmtTable, table);
	}
	
	public void addReifTable( String table ) {
		putPropString(reifTable, table);
	}

	public String getName() { return getPropString( graphName); }

    public String getType() { return getPropString( graphType); }
	
    public String getStmtTable() { return getPropString(stmtTable); }
	
    public String getReifTable() { return getPropString(reifTable); }
	
    public int getGraphId() {
		String i = getPropString(graphId);
		return i == null ? -1 : Integer.parseInt(i);
	}	
	
	public ExtendedIterator getAllLSets() {
		return 
            graph.find( self, graphLSet, null, newComplete() )
             .mapWith ( new MapToLSet() );
	}
	
	public ExtendedIterator getAllPrefixes() {
		return 
            graph.find( self, graphPrefix, null, newComplete() )
            .mapWith ( new MapToPrefix() );
	}
	
    /**
        @deprecated this method should not have been visible
        @param prefix
        @return
     */
	public DBPropPrefix getPrefix( String prefix ) {
		ExtendedIterator prefixes = 
            graph.find( self, graphPrefix, null, newComplete() )
            .mapWith ( new MapToPrefix() );
		while( prefixes.hasNext() ) {
			DBPropPrefix dbp = (DBPropPrefix)prefixes.next();
			if( dbp.getValue().compareTo(prefix)==0) 
				return dbp;
		}
		return null;
	}
	
    /**
        @deprecated this method should not have been visible
        @param uri
        @return
     */
	public DBPropPrefix getURI( String uri ) {
		ExtendedIterator prefixes = getAllPrefixes();
		while( prefixes.hasNext() ) {
			DBPropPrefix prefix = (DBPropPrefix)prefixes.next();
			if( prefix.getURI().compareTo(uri)==0) 
				return prefix;
		}
		return null;
	}
	
	public ExtendedIterator listTriples() {
		// First get all the triples that directly desrcribe this graph
		ExtendedIterator result = DBProp.listTriples( graph, self );
		
		// Now get all the triples that describe any lsets
		ExtendedIterator lsets = getAllLSets();
		while( lsets.hasNext()) {
			result = result.andThen( ((DBPropLSet)lsets.next()).listTriples() );
		}

		// Now get all the triples that describe any prefixes
		ExtendedIterator prefixes = getAllPrefixes();
		while( prefixes.hasNext()) {
			result = result.andThen( ((DBPropPrefix)prefixes.next()).listTriples() );
		}
		return result;
	}
	
	
	private class MapToLSet implements Map1 {
		public Object map1( Object o) {
			Triple t = (Triple) o;
			return new DBPropLSet( graph, t.getObject() );			
		}
	}
	
    /**
        @deprecated only required in deprecated code
        @author kers
     */
	private class MapToPrefix implements Map1 {
		public Object map1( Object o) {
			Triple t = (Triple) o;
			return new DBPropPrefix( graph, t.getObject() );			
		}
	}
	
	public static DBPropGraph findPropGraphByName( SpecializedGraph graph, String name ) {
		Node myNode = Node.createLiteral( name );
		ClosableIterator it = graph.find( null, graphName, myNode, newComplete() );
        
        try {
            if( it.hasNext() )
                return new DBPropGraph( graph, ((Triple)it.next()).getSubject());
            return null;
        }
        finally { it.close(); }
	}
	
	/*
	 * return true if the DBPropGraph has the required
	 * properties for the named, stored graph.
	 */
	
	public boolean isDBPropGraphOk ( String name ) {
		String s = getName();
		boolean res = (s == null) ? false : s.equals(name);		
		res = res & (getGraphId() != -1);
		res = res & (getType() != null);
		res = res & (getStmtTable() != null);
		res = res & (getReifTable() != null);
		return res;
	}
	
	public void remove() {
		Iterator it = getAllPrefixes();
		while( it.hasNext()) {
			((DBPropPrefix)it.next()).remove();			
		}
		it = getAllLSets();
		while( it.hasNext()) {
			((DBPropLSet)it.next()).remove();			
		}
		super.remove();
	}

}

/*
 *  (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
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