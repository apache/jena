/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: GraphRDBMaker.java,v 1.1 2003-05-08 15:18:26 chris-dollin Exp $
*/

package com.hp.hpl.jena.db.impl;

import com.hp.hpl.jena.db.GraphRDB;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.*;
import com.hp.hpl.jena.shared.*;

import java.util.*;

/**
    A GraphFactory that produces Graphs from database connections. 
    The connection is supplied when the factory is constructed. All the
    created graphs are tracked so that we can supply a removeAll call
    to dispose of them.

    @author kers 
*/

public class GraphRDBMaker extends BaseGraphMaker
    {
    private IDBConnection c;
    private int counter = 0;
    private Set created = new HashSet();
    
    /**
        Construct a new GraphRDB factory based on the supplied DB connection.
        @param c the database connection
    */
    public GraphRDBMaker( IDBConnection c ) { this.c = c; }
     
    /**
     	@see com.hp.hpl.jena.graph.GraphFactory#getGraph()
     */
    public Graph getGraph()
        { return createGraph( "anon_" + counter++ + "", false ); }
    
    /**
     	Create an RDB graph and remember its name.
     	@see com.hp.hpl.jena.graph.GraphFactory#createGraph(java.lang.String)
     */
    public Graph createGraph( String name, boolean strict )
        {
        created.add( name );
        boolean fresh = strict || !hasGraph( name );
        return consGraph( name, fresh );
        }
    
    /**
        Open an existing graph; if there's no such graph, but failIfAbsent is
        false, create a new one. In any case, return that graph.
    */
    public Graph openGraph( String name, boolean strict )
        {
        boolean fresh = hasGraph( name ) == false && strict == false;
        return consGraph( name, fresh );
        }
        
    private Graph consGraph( String name, boolean fresh )
        {        
        Graph p = c.getDefaultModelProperties().getGraph();
        return new GraphRDB( c, name, (fresh ? p : null), fresh );
        }
        
    /**
     	Remove a graph from the database - at present, this has to be done by
        opening it first.
        
     	@see com.hp.hpl.jena.graph.GraphFactory#removeGraph(java.lang.String)
     */
    public void removeGraph( String name )
        {
        GraphRDB toDelete = (GraphRDB) openGraph( name, true );
        toDelete.remove();
        toDelete.close();
        created.remove( name );
        }
        
    /**
        Return true iff there's a graph with the given name. The implementation
        is horrid.
    */
    public boolean hasGraph( String name )
        {
        DBPropGraph properties = DBPropGraph.findPropGraphByName
            ( c.getDriver().getSystemSpecializedGraph(), name.toUpperCase() );
        return properties != null; 
        }
        
    /**
        Remove all the graphs that have been created by this factory.
    */
    public void removeAll()
        {
        Iterator it = new HashSet( created ).iterator();
        while (it.hasNext()) removeGraph( (String) it.next() );
        }
        
    public void close()
        { /* should consider - do we close the connection or not? */ }
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