/*
  (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: DBBulkUpdateHandler.java,v 1.19 2005-02-21 12:02:42 andy_seaborne Exp $
*/

package com.hp.hpl.jena.db.impl;

import java.util.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.IteratorCollection;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.graph.impl.*;
import com.hp.hpl.jena.db.*;

/**
    An implementation of the bulk update interface. Updated by kers to permit event
    handling for bulk updates.
    
 	@author csayers based on SimpleBulkUpdateHandler by kers
 	@version $Revision: 1.19 $
*/

public class DBBulkUpdateHandler implements BulkUpdateHandler {
	private GraphRDB graph;
    private GraphEventManager manager;
    
	protected static int CHUNK_SIZE = 50;

	public DBBulkUpdateHandler(GraphRDB graph) {
		this.graph = graph;
        this.manager = graph.getEventManager();
	}

    /**
        add a list of triples to the graph; the add is done as a list with notify off,
        and then the array-notify invoked.
    */
	public void add(Triple[] triples) {
		add( Arrays.asList(triples), false );
        manager.notifyAddArray( graph, triples );
	}

	public void add( List triples ) 
        { add( triples, true ); }
        
    /**
        add a list of triples to the graph, notifying only if requested.
    */
    protected void add( List triples, boolean notify ) {
		graph.add(triples);
        if (notify) manager.notifyAddList( graph, triples );
	}

    /**
        Add the [elements of the] iterator to the graph. Complications arise because
        we wish to avoid duplicating the iterator if there are no listeners; otherwise
        we have to read the entire iterator into a list and use add(List) with notification
        turned off.
     	@see com.hp.hpl.jena.graph.BulkUpdateHandler#add(java.util.Iterator)
     */
	public void add(Iterator it) 
        { 
        if (manager.listening())
            {
            List L = IteratorCollection.iteratorToList( it );
            add( L, false );
            manager.notifyAddIterator( graph, L );    
            }
        else
            addIterator( it ); 
        }
    
    protected void addIterator( Iterator it )
    {
		ArrayList list = new ArrayList(CHUNK_SIZE);
		while (it.hasNext()) {
			while (it.hasNext() && list.size() < CHUNK_SIZE) {
				list.add( it.next() );
			}
			graph.add(list);
			list.clear();
		}
    }
        
    public void add( Graph g )
        { add( g, false ); }
        
	public void add( Graph g, boolean withReifications ) {
		ExtendedIterator triplesToAdd = GraphUtil.findAll( g );
		try { addIterator( triplesToAdd ); } finally { triplesToAdd.close(); }
        if (withReifications) SimpleBulkUpdateHandler.addReifications( graph, g );
        manager.notifyAddGraph( graph, g );
	}

    /**
        remove a list of triples from the graph; the remove is done as a list with notify off,
        and then the array-notify invoked.
    */
	public void delete( Triple[] triples ) {
		delete( Arrays.asList(triples), false );
        manager.notifyDeleteArray( graph, triples );
	}

    public void delete( List triples )
        { delete( triples, true ); }
        
    /**
        Add a list of triples to the graph, notifying only if requested.
    */
	protected void delete(List triples, boolean notify ) {
		graph.delete( triples );
        if (notify) manager.notifyDeleteList( graph, triples );
	}
    
    /**
        Delete the [elements of the] iterator from the graph. Complications arise 
        because we wish to avoid duplicating the iterator if there are no listeners; 
        otherwise we have to read the entire iterator into a list and use delete(List) 
        with notification turned off.
        @see com.hp.hpl.jena.graph.BulkUpdateHandler#add(java.util.Iterator)
     */
    public void delete(Iterator it) 
        { 
        if (manager.listening())
            {
            List L = IteratorCollection.iteratorToList( it );
            delete( L, false );
            manager.notifyDeleteIterator( graph, L );    
            }
        else
            deleteIterator( it ); 
        }
    
	protected void deleteIterator(Iterator it) {
		ArrayList list = new ArrayList(CHUNK_SIZE);
		while (it.hasNext()) {
			while (it.hasNext() && list.size() < CHUNK_SIZE) {
				list.add(it.next());
			}
			graph.delete(list);
			list.clear();
		}
	}

	public void delete(Graph g)
        { delete( g, false ); }
        
    public void delete( Graph g, boolean withReifications ) {
		ExtendedIterator triplesToDelete = GraphUtil.findAll( g );
		try { deleteIterator( triplesToDelete ); } finally { triplesToDelete.close(); }
        if (withReifications) SimpleBulkUpdateHandler.deleteReifications( graph, g );
        manager.notifyDeleteGraph( graph, g );
   	}
    
    public void removeAll()
        { SimpleBulkUpdateHandler.removeAll( graph ); 
        manager.notifyEvent( graph, GraphEvents.removeAll ); }
    
    public void remove( Node s, Node p, Node o )
        { SimpleBulkUpdateHandler.removeAll( graph, s, p, o ); 
        manager.notifyEvent( graph, GraphEvents.remove( s, p, o ) ); }
}


/*
    (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/