/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.migrate;

import com.hp.hpl.jena.graph.BulkUpdateHandler ;
import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.TransactionHandler ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.graph.impl.SimpleBulkUpdateHandler ;
import com.hp.hpl.jena.graph.impl.SimpleTransactionHandler ;
import com.hp.hpl.jena.graph.impl.WrappedGraph ;
import com.hp.hpl.jena.shared.AddDeniedException ;
import com.hp.hpl.jena.shared.DeleteDeniedException ;

public class GraphReadOnly extends WrappedGraph
{
    public GraphReadOnly(Graph graph) { super(graph) ; }
    
    @Override
    public void add(Triple t) throws AddDeniedException
    { throw new AddDeniedException("read-only graph") ; }

    @Override
    public void performAdd(Triple t) throws AddDeniedException
    { throw new AddDeniedException("read-only graph") ; }

    @Override
    public void delete(Triple t) throws DeleteDeniedException
    { throw new DeleteDeniedException("read-only graph") ; }
    
    @Override
    public void performDelete(Triple t) throws DeleteDeniedException
    { throw new DeleteDeniedException("read-only graph") ; }
    
    @Override
    public TransactionHandler getTransactionHandler()
    {
        // AKA "no".  
        return new SimpleTransactionHandler() ;
    }

    @Override
    public BulkUpdateHandler getBulkUpdateHandler()
    {
        //This turns all operations into calls to add/remove.
        return new SimpleBulkUpdateHandler(this) ;
    }
}

/*
 * (c) Copyright 2011 Epimorphics Ltd.
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