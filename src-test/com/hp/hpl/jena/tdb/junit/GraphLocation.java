/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.junit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lib.FileOps;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.pgraph.GraphBTree;

public class GraphLocation
{
    private Location loc = null ;
    private Graph graph = null ;
    private Model model = null ;
    
    public GraphLocation(Location loc)
    {
        this.loc = loc ;
    }
    
    public void clearDirectory() { FileOps.clearDirectory(loc.getDirectoryPath()) ; }
    
    public Graph getGraph() { return graph ; }
    
    public Model getModel() { return model ; }
    
    public Graph createGraph()
    {
        if ( graph != null )
            throw new PGraphTestException("Graph already in use") ;
        graph = new GraphBTree(loc) ;
        model = ModelFactory.createModelForGraph(graph) ;
        return graph ;
    }
    
    public void clearGraph()
    { 
        if ( graph != null )
        {
            List<Triple> stmts = new ArrayList<Triple>() ;
            @SuppressWarnings("unchecked")
            Iterator<Triple> iter = (Iterator<Triple>)graph.find(Node.ANY, Node.ANY, Node.ANY) ;
            for ( ; iter.hasNext() ; )
                graph.delete(iter.next()) ;
        }
    }

    public void releaseGraph()
    {
        if ( graph != null )
        {
            graph.close();
            graph = null ;
            model = null ;
        }
    }
}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
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
 *
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