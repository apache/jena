/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.modify;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.store.DatasetStoreGraph;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.modify.UpdateProcessorVisitor;
import com.hp.hpl.jena.sparql.modify.op.UpdateClear;
import com.hp.hpl.jena.sparql.modify.op.UpdateCreate;
import com.hp.hpl.jena.sparql.modify.op.UpdateDrop;
import com.hp.hpl.jena.sparql.modify.op.UpdateExt;

public class UpdateProcessorVisitorSDB extends UpdateProcessorVisitor
{
    Store store ;
    DatasetStoreGraph graphStore ;
    
    UpdateProcessorVisitorSDB(DatasetStoreGraph graphStore, Binding inputBinding)
    {
        super(graphStore, inputBinding) ;
        this.graphStore = graphStore ;
    }

//    @Override public void visit(UpdateModify modify) {}
//
//    @Override public void visit(UpdateDelete delete) {}
//
//    @Override public void visit(UpdateInsert insert) {}
// 
//    @Override public void visit(UpdateInsertData arg) {}
//
//    @Override public void visit(UpdateDeleteData arg) {}
//
//    @Override public void visit(UpdateLoad load) {}

    @Override 
    public void visit(UpdateClear clear)
    {
        clearGraph(clear.getGraphName()) ;
    }

    @Override
    public void visit(UpdateDrop drop)
    { 
        clearGraph(drop.getIRI()) ;
    }

    private void clearGraph(Node n)
    {
        Graph g ;
        if (n != null )
            g = SDBFactory.connectNamedGraph(store, n) ;
        else
            g = SDBFactory.connectDefaultGraph(store) ;
        // Delete all triples.
        g.getBulkUpdateHandler().removeAll() ;
    }
    
    @Override
    public void visit(UpdateCreate create)
    { /* No-op in SDB (until a graph management module is written) */ }

    @Override
    public void visit(UpdateExt arg)
    { throw new SDBException("UpdateExt - not supported for SDB") ; }
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