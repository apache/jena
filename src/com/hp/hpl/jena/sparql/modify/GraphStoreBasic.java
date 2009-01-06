/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.modify;

import com.hp.hpl.jena.graph.Graph;

import com.hp.hpl.jena.sparql.core.DataSourceGraphImpl;
import com.hp.hpl.jena.sparql.core.DataSourceImpl;
import com.hp.hpl.jena.sparql.util.graph.GraphUtils;

import com.hp.hpl.jena.query.Dataset;

import com.hp.hpl.jena.update.GraphStore;

public class GraphStoreBasic extends DataSourceGraphImpl implements GraphStore
{
    public GraphStoreBasic() { super.setDefaultGraph(GraphUtils.makeDefaultGraph()) ; }
    
    public GraphStoreBasic(Dataset ds) { super(ds) ; }
    
    public GraphStoreBasic(Graph graph)
    {
        super(graph) ;
    }

    
    
    public Dataset toDataset()
    {
        // This is a shallow structure copy.
        return new DataSourceImpl(this) ;
    }

    public void startRequest()
    { GraphStoreUtils.sendToAll(this, GraphStoreEvents.RequestStartEvent) ; }
    public void finishRequest()
    { GraphStoreUtils.sendToAll(this, GraphStoreEvents.RequestFinishEvent) ; }
    
    @Override
    public void close()
    {
        GraphStoreUtils.actionAll(this, 
                                  new GraphStoreAction()
        {
            public void exec(Graph graph){ graph.close() ; }
        }) ;
    }
}
/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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