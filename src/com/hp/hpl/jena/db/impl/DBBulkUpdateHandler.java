/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: DBBulkUpdateHandler.java,v 1.4 2003-07-10 12:53:08 chris-dollin Exp $
*/

package com.hp.hpl.jena.db.impl;

import java.util.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.db.*;

/**
    An implementation of the bulk update interface.
    
 	@author csayers based on SimpleBulkUpdateHandler by kers
 	@version $Revision: 1.4 $
*/

public class DBBulkUpdateHandler implements BulkUpdateHandler {
	private GraphRDB graph;
    private GraphEventManager manager;
    
	protected static int CHUNK_SIZE = 50;

	public DBBulkUpdateHandler(GraphRDB graph) {
		this.graph = graph;
        this.manager = graph.getEventManager();
	}

	public void add(Triple[] triples) {
		add( Arrays.asList(triples), false );
        manager.notifyAdd( triples );
	}

	public void add( List triples ) 
        { add( triples, true ); }
        
    protected void add( List triples, boolean notify ) {
		graph.add(triples);
        if (notify) manager.notifyAdd( triples );
	}

	public void add(Iterator it) {
		ArrayList list = new ArrayList(CHUNK_SIZE);
		while (it.hasNext()) {
			while (it.hasNext() && list.size() < CHUNK_SIZE) {
				list.add(it.next());
			}
			graph.add(list);
			list.clear();
		}
	}

	public void add(Graph g) {
		ExtendedIterator triplesToAdd = g.find(null, null, null);
		add(triplesToAdd);
		triplesToAdd.close();
	}

	public void delete( Triple[] triples ) {
		delete( Arrays.asList(triples), false );
        manager.notifyDelete( triples );
	}

    public void delete( List triples )
        { delete( triples, true ); }
        
	protected void delete(List triples, boolean notify ) {
		graph.delete( triples );
        if (notify) manager.notifyDelete( triples );
	}

	public void delete(Iterator it) {
		ArrayList list = new ArrayList(CHUNK_SIZE);
		while (it.hasNext()) {
			while (it.hasNext() && list.size() < CHUNK_SIZE) {
				list.add(it.next());
			}
			graph.delete(list);
			list.clear();
		}
	}

	public void delete(Graph g) {
		ExtendedIterator triplesToDelete = g.find(null, null, null);
		delete(triplesToDelete);
		triplesToDelete.close();
	}
}


/*
    (c) Copyright Hewlett-Packard Company 2003
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